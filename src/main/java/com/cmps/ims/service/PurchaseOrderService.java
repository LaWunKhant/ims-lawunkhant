package com.cmps.ims.service;

import com.cmps.ims.entity.Company;
import com.cmps.ims.entity.Product;
import com.cmps.ims.entity.PurchaseOrder;
import com.cmps.ims.repository.CompanyRepository;
import com.cmps.ims.repository.ProductRepository;
import com.cmps.ims.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final MailService mailService;

    /**
     * 発注を全件取得（ページング付き、降順）
     */
    @Transactional(readOnly = true)
    public Page<PurchaseOrder> findAllPurchaseOrders(Pageable pageable) {
        return purchaseOrderRepository.findAllByOrderByOrderPlannedDateDesc(pageable);
    }

    /**
     * 発注を検索（複合条件）
     */
    @Transactional(readOnly = true)
    public Page<PurchaseOrder> searchPurchaseOrders(Integer productId, Integer companyId, Integer status,
            LocalDate orderPlannedDateFrom, LocalDate orderPlannedDateTo, Pageable pageable) {
        return purchaseOrderRepository.searchPurchaseOrders(
            productId, companyId, status, orderPlannedDateFrom, orderPlannedDateTo, pageable);
    }

    /**
     * IDで発注を取得
     */
    @Transactional(readOnly = true)
    public Optional<PurchaseOrder> findById(Integer id) {
        return purchaseOrderRepository.findById(id);
    }

    /**
     * 新規発注を作成
     * 支払金額は商品マスタの単価を元に自動計算（単価自体は保存しない）
     */
    public PurchaseOrder createPurchaseOrder(PurchaseOrder po) {
        log.info("新規発注作成: companyId={}, productId={}, quantity={}",
            po.getCompanyId(), po.getProductId(), po.getQuantity());

        Product product = productRepository.findById(po.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません: id=" + po.getProductId()));

        po.calculatePaymentAmount(product.getPrice());
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdateAt(LocalDateTime.now());

        return purchaseOrderRepository.save(po);
    }

    /**
     * 発注を更新
     * 仕入済（status=2）の発注は編集不可
     */
    public PurchaseOrder updatePurchaseOrder(Integer id, PurchaseOrder poDetails) {
        log.info("発注更新: id={}", id);

        PurchaseOrder po = purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("発注が見つかりません: id=" + id));

        if (po.getStatus() != null && po.getStatus() == 2) {
            throw new IllegalArgumentException("仕入済の発注は編集できません");
        }

        if (poDetails.getCompanyId() != null) {
            po.setCompanyId(poDetails.getCompanyId());
        }
        if (poDetails.getProductId() != null) {
            po.setProductId(poDetails.getProductId());
        }
        if (poDetails.getOrderPlannedDate() != null) {
            po.setOrderPlannedDate(poDetails.getOrderPlannedDate());
        }
        if (poDetails.getQuantity() != null) {
            po.setQuantity(poDetails.getQuantity());
        }
        if (poDetails.getTaxFlag() != null) {
            po.setTaxFlag(poDetails.getTaxFlag());
        }

        Product product = productRepository.findById(po.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません: id=" + po.getProductId()));
        po.calculatePaymentAmount(product.getPrice());
        po.setUpdateAt(LocalDateTime.now());

        return purchaseOrderRepository.save(po);
    }

    /**
     * 発注を削除
     * 未発注（status=0）のみ削除可能
     */
    public void deletePurchaseOrder(Integer id) {
        log.info("発注削除: id={}", id);

        PurchaseOrder po = purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("発注が見つかりません: id=" + id));

        if (po.getStatus() != null && po.getStatus() != 0) {
            throw new IllegalArgumentException("未発注の発注のみ削除できます");
        }

        purchaseOrderRepository.deleteById(id);
    }

    /**
     * 発注する：status→1、発注日をセット、発注先へメール送信
     * 未発注（status=0）のみ実行可能
     */
    public PurchaseOrder placeOrder(Integer id, LocalDate orderDate) {
        log.info("発注実行: id={}", id);

        PurchaseOrder po = purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("発注が見つかりません: id=" + id));

        if (po.getStatus() == null || po.getStatus() != 0) {
            throw new IllegalArgumentException("未発注の発注のみ発注できます");
        }

        if (orderDate == null) {
            throw new IllegalArgumentException("発注日を入力してください");
        }

        po.setStatus(1);
        po.setOrderDate(orderDate);
        po.setUpdateAt(LocalDateTime.now());
        PurchaseOrder saved = purchaseOrderRepository.save(po);

        Company company = companyRepository.findById(po.getCompanyId())
            .orElseThrow(() -> new IllegalArgumentException("企業が見つかりません: id=" + po.getCompanyId()));
        Product product = productRepository.findById(po.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません: id=" + po.getProductId()));

        mailService.sendPurchaseOrderNotification(
            company.getEmail(), company.getCompanyName(),
            product.getProductName(), po.getQuantity(), po.getPaymentAmount());

        log.info("発注完了: id={}, orderDate={}", id, po.getOrderDate());
        return saved;
    }
    /**
     * 発注解除：status→0、発注日をクリア
     * 発注済（status=1）のみ実行可能
     */
    public PurchaseOrder cancelOrder(Integer id) {
        log.info("発注解除: id={}", id);

        PurchaseOrder po = purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("発注が見つかりません: id=" + id));

        if (po.getStatus() == null || po.getStatus() != 1) {
            throw new IllegalArgumentException("発注済の発注のみ発注解除できます");
        }

        po.setStatus(0);
        po.setOrderDate(null);
        po.setUpdateAt(LocalDateTime.now());

        return purchaseOrderRepository.save(po);
    }

    /**
     * 仕入する：status→2、仕入日をセット、商品在庫を増加
     * 発注済（status=1）のみ実行可能
     */
    public PurchaseOrder purchaseIn(Integer id, LocalDate purchaseDate) {
        log.info("仕入実行: id={}", id);

        PurchaseOrder po = purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("発注が見つかりません: id=" + id));

        if (po.getStatus() == null || po.getStatus() != 1) {
            throw new IllegalArgumentException("発注済の発注のみ仕入できます");
        }

        if (purchaseDate == null) {
            throw new IllegalArgumentException("仕入日を入力してください");
        }

        Product product = productRepository.findById(po.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません: id=" + po.getProductId()));

        product.setStock((product.getStock() == null ? 0 : product.getStock()) + po.getQuantity());
        productRepository.save(product);

        po.setStatus(2);
        po.setPurchaseDate(purchaseDate);
        po.setUpdateAt(LocalDateTime.now());

        log.info("仕入完了: id={}, purchaseDate={}, 在庫増加後={}", id, po.getPurchaseDate(), product.getStock());
        return purchaseOrderRepository.save(po);
    }
}
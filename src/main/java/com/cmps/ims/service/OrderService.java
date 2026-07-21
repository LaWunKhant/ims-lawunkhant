package com.cmps.ims.service;

import com.cmps.ims.entity.Order;
import com.cmps.ims.entity.Product;
import com.cmps.ims.repository.OrderRepository;
import com.cmps.ims.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {
    	
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    
    /**
     * 受注を全件取得（ページング付き、降順）
     */
    public Page<Order> findAllOrders(Pageable pageable) {
        log.debug("受注全件取得: pageable={}", pageable);
        return orderRepository.findByStatusOrderByOrderDateDesc(0, pageable);
    }
    
    /**
     * 受注を検索（複合条件）
     */
    public Page<Order> searchOrders(Integer companyId, Integer productId, LocalDate orderDateFrom, 
                                     LocalDate orderDateTo, Pageable pageable) {
        log.debug("受注検索: companyId={}, productId={}, orderDateFrom={}, orderDateTo={}", 
                 companyId, productId, orderDateFrom, orderDateTo);
        return orderRepository.searchOrders(
            companyId, productId, orderDateFrom, orderDateTo, pageable);
    }
    
    /**
     * IDで受注を取得
     */
    @Transactional(readOnly = true)
    public Optional<Order> findOrderById(Integer id) {
        log.debug("受注取得: id={}", id);
        return orderRepository.findById(id);
    }
    
    /**
     * 企業IDで受注一覧を取得
     */
    @Transactional(readOnly = true)
    public List<Order> findOrdersByCompanyId(Integer companyId) {
        log.debug("企業の受注一覧: companyId={}", companyId);
        return orderRepository.findByCompanyId(companyId);
    }
    
    /**
     * 企業IDで充当可能な受注一覧を取得（status=0:受注のみ）
     */
    @Transactional(readOnly = true)
    public List<Order> findAvailableOrdersByCompanyId(Integer companyId) {
        log.debug("企業の充当可能な受注一覧: companyId={}", companyId);
        return orderRepository.findByCompanyIdAndStatus(companyId, 0);
    }
    
    /**
     * 商品IDで受注一覧を取得
     */
    @Transactional(readOnly = true)
    public List<Order> findOrdersByProductId(Integer productId) {
        log.debug("商品の受注一覧: productId={}", productId);
        return orderRepository.findByProductId(productId);
    }
    
    /**
     * 新規受注を作成
     * 単価と金額は自動計算される
     */
    public Order createOrder(Order order) {
        log.info("新規受注作成: companyId={}, productId={}, quantity={}", 
                order.getCompanyId(), order.getProductId(), order.getQuantity());
        
        // 商品から単価を取得
        if (order.getProductId() != null) {
            Optional<Product> product = productRepository.findById(order.getProductId());
            if (product.isPresent()) {
                order.setUnitPrice(product.get().getPrice());
            } else {
                throw new IllegalArgumentException("商品が見つかりません: productId=" + order.getProductId());
            }
        }
        
        // 金額と請求金額を計算
        order.recalculateAmounts();
        
        return orderRepository.save(order);
    }
    
    /**
     * 受注を更新
     */
    public Order updateOrder(Integer id, Order orderDetails) {
        log.info("受注更新: id={}", id);
        
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("受注が見つかりません: id=" + id));
        
        // 更新可能なフィールド
        if (orderDetails.getCompanyId() != null) {
            order.setCompanyId(orderDetails.getCompanyId());
        }
        if (orderDetails.getProductId() != null) {
            order.setProductId(orderDetails.getProductId());
            // 商品から単価を取得
            Optional<Product> product = productRepository.findById(orderDetails.getProductId());
            if (product.isPresent()) {
                order.setUnitPrice(product.get().getPrice());
            }
        }
        if (orderDetails.getOrderDate() != null) {
            order.setOrderDate(orderDetails.getOrderDate());
        }
        if (orderDetails.getQuantity() != null) {
            order.setQuantity(orderDetails.getQuantity());
        }
        if (orderDetails.getTaxFlag() != null) {
            order.setTaxFlag(orderDetails.getTaxFlag());
        }
        if (orderDetails.getPaymentDueDate() != null) {
            order.setPaymentDueDate(orderDetails.getPaymentDueDate());
        }
        
        // 金額を再計算
        order.recalculateAmounts();
        
        return orderRepository.save(order);
    }
    
    /**
     * 受注を削除
     */
    public void deleteOrder(Integer id) {
        log.info("受注削除: id={}", id);
        
        if (!orderRepository.existsById(id)) {
            throw new IllegalArgumentException("受注が見つかりません: id=" + id);
        }
        
        orderRepository.deleteById(id);
    }
    
    /**
     * 課税区分で検索
     */
    @Transactional(readOnly = true)
    public List<Order> findOrdersByTaxFlag(Integer taxFlag) {
        log.debug("課税区分で検索: taxFlag={}", taxFlag);
        return orderRepository.findByTaxFlag(taxFlag);
    }
    
    /**
     * 金額範囲で検索
     */
    @Transactional(readOnly = true)
    public List<Order> findOrdersByBillingAmountRange(Integer minAmount, Integer maxAmount) {
        log.debug("金額範囲で検索: minAmount={}, maxAmount={}", minAmount, maxAmount);
        return orderRepository.findByBillingAmountRange(minAmount, maxAmount);
    }
    
    /**
     * 企業の受注件数を集計
     */
    @Transactional(readOnly = true)
    public long countOrdersByCompanyId(Integer companyId) {
        List<Order> orders = orderRepository.findByCompanyId(companyId);
        return orders.size();
    }
    
    /**
     * 企業の受注合計金額を計算
     */
    @Transactional(readOnly = true)
    public Integer sumBillingAmountByCompanyId(Integer companyId) {
        List<Order> orders = orderRepository.findByCompanyId(companyId);
        return orders.stream()
            .map(Order::getBillingAmount)
            .reduce(0, Integer::sum);
    }
}
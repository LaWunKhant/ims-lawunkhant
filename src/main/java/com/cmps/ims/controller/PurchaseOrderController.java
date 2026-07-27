package com.cmps.ims.controller;

import com.cmps.ims.entity.PurchaseOrder;
import com.cmps.ims.service.CompanyService;
import com.cmps.ims.service.ProductService;
import com.cmps.ims.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/place")
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final CompanyService companyService;
    private final ProductService productService;

    /**
     * 発注一覧ページ（検索付き）
     * GET /place → place/index.html
     */
    @GetMapping
    public String index(
            @RequestParam(value = "productId", required = false) Integer productId,
            @RequestParam(value = "companyId", required = false) Integer companyId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "orderPlannedDateFrom", required = false) LocalDate orderPlannedDateFrom,
            @RequestParam(value = "orderPlannedDateTo", required = false) LocalDate orderPlannedDateTo,
            @PageableDefault(size = 20, page = 0, sort = "orderPlannedDate", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        log.debug("発注一覧表示");

        Page<PurchaseOrder> orders;
        if (productId != null || companyId != null || status != null || orderPlannedDateFrom != null || orderPlannedDateTo != null) {
            orders = purchaseOrderService.searchPurchaseOrders(productId, companyId, status, orderPlannedDateFrom, orderPlannedDateTo, pageable);
        } else {
            orders = purchaseOrderService.findAllPurchaseOrders(pageable);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("companies", companyService.findAll());
        model.addAttribute("products", productService.findAll());
        model.addAttribute("productId", productId);
        model.addAttribute("companyId", companyId);
        model.addAttribute("status", status);
        model.addAttribute("orderPlannedDateFrom", orderPlannedDateFrom);
        model.addAttribute("orderPlannedDateTo", orderPlannedDateTo);

        return "place/index";
    }

    /**
     * 発注新規登録フォーム
     * GET /place/entry → place/entry.html
     */
    @GetMapping("/entry")
    public String entryNew(Model model) {
        log.debug("発注新規登録フォーム表示");
        model.addAttribute("purchaseOrder", new PurchaseOrder());
        model.addAttribute("companies", companyService.findAll());
        model.addAttribute("products", productService.findAll());
        return "place/entry";
    }

    /**
     * 発注編集フォーム
     * GET /place/entry/{id} → place/entry.html
     */
    @GetMapping("/entry/{id}")
    public String entryEdit(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        log.debug("発注編集フォーム表示: id={}", id);

        Optional<PurchaseOrder> po = purchaseOrderService.findById(id);
        if (po.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "発注が見つかりません");
            return "redirect:/place";
        }

        model.addAttribute("purchaseOrder", po.get());
        model.addAttribute("companies", companyService.findAll());
        model.addAttribute("products", productService.findAll());
        return "place/entry";
    }

    /**
     * 発注を保存（新規登録のみ活性化）
     * POST /place/entry
     */
    @PostMapping("/entry")
    public String entrySave(@Valid @ModelAttribute("purchaseOrder") PurchaseOrder po,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        log.debug("発注保存処理: id={}, companyId={}, productId={}", po.getId(), po.getCompanyId(), po.getProductId());

        if (bindingResult.hasErrors()) {
            log.warn("バリデーションエラー: {}", bindingResult.getAllErrors());
            model.addAttribute("companies", companyService.findAll());
            model.addAttribute("products", productService.findAll());
            return "place/entry";
        }

        try {
            if (po.getId() == null) {
                purchaseOrderService.createPurchaseOrder(po);
                log.info("発注新規作成完了: id={}", po.getId());
                redirectAttributes.addFlashAttribute("message", "発注を登録しました");
            } else {
                purchaseOrderService.updatePurchaseOrder(po.getId(), po);
                log.info("発注更新完了: id={}", po.getId());
                redirectAttributes.addFlashAttribute("message", "発注を更新しました");
            }
            return "redirect:/place";
        } catch (IllegalArgumentException e) {
            log.warn("発注保存エラー: {}", e.getMessage());
            bindingResult.reject("error.purchaseOrder.save", e.getMessage());
            model.addAttribute("companies", companyService.findAll());
            model.addAttribute("products", productService.findAll());
            return "place/entry";
        }
    }

    /**
     * 発注を削除（未発注のみ活性化）
     * POST /place/delete/{id}
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        log.debug("発注削除実行: id={}", id);

        try {
            purchaseOrderService.deletePurchaseOrder(id);
            log.info("発注削除完了: id={}", id);
            redirectAttributes.addFlashAttribute("message", "発注を削除しました");
        } catch (IllegalArgumentException e) {
            log.warn("発注削除エラー: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/place/entry/" + id;
    }

    /**
     * 発注する（未発注のみ活性化）
     * POST /place/order/{id}
     */
    @PostMapping("/order/{id}")
    public String placeOrder(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        log.debug("発注実行: id={}", id);

        try {
            purchaseOrderService.placeOrder(id);
            log.info("発注完了: id={}", id);
            redirectAttributes.addFlashAttribute("message", "発注しました");
        } catch (IllegalArgumentException e) {
            log.warn("発注エラー: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/place/entry/" + id;
    }

    /**
     * 発注解除（発注済のみ活性化）
     * POST /place/cancel/{id}
     */
    @PostMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        log.debug("発注解除実行: id={}", id);

        try {
            purchaseOrderService.cancelOrder(id);
            log.info("発注解除完了: id={}", id);
            redirectAttributes.addFlashAttribute("message", "発注を解除しました");
        } catch (IllegalArgumentException e) {
            log.warn("発注解除エラー: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/place/entry/" + id;
    }

    /**
     * 仕入する（発注済のみ活性化）
     * POST /place/purchase/{id}
     */
    @PostMapping("/purchase/{id}")
    public String purchaseIn(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        log.debug("仕入実行: id={}", id);

        try {
            purchaseOrderService.purchaseIn(id);
            log.info("仕入完了: id={}", id);
            redirectAttributes.addFlashAttribute("message", "仕入しました");
        } catch (IllegalArgumentException e) {
            log.warn("仕入エラー: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/place/entry/" + id;
    }
}
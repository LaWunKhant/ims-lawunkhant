package com.cmps.ims.controller;

import com.cmps.ims.entity.Order;
import com.cmps.ims.service.CompanyService;
import com.cmps.ims.service.OrderService;
import com.cmps.ims.service.ProductService;
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
@RequestMapping("/receive")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    
    private final OrderService orderService;
    private final CompanyService companyService;
    private final ProductService productService;
    
    /**
     * 受注一覧ページ（検索付き）
     * GET /receive → receive/index.html
     */
    @GetMapping
    public String index(
            @RequestParam(value = "companyId", required = false) Integer companyId,
            @RequestParam(value = "productId", required = false) Integer productId,
            @RequestParam(value = "orderDateFrom", required = false) LocalDate orderDateFrom,
            @RequestParam(value = "orderDateTo", required = false) LocalDate orderDateTo,
            @PageableDefault(size = 20, page = 0, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        
        log.debug("受注一覧表示");
        
        Page<Order> orders;
        if (companyId != null || productId != null || orderDateFrom != null || orderDateTo != null) {
            orders = orderService.searchOrders(companyId, productId, orderDateFrom, orderDateTo, pageable);
            log.debug("検索実行: companyId={}, productId={}, from={}, to={}", companyId, productId, orderDateFrom, orderDateTo);
        } else {
            orders = orderService.findAllOrders(pageable);
            log.debug("全件表示");
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("companies", companyService.findAll());
        model.addAttribute("products", productService.findAll());
        model.addAttribute("companyId", companyId);
        model.addAttribute("productId", productId);
        model.addAttribute("orderDateFrom", orderDateFrom);
        model.addAttribute("orderDateTo", orderDateTo);
        
        return "receive/index";
    }
    
    /**
     * 受注登録フォーム（新規）
     * GET /receive/entry → receive/entry.html
     */
    @GetMapping("/entry")
    public String entryNew(Model model) {
        log.debug("受注新規登録フォーム表示");
        
        model.addAttribute("order", new Order());
        model.addAttribute("companies", companyService.findAll());
        model.addAttribute("products", productService.findAll());
        
        return "receive/entry";
    }
    
    /**
     * 受注編集フォーム（既存）
     * GET /receive/entry/{id} → receive/entry.html
     */
    @GetMapping("/entry/{id}")
    public String entryEdit(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        log.debug("受注編集フォーム表示: id={}", id);
        
        Optional<Order> order = orderService.findOrderById(id);
        if (order.isEmpty()) {
            log.warn("受注が見つかりません: id={}", id);
            redirectAttributes.addFlashAttribute("error", "受注が見つかりません");
            return "redirect:/receive";
        }
        
        model.addAttribute("order", order.get());
        model.addAttribute("companies", companyService.findAll());
        model.addAttribute("products", productService.findAll());
        
        return "receive/entry";
    }
    
    /**
     * 受注を保存（新規登録 or 更新）
     * POST /receive/entry
     */
    @PostMapping("/entry")
    public String entrySave(@Valid @ModelAttribute Order order, 
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        
        log.debug("受注保存処理: id={}, companyId={}, productId={}", 
                 order.getId(), order.getCompanyId(), order.getProductId());
        
        if (bindingResult.hasErrors()) {
            log.warn("バリデーションエラー: {}", bindingResult.getAllErrors());
            model.addAttribute("companies", companyService.findAll());
            model.addAttribute("products", productService.findAll());
            return "receive/entry";
        }
        
        try {
            Order savedOrder;
            if (order.getId() == null) {
                // 新規作成
                savedOrder = orderService.createOrder(order);
                log.info("受注新規作成完了: id={}", savedOrder.getId());
                redirectAttributes.addFlashAttribute("success", "受注を登録しました");
            } else {
                // 更新
                savedOrder = orderService.updateOrder(order.getId(), order);
                log.info("受注更新完了: id={}", savedOrder.getId());
                redirectAttributes.addFlashAttribute("success", "受注を更新しました");
            }
            return "redirect:/receive";
        } catch (Exception e) {
            log.error("受注保存エラー", e);
            bindingResult.reject("error.order.save", e.getMessage());
            model.addAttribute("companies", companyService.findAll());
            model.addAttribute("products", productService.findAll());
            return "receive/entry";
        }
    }
    
    /**
     * 受注を削除
     * POST /receive/delete/{id}
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        log.debug("受注削除実行: id={}", id);
        
        try {
            orderService.deleteOrder(id);
            log.info("受注削除完了: id={}", id);
            redirectAttributes.addFlashAttribute("success", "受注を削除しました");
        } catch (Exception e) {
            log.error("受注削除エラー", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/receive";
    }
}
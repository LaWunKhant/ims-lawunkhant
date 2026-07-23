package com.cmps.ims.controller;

import com.cmps.ims.entity.Order;
import com.cmps.ims.service.CompanyService;
import com.cmps.ims.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/send")
@RequiredArgsConstructor
@Slf4j
public class ShippingController {

    private final OrderService orderService;
    private final CompanyService companyService;

    /**
     * 発送一覧ページ（検索付き）
     * GET /send → send/index.html
     */
    @GetMapping
    public String index(
            @RequestParam(value = "companyId", required = false) Integer companyId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "orderDateFrom", required = false) LocalDate orderDateFrom,
            @RequestParam(value = "orderDateTo", required = false) LocalDate orderDateTo,
            @PageableDefault(size = 20, page = 0, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        log.debug("発送一覧表示");

        Page<Order> orders = orderService.searchShippableOrders(companyId, status, orderDateFrom, orderDateTo, pageable);

        model.addAttribute("orders", orders);
        model.addAttribute("companies", companyService.findAll());
        model.addAttribute("companyId", companyId);
        model.addAttribute("status", status);
        model.addAttribute("orderDateFrom", orderDateFrom);
        model.addAttribute("orderDateTo", orderDateTo);

        return "send/index";
    }

    /**
     * 発送変更画面
     * GET /send/entry/{id} → send/entry.html
     */
    @GetMapping("/entry/{id}")
    public String entry(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        log.debug("発送変更画面表示: id={}", id);

        Optional<Order> order = orderService.findOrderById(id);
        if (order.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "受注が見つかりません");
            return "redirect:/send";
        }

        model.addAttribute("order", order.get());
        return "send/entry";
    }

    /**
     * 発送処理
     * POST /send/ship/{id}
     */
    @PostMapping("/ship/{id}")
    public String ship(@PathVariable Integer id,
            @RequestParam(value = "sendDate", required = false) String sendDate,
            RedirectAttributes redirectAttributes) {

        LocalDate parsedDate = null;
        if (sendDate != null && !sendDate.isEmpty()) {
            parsedDate = LocalDate.parse(sendDate);
        }

        try {
            orderService.shipOrder(id, parsedDate);
            log.info("発送処理完了: orderId={}", id);
            redirectAttributes.addFlashAttribute("message", "発送処理が完了しました");
        } catch (IllegalArgumentException e) {
            log.warn("発送処理に失敗: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/send/entry/" + id;
    }

    /**
     * 発送解除
     * POST /send/unship/{id}
     */
    @PostMapping("/unship/{id}")
    public String unship(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            orderService.unshipOrder(id);
            log.info("発送解除完了: orderId={}", id);
            redirectAttributes.addFlashAttribute("message", "発送を解除しました");
        } catch (IllegalArgumentException e) {
            log.warn("発送解除に失敗: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/send/entry/" + id;
    }
}
package com.cmps.ims.controller;

import com.cmps.ims.entity.Payment;
import com.cmps.ims.service.CompanyService;
import com.cmps.ims.service.OrderService;
import com.cmps.ims.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private CompanyService companyService;

	@Autowired
	private OrderService orderService;

	/**
	 * List and search payments
	 */
	@GetMapping
	public String index(@RequestParam(value = "companyId", required = false) Integer companyId,
			@RequestParam(value = "paymentDateFrom", required = false) String paymentDateFrom,
			@RequestParam(value = "paymentDateTo", required = false) String paymentDateTo,
			@RequestParam(value = "paymentType", required = false) Integer paymentType,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "page", defaultValue = "0") int page, Model model) {

		log.debug("入金一覧を表示");

		Pageable pageable = PageRequest.of(page, 10);

		LocalDate fromDate = null;
		LocalDate toDate = null;

		if (paymentDateFrom != null && !paymentDateFrom.isEmpty()) {
			fromDate = LocalDate.parse(paymentDateFrom);
		}
		if (paymentDateTo != null && !paymentDateTo.isEmpty()) {
			toDate = LocalDate.parse(paymentDateTo);
		}

		Page<Payment> payments = paymentService.searchPayments(companyId, fromDate, toDate, paymentType, pageable);

		model.addAttribute("payments", payments);
		model.addAttribute("companies", companyService.findAll());
		model.addAttribute("companyId", companyId);
		model.addAttribute("paymentDateFrom", paymentDateFrom);
		model.addAttribute("paymentDateTo", paymentDateTo);
		model.addAttribute("paymentType", paymentType);
		model.addAttribute("status", status);

		return "payment/index";
	}

	/**
	 * Show new payment form
	 */
	@GetMapping("/entry")
	public String entryNew(Model model) {
		log.debug("入金新規登録フォーム表示");

		Payment payment = new Payment();
		model.addAttribute("payment", payment);
		model.addAttribute("companies", companyService.findAll());

		return "payment/entry";
	}

	/**
	 * Show edit/allocation payment page (伝票リスト付き)
	 * GET /payment/edit/{id}
	 */
	@GetMapping("/edit/{id}")
	public String edit(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
		log.debug("入金変更フォーム表示: id={}", id);

		Optional<Payment> payment = paymentService.findPaymentById(id);
		if (payment.isEmpty()) {
			log.warn("入金が見つかりません: id={}", id);
			redirectAttributes.addFlashAttribute("error", "入金が見つかりません");
			return "redirect:/payment";
		}

		Payment p = payment.get();
		List<com.cmps.ims.entity.Order> orders = orderService.findOrdersByCompanyId(p.getCompanyId());

		model.addAttribute("payment", p);
		model.addAttribute("orders", orders);

		return "payment/edit";
	}

	/**
	 * Save new payment (entry.html only handles NEW registration)
	 */
	@PostMapping("/entry")
	public String entrySave(@Valid @ModelAttribute("payment") Payment payment, BindingResult result, Model model,
			RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			log.warn("バリデーションエラー: {}", result.getAllErrors());
			model.addAttribute("companies", companyService.findAll());
			return "payment/entry";
		}

		try {
			paymentService.createPayment(payment);
			log.info("入金新規作成完了: id={}", payment.getId());
			redirectAttributes.addFlashAttribute("message", "入金を作成しました");
		} catch (Exception e) {
			log.error("入金の保存に失敗", e);
			model.addAttribute("companies", companyService.findAll());
			model.addAttribute("error", "入金の保存に失敗しました: " + e.getMessage());
			return "payment/entry";
		}

		return "redirect:/payment";
	}

	/**
	 * Delete payment
	 */
	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
		log.debug("入金削除: id={}", id);

		try {
			paymentService.deletePayment(id);
			log.info("入金削除完了: id={}", id);
			redirectAttributes.addFlashAttribute("message", "入金を削除しました");
		} catch (Exception e) {
			log.error("入金の削除に失敗", e);
			redirectAttributes.addFlashAttribute("error", "入金の削除に失敗しました");
		}

		return "redirect:/payment";
	}

	/**
	 * Allocate payment to order (充当)
	 */
	@PostMapping("/allocate/{id}")
	public String allocate(@PathVariable Integer id, @RequestParam("orderId") Integer orderId,
			RedirectAttributes redirectAttributes) {

		log.debug("入金を充当: paymentId={}, orderId={}", id, orderId);

		try {
			paymentService.allocatePayment(id, orderId, LocalDate.now());
			log.info("入金充当完了: paymentId={}", id);
			redirectAttributes.addFlashAttribute("message", "入金を充当しました");
		} catch (Exception e) {
			log.error("入金の充当に失敗", e);
			redirectAttributes.addFlashAttribute("error", "入金の充当に失敗しました");
		}

		return "redirect:/payment/edit/" + id;
	}

	/**
	 * Deallocate payment (充当解除)
	 */
	@PostMapping("/deallocate/{id}")
	public String deallocate(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
		log.debug("入金の充当を解除: paymentId={}", id);

		try {
			paymentService.deallocatePayment(id);
			log.info("入金充当解除完了: paymentId={}", id);
			redirectAttributes.addFlashAttribute("message", "入金の充当を解除しました");
		} catch (Exception e) {
			log.error("入金の充当解除に失敗", e);
			redirectAttributes.addFlashAttribute("error", "入金の充当解除に失敗しました");
		}

		return "redirect:/payment/edit/" + id;
	}
}
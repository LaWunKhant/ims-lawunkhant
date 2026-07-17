package com.cmps.ims.controller;

import com.cmps.ims.entity.Payment;
import com.cmps.ims.entity.Company;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

		// Normalize empty string params to null (HTML forms submit "" for unselected dropdowns)
		if (status != null && status.trim().isEmpty()) {
			status = null;
		}

		Page<Payment> payments = paymentService.searchPayments(companyId, fromDate, toDate, paymentType, status, pageable);

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
		List<com.cmps.ims.entity.Order> orders;

		if (p.getOrderId() != null) {
			// 充当済: 現在充当されている受注のみ表示
			orders = orderService.findOrderById(p.getOrderId())
					.map(List::of)
					.orElse(List.of());
		} else {
			// 未充当: まだどの入金にも紐付いていない受注（status=0）のみ表示
			orders = orderService.findAvailableOrdersByCompanyId(p.getCompanyId());
		}

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
	public String allocate(@PathVariable Integer id, @RequestParam(value = "orderId", required = false) Integer orderId,
			@RequestParam(value = "shippingDate", required = false) String shippingDate,
			RedirectAttributes redirectAttributes) {

		log.debug("入金を充当: paymentId={}, orderId={}, shippingDate={}", id, orderId, shippingDate);

		if (orderId == null) {
			redirectAttributes.addFlashAttribute("error", "充当する伝票を選択してください");
			return "redirect:/payment/edit/" + id;
		}

		LocalDate shipDate = null;
		if (shippingDate != null && !shippingDate.isEmpty()) {
			shipDate = LocalDate.parse(shippingDate);
		}

		try {
			paymentService.allocatePayment(id, orderId, LocalDate.now(), shipDate);
			log.info("入金充当完了: paymentId={}", id);
			redirectAttributes.addFlashAttribute("message", "入金を充当しました");
		} catch (IllegalArgumentException e) {
			log.warn("入金の充当に失敗: {}", e.getMessage());
			redirectAttributes.addFlashAttribute("error", e.getMessage());
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
		} catch (IllegalArgumentException e) {
			log.warn("入金の充当解除に失敗: {}", e.getMessage());
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		} catch (Exception e) {
			log.error("入金の充当解除に失敗", e);
			redirectAttributes.addFlashAttribute("error", "入金の充当解除に失敗しました");
		}

		return "redirect:/payment/edit/" + id;
	}

	/**
	 * CSV取込 (CSV Upload)
	 * Expected columns: 入金先(company name), 入金日(yyyy/MM/dd), 入金区分(銀行振込/カード決済), 入金額
	 */
	@PostMapping("/upload")
	public String upload(@RequestParam("upload") MultipartFile file, RedirectAttributes redirectAttributes) {
		log.debug("CSV取込開始: filename={}", file.getOriginalFilename());

		if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "ファイルを選択してください");
			return "redirect:/payment";
		}

		int successCount = 0;
		int errorCount = 0;
		StringBuilder errorDetails = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

			String line;
			int rowNum = 0;
			boolean firstRow = true;

			while ((line = reader.readLine()) != null) {
				rowNum++;
				if (firstRow) {
					// skip header row
					firstRow = false;
					continue;
				}
				if (line.trim().isEmpty()) {
					continue;
				}

				try {
					String[] cols = line.split(",");
					if (cols.length < 4) {
						throw new IllegalArgumentException("列数が不足しています");
					}

					String companyIdentifier = cols[0].trim();
					String dateStr = cols[1].trim();
					String typeStr = cols[2].trim();
					String amountStr = cols[3].trim();

					List<Company> companies = companyService.findAll();
					Company matched = companies.stream()
							.filter(c -> companyIdentifier.equals(c.getCompanyCode())
									|| companyIdentifier.equals(c.getCompanyName()))
							.findFirst()
							.orElseThrow(() -> new IllegalArgumentException("取引先が見つかりません: " + companyIdentifier));

					Payment payment = new Payment();
					payment.setCompanyId(matched.getId());
					payment.setPaymentDate(LocalDate.parse(dateStr.replace("/", "-")));

					if (typeStr.equals("銀行振込") || typeStr.equals("1")) {
						payment.setPaymentType(1);
					} else if (typeStr.equals("カード決済") || typeStr.equals("2")) {
						payment.setPaymentType(2);
					} else {
						throw new IllegalArgumentException("入金区分が不正です: " + typeStr);
					}

					payment.setPaymentAmount(Integer.parseInt(amountStr.replace(",", "")));

					paymentService.createPayment(payment);
					successCount++;
				} catch (Exception rowEx) {
					errorCount++;
					errorDetails.append(String.format("%d行目: %s ", rowNum, rowEx.getMessage()));
					log.warn("CSV行の取込に失敗: row={}, error={}", rowNum, rowEx.getMessage());
				}
			}

			if (errorCount == 0) {
				redirectAttributes.addFlashAttribute("message", successCount + "件の入金データを取り込みました");
			} else {
				redirectAttributes.addFlashAttribute("error",
						successCount + "件成功、" + errorCount + "件失敗: " + errorDetails);
			}

		} catch (Exception e) {
			log.error("CSV取込に失敗", e);
			redirectAttributes.addFlashAttribute("error", "CSV取込に失敗しました: " + e.getMessage());
		}

		return "redirect:/payment";
	}
}
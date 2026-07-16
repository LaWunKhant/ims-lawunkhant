package com.cmps.ims.service;

import com.cmps.ims.entity.Order;
import com.cmps.ims.entity.Payment;
import com.cmps.ims.repository.OrderRepository;
import com.cmps.ims.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    /**
     * Get all payments (paginated)
     */
    public Page<Payment> findAllPayments(Pageable pageable) {
        log.debug("入金一覧を取得");
        return paymentRepository.findAll(pageable);
    }
    
    /**
     * Search payments with multiple conditions
     */
    public Page<Payment> searchPayments(Integer companyId, LocalDate paymentDateFrom, 
                                       LocalDate paymentDateTo, Integer paymentType, 
                                       String status, Pageable pageable) {
        log.debug("入金検索: companyId={}, paymentDateFrom={}, paymentDateTo={}, paymentType={}, status={}", 
                 companyId, paymentDateFrom, paymentDateTo, paymentType, status);
        return paymentRepository.searchPayments(companyId, paymentDateFrom, paymentDateTo, paymentType, status, pageable);
    }
    
    /**
     * Find payment by ID
     */
    public Optional<Payment> findPaymentById(Integer id) {
        log.debug("入金を取得: id={}", id);
        return paymentRepository.findById(id);
    }
    
    /**
     * Find unallocated payments
     */
    public Page<Payment> findUnallocatedPayments(Pageable pageable) {
        log.debug("未充当の入金一覧を取得");
        return paymentRepository.findByOrderIdNull(pageable);
    }
    
    /**
     * Find allocated payments
     */
    public Page<Payment> findAllocatedPayments(Pageable pageable) {
        log.debug("充当済みの入金一覧を取得");
        return paymentRepository.findByOrderIdNotNull(pageable);
    }
    
    /**
     * Create new payment
     */
    public Payment createPayment(Payment payment) {
        log.debug("新規入金作成: companyId={}, paymentAmount={}, paymentType={}", 
                 payment.getCompanyId(), payment.getPaymentAmount(), payment.getPaymentType());
        
        // Validation
        if (payment.getPaymentAmount() <= 0) {
            throw new IllegalArgumentException("入金額は0より大きい値である必要があります");
        }
        
        // Manually set timestamps (safety net in case JPA Auditing isn't configured)
        if (payment.getCreatedAt() == null) {
            payment.setCreatedAt(LocalDateTime.now());
        }
        if (payment.getUpdateAt() == null) {
            payment.setUpdateAt(LocalDateTime.now());
        }
        
        Payment saved = paymentRepository.save(payment);
        log.info("新規入金作成完了: id={}", saved.getId());
        return saved;
    }
    
    /**
     * Update existing payment
     */
    public Payment updatePayment(Payment payment) {
        log.debug("入金更新: id={}, paymentAmount={}", payment.getId(), payment.getPaymentAmount());
        
        Optional<Payment> existing = paymentRepository.findById(payment.getId());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("入金が見つかりません: id=" + payment.getId());
        }
        
        // Preserve original createdAt, update updateAt
        payment.setCreatedAt(existing.get().getCreatedAt());
        payment.setUpdateAt(LocalDateTime.now());
        
        Payment updated = paymentRepository.save(payment);
        log.info("入金更新完了: id={}", updated.getId());
        return updated;
    }
    
    /**
     * Allocate payment to order (充当)
     */
    public Payment allocatePayment(Integer paymentId, Integer orderId, LocalDate allocationDate) {
        log.debug("入金を充当: paymentId={}, orderId={}", paymentId, orderId);
        
        Optional<Payment> payment = paymentRepository.findById(paymentId);
        if (payment.isEmpty()) {
            throw new IllegalArgumentException("入金が見つかりません: id=" + paymentId);
        }
        
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            throw new IllegalArgumentException("受注が見つかりません: id=" + orderId);
        }
        
        Payment p = payment.get();
        Order o = order.get();
        
        // Validation: payment amount must match the order's billing amount
        if (!p.getPaymentAmount().equals(o.getBillingAmount())) {
            throw new IllegalArgumentException(
                String.format("入金額（%,d円）と請求金額（%,d円）が一致しないため充当できません",
                    p.getPaymentAmount(), o.getBillingAmount()));
        }
        
        p.setOrderId(orderId);
        p.setAllocationDate(allocationDate != null ? allocationDate : LocalDate.now());
        
        Payment saved = paymentRepository.save(p);
        log.info("入金充当完了: paymentId={}, orderId={}", paymentId, orderId);
        return saved;
    }
    
    /**
     * Deallocate payment (充当解除)
     */
    public Payment deallocatePayment(Integer paymentId) {
        log.debug("入金の充当を解除: paymentId={}", paymentId);
        
        Optional<Payment> payment = paymentRepository.findById(paymentId);
        if (payment.isEmpty()) {
            throw new IllegalArgumentException("入金が見つかりません: id=" + paymentId);
        }
        
        Payment p = payment.get();
        p.setOrderId(null);
        p.setAllocationDate(null);
        
        Payment saved = paymentRepository.save(p);
        log.info("入金充当解除完了: paymentId={}", paymentId);
        return saved;
    }
    
    /**
     * Delete payment
     */
    public void deletePayment(Integer id) {
        log.debug("入金削除: id={}", id);
        
        Optional<Payment> payment = paymentRepository.findById(id);
        if (payment.isEmpty()) {
            throw new IllegalArgumentException("入金が見つかりません: id=" + id);
        }
        
        paymentRepository.deleteById(id);
        log.info("入金削除完了: id={}", id);
    }
    
    /**
     * Get payments by order ID
     */
    public List<Payment> findPaymentsByOrderId(Integer orderId) {
        log.debug("受注の入金一覧を取得: orderId={}", orderId);
        return paymentRepository.findByOrderId(orderId);
    }
}
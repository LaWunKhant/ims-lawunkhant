package com.cmps.ims.repository;

import com.cmps.ims.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    /**
     * Find all payments by company ID
     */
    Page<Payment> findByCompanyId(Integer companyId, Pageable pageable);
    
    /**
     * Find all payments within date range
     */
    Page<Payment> findByPaymentDateBetween(LocalDate fromDate, LocalDate toDate, Pageable pageable);
    
    /**
     * Find unallocated payments (orderId is NULL)
     */
    Page<Payment> findByOrderIdNull(Pageable pageable);
    
    /**
     * Find allocated payments (orderId is NOT NULL)
     */
    Page<Payment> findByOrderIdNotNull(Pageable pageable);
    
    /**
     * Complex search with multiple conditions (AND logic)
     * status: "unallocated" = orderId IS NULL, "allocated" = orderId IS NOT NULL, null = no filter
     */
    @Query("SELECT p FROM Payment p WHERE " +
           "(:companyId IS NULL OR p.companyId = :companyId) AND " +
           "(:paymentDateFrom IS NULL OR p.paymentDate >= :paymentDateFrom) AND " +
           "(:paymentDateTo IS NULL OR p.paymentDate <= :paymentDateTo) AND " +
           "(:paymentType IS NULL OR p.paymentType = :paymentType) AND " +
           "(:status IS NULL OR :status = '' OR " +
           "  (:status = 'unallocated' AND p.orderId IS NULL) OR " +
           "  (:status = 'allocated' AND p.orderId IS NOT NULL)) " +
           "ORDER BY p.paymentDate DESC, p.id DESC")
    Page<Payment> searchPayments(
            @Param("companyId") Integer companyId,
            @Param("paymentDateFrom") LocalDate paymentDateFrom,
            @Param("paymentDateTo") LocalDate paymentDateTo,
            @Param("paymentType") Integer paymentType,
            @Param("status") String status,
            Pageable pageable);
    
    /**
     * Find payments by order ID
     */
    List<Payment> findByOrderId(Integer orderId);
}
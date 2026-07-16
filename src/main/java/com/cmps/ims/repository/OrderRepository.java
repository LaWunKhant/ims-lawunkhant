package com.cmps.ims.repository;

import com.cmps.ims.entity.Order;
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
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    /**
     * 企業IDで受注を検索
     */
    List<Order> findByCompanyId(Integer companyId);
    
    /**
     * 商品IDで受注を検索
     */
    List<Order> findByProductId(Integer productId);
    
    /**
     * 受注日で検索
     */
    List<Order> findByOrderDate(LocalDate orderDate);
    
    /**
     * 受注日範囲で検索
     */
    List<Order> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 支払期日で検索
     */
    List<Order> findByPaymentDueDate(LocalDate paymentDueDate);
    
    /**
     * ページング付き：企業と受注日で複合検索
     */
    @Query("SELECT o FROM Order o WHERE " +
           "(:companyId IS NULL OR o.companyId = :companyId) AND " +
           "(:orderDateFrom IS NULL OR o.orderDate >= :orderDateFrom) AND " +
           "(:orderDateTo IS NULL OR o.orderDate <= :orderDateTo)")
    Page<Order> findByCompanyIdAndOrderDateRange(
        @Param("companyId") Integer companyId,
        @Param("orderDateFrom") LocalDate orderDateFrom,
        @Param("orderDateTo") LocalDate orderDateTo,
        Pageable pageable
    );
    
    /**
     * ページング付き：全受注を取得（降順）
     */
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);
    
    /**
     * 課税区分で検索
     */
    List<Order> findByTaxFlag(Integer taxFlag);
    
    /**
     * 企業と課税区分で検索
     */
    List<Order> findByCompanyIdAndTaxFlag(Integer companyId, Integer taxFlag);
    
    /**
     * 金額範囲で検索
     */
    @Query("SELECT o FROM Order o WHERE o.billingAmount BETWEEN :minAmount AND :maxAmount")
    List<Order> findByBillingAmountRange(
        @Param("minAmount") Integer minAmount,
        @Param("maxAmount") Integer maxAmount
    );
}
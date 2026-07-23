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
     * 企業IDと状態で受注を検索（充当可能な受注のみ取得する用）
     */
    List<Order> findByCompanyIdAndStatus(Integer companyId, Integer status);
    
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
     * ページング付き：企業・商品・受注日で複合検索
     */
    @Query("SELECT o FROM Order o WHERE o.status = 0 AND " +
            "(:companyId IS NULL OR o.companyId = :companyId) AND " +
            "(:productId IS NULL OR o.productId = :productId) AND " +
            "(:orderDateFrom IS NULL OR o.orderDate >= :orderDateFrom) AND " +
            "(:orderDateTo IS NULL OR o.orderDate <= :orderDateTo)")
     Page<Order> searchOrders(
         @Param("companyId") Integer companyId,
         @Param("productId") Integer productId,
         @Param("orderDateFrom") LocalDate orderDateFrom,
         @Param("orderDateTo") LocalDate orderDateTo,
         Pageable pageable
     );
    
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
     * ページング付き：status指定で受注を取得（降順）— 受注一覧画面用（status=0のみ表示）
     */
    Page<Order> findByStatusOrderByOrderDateDesc(Integer status, Pageable pageable);
    
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
    
    /**
     * 発送画面用：状態(1:入金済 or 2:発送済)・取引先・受注日で複合検索
     */
    @Query("SELECT o FROM Order o WHERE o.status IN (1, 2) AND " +
           "(:companyId IS NULL OR o.companyId = :companyId) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:orderDateFrom IS NULL OR o.orderDate >= :orderDateFrom) AND " +
           "(:orderDateTo IS NULL OR o.orderDate <= :orderDateTo) " +
           "ORDER BY o.orderDate DESC")
    Page<Order> searchShippableOrders(
        @Param("companyId") Integer companyId,
        @Param("status") Integer status,
        @Param("orderDateFrom") LocalDate orderDateFrom,
        @Param("orderDateTo") LocalDate orderDateTo,
        Pageable pageable
    );
}
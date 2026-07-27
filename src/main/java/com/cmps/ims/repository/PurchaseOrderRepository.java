package com.cmps.ims.repository;

import com.cmps.ims.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Integer> {

    /**
     * ページング付き：商品・発注先・状態・発注予定日で複合検索（AND検索）
     */
    @Query("SELECT p FROM PurchaseOrder p WHERE " +
           "(:productId IS NULL OR p.productId = :productId) AND " +
           "(:companyId IS NULL OR p.companyId = :companyId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:orderPlannedDateFrom IS NULL OR p.orderPlannedDate >= :orderPlannedDateFrom) AND " +
           "(:orderPlannedDateTo IS NULL OR p.orderPlannedDate <= :orderPlannedDateTo) " +
           "ORDER BY p.orderPlannedDate DESC")
    Page<PurchaseOrder> searchPurchaseOrders(
        @Param("productId") Integer productId,
        @Param("companyId") Integer companyId,
        @Param("status") Integer status,
        @Param("orderPlannedDateFrom") LocalDate orderPlannedDateFrom,
        @Param("orderPlannedDateTo") LocalDate orderPlannedDateTo,
        Pageable pageable
    );

    /**
     * ページング付き：全発注を取得（降順）
     */
    Page<PurchaseOrder> findAllByOrderByOrderPlannedDateDesc(Pageable pageable);
}
package com.cmps.ims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "purchase_orders", indexes = {
    @Index(name = "idx_po_company_id", columnList = "company_id"),
    @Index(name = "idx_po_product_id", columnList = "product_id")
})
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "発注先を選択してください")
    @Column(name = "company_id", nullable = false)
    private Integer companyId;

    @NotNull(message = "商品を選択してください")
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @NotNull(message = "発注予定日を入力してください")
    @Column(name = "order_planned_date", nullable = false)
    private LocalDate orderPlannedDate;

    @NotNull(message = "発注数を入力してください")
    @Positive(message = "発注数は1以上で入力してください")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "課税区分を選択してください")
    @Min(value = 0, message = "課税区分は0または1です")
    @Max(value = 1, message = "課税区分は0または1です")
    @Column(name = "tax_flag", nullable = false)
    private Integer taxFlag = 0;

    @NotNull(message = "支払金額を計算してください")
    @PositiveOrZero(message = "支払金額は0以上である必要があります")
    @Column(name = "payment_amount", nullable = false)
    private Integer paymentAmount;
    
    @Column(name = "order_date")
    private LocalDate orderDate;

    /**
     * 発注状態: 0=未発注, 1=発注済, 2=仕入済
     */
    @Column(name = "status")
    private Integer status = 0;


    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "created_member_id")
    private Integer createdMemberId;

    @Column(name = "update_member_id")
    private Integer updateMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    /**
     * 支払金額を自動計算: 単価(商品マスタ) × 数量 + 税
     * unitPrice は保存しない（画面表示のみ、DBにはpayment_amountのみ保存）
     */
    public void calculatePaymentAmount(Integer unitPrice) {
        if (unitPrice != null && this.quantity != null) {
            int amount = unitPrice * this.quantity;
            if (this.taxFlag != null && this.taxFlag == 1) {
                this.paymentAmount = (int) Math.round(amount * 1.10);
            } else {
                this.paymentAmount = amount;
            }
        }
    }
}
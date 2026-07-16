package com.cmps.ims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_company_id", columnList = "company_id"),
    @Index(name = "idx_product_id", columnList = "product_id"),
    @Index(name = "idx_order_date", columnList = "order_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @NotNull(message = "企業を選択してください")
    @Column(name = "company_id", nullable = false)
    private Integer companyId;
    
    @NotNull(message = "商品を選択してください")
    @Column(name = "product_id", nullable = false)
    private Integer productId;
    
    @NotNull(message = "受注日を入力してください")
    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;
    
    @NotNull(message = "数量を入力してください")
    @Positive(message = "数量は1以上で入力してください")
    @Column(nullable = false)
    private Integer quantity;
    
    @NotNull(message = "単価を入力してください")
    @PositiveOrZero(message = "単価は0以上で入力してください")
    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;
    
    @NotNull(message = "金額を入力してください")
    @PositiveOrZero(message = "金額は0以上である必要があります")
    @Column(nullable = false)
    private Integer amount;
    
    @NotNull(message = "課税区分を選択してください")
    @Min(value = 0, message = "課税区分は0または1です")
    @Max(value = 1, message = "課税区分は0または1です")
    @Column(name = "tax_flag", nullable = false)
    private Integer taxFlag;
    
    @NotNull(message = "請求金額を計算してください")
    @PositiveOrZero(message = "請求金額は0以上である必要があります")
    @Column(name = "billing_amount", nullable = false)
    private Integer billingAmount;
    
    @Column(name = "payment_due_date")
    private LocalDate paymentDueDate;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "update_at")
    private LocalDateTime updateAt;
    
    @CreatedBy
    @Column(name = "created_member_id")
    private Integer createdMemberId;
    

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
    
    /**
     * 金額を自動計算: 単価 × 数量
     */
    public void calculateAmount() {
        if (this.unitPrice != null && this.quantity != null) {
            this.amount = this.unitPrice * this.quantity;
        }
    }
    
    /**
     * 請求金額を自動計算: 金額 + 税
     * taxFlag: 0=非課税, 1=課税(10%)
     */
    public void calculateBillingAmount() {
        if (this.amount != null) {
            if (this.taxFlag == 1) {
                this.billingAmount = (int) Math.round(this.amount * 1.10);
            } else {
                this.billingAmount = this.amount;
            }
        }
    }
    
    /**
     * 金額と請求金額を両方計算
     */
    public void recalculateAmounts() {
        calculateAmount();
        calculateBillingAmount();
    }
}
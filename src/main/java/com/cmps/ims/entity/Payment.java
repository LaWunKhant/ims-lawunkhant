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
@Table(name = "payments", indexes = {
    @Index(name = "idx_company_id", columnList = "company_id"),
    @Index(name = "idx_order_id", columnList = "order_id"),
    @Index(name = "idx_payment_date", columnList = "payment_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @NotNull(message = "企業を選択してください")
    @Column(name = "company_id", nullable = false)
    private Integer companyId;
    
    @NotNull(message = "入金日を入力してください")
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;
    
    @NotNull(message = "入金区分を選択してください")
    @Min(value = 1, message = "入金区分は1または2です")
    @Max(value = 2, message = "入金区分は1または2です")
    @Column(name = "payment_type", nullable = false)
    private Integer paymentType;  // 1:銀行振込, 2:カード決済
    
    @NotNull(message = "入金額を入力してください")
    @PositiveOrZero(message = "入金額は0以上である必要があります")
    @Column(name = "payment_amount", nullable = false)
    private Integer paymentAmount;
    
    @Column(name = "order_id")
    private Integer orderId;  // FK→orders (NULL可能 = 未充当)
    
    @Column(name = "allocation_date")
    private LocalDate allocationDate;  // 充当日 (NULL=未充当)
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "update_at")
    private LocalDateTime updateAt;
    
    @CreatedBy
    @Column(name = "created_member_id")
    private Integer createdMemberId;
    
    @LastModifiedBy
    @Column(name = "update_member_id")
    private Integer updateMemberId;
    
    // Relationships (lazy loaded)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;
    
    /**
     * Get payment type label
     */
    public String getPaymentTypeLabel() {
        if (paymentType == null) return "";
        return paymentType == 1 ? "銀行振込" : "カード決済";
    }
    
    /**
     * Get allocation status
     */
    public String getAllocationStatus() {
        if (allocationDate != null && orderId != null) {
            return "充当済";
        } else if (orderId != null) {
            return "充当済";
        } else {
            return "未充当";
        }
    }
}
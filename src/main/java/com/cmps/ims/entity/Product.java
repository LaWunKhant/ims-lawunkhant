package com.cmps.ims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_code", length = 4, nullable = false, unique = true)
    @NotBlank(message = "商品コードは必須です")
    @Size(max = 4, message = "商品コードは4文字以内で入力してください")
    private String productCode;

    @Column(name = "product_name", length = 100, nullable = false)
    @NotBlank(message = "商品名は必須です")
    @Size(max = 100, message = "商品名は100文字以内で入力してください")
    private String productName;

    @Column(name = "product_name_short", length = 50)
    @Size(max = 50, message = "商品名（略）は50文字以内で入力してください")
    private String productNameShort;

    @Column(name = "category")
    private Integer category;

    @Column(name = "stock")
    @Min(value = 0, message = "在庫数は0以上で入力してください")
    private Integer stock = 0;

    @Column(name = "price")
    @NotNull(message = "金額は必須です")
    @Min(value = 0, message = "金額は0以上で入力してください")
    private Integer price;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "created_member_id")
    private Integer createdMemberId;

    @Column(name = "update_member_id")
    private Integer updateMemberId;
}
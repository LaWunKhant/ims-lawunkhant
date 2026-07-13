package com.cmps.ims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

@Entity
@Data
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "company_code", length = 4, nullable = false, unique = true)
    @NotBlank(message = "企業コードは必須です")
    @Size(max = 4, message = "企業コードは4文字以内で入力してください")
    private String companyCode;

    @Column(name = "company_name", length = 100, nullable = false)
    @NotBlank(message = "企業名は必須です")
    @Size(max = 100, message = "企業名は100文字以内で入力してください")
    private String companyName;

    @Column(name = "tel", length = 15)
    @Pattern(regexp = "^[0-9\\-]*$", message = "電話番号は半角数字とハイフン(-)区切りの形式で入力してください")
    private String tel;

    @Column(name = "email", length = 100)
    @Email(message = "メールアドレスの形式で入力してください")
    private String email;

    @Column(name = "postal_code", length = 8)
    @Pattern(regexp = "^[0-9]{3}-[0-9]{4}$", message = "郵便番号は000-0000の形式で入力してください")
    private String postalCode;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "created_member_id")
    private Integer createdMemberId;

    @Column(name = "update_member_id")
    private Integer updateMemberId;
}
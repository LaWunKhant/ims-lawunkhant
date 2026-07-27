package com.cmps.ims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", length = 100, nullable = false, unique = true)
    @NotBlank(message = "ユーザーIDは必須です")
    @Size(max = 100, message = "ユーザーIDは100文字以内で入力してください")
    private String userId;

    @Column(name = "password", length = 255, nullable = false)
    @NotBlank(message = "パスワードは必須です")
    private String password;

    @Column(name = "name", length = 100, nullable = false)
    @NotBlank(message = "氏名は必須です")
    @Size(max = 100, message = "氏名は100文字以内で入力してください")
    private String name;

    @Column(name = "tel", length = 15)
    @Pattern(regexp = "^[0-9\\-]*$", message = "電話番号は半角数字とハイフン(-)区切りの形式で入力してください")
    private String tel;

    @Column(name = "email", length = 100, nullable = false)
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式で入力してください")
    private String email;

    /**
     * 権限: 0=一般, 1=管理者
     */
    @NotNull(message = "権限を選択してください")
    @Min(value = 0, message = "権限は0または1です")
    @Max(value = 1, message = "権限は0または1です")
    @Column(name = "role", nullable = false)
    private Integer role;

    /**
     * ユーザー状態: 1=使用可能, 0=使用不可
     */
    @NotNull(message = "状態を選択してください")
    @Column(name = "status", nullable = false)
    private Integer status = 1;

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

    /**
     * 権限ラベルを取得
     */
    public String getRoleLabel() {
        if (role == null) return "";
        return role == 1 ? "管理者" : "一般";
    }

    /**
     * 状態ラベルを取得
     */
    public String getStatusLabel() {
        if (status == null) return "";
        return status == 1 ? "使用可能" : "使用不可";
    }
}
package com.cmps.ims.repository;

import com.cmps.ims.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * ユーザーIDでユーザーを取得（ログイン認証用）
     */
    Optional<User> findByUserId(String userId);

    /**
     * ユーザーIDの重複チェック
     */
    boolean existsByUserId(String userId);

    /**
     * ページング付き：ユーザーID・電話番号・氏名・メールで複合検索（AND検索）
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:userId IS NULL OR :userId = '' OR u.userId LIKE %:userId%) AND " +
           "(:tel IS NULL OR :tel = '' OR u.tel LIKE %:tel%) AND " +
           "(:name IS NULL OR :name = '' OR u.name LIKE %:name%) AND " +
           "(:email IS NULL OR :email = '' OR u.email LIKE %:email%) " +
           "ORDER BY u.id DESC")
    Page<User> searchUsers(
        @Param("userId") String userId,
        @Param("tel") String tel,
        @Param("name") String name,
        @Param("email") String email,
        Pageable pageable
    );
}
package com.cmps.ims.service;

import com.cmps.ims.entity.User;
import com.cmps.ims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * ユーザー検索（複合条件）
     */
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String userId, String tel, String name, String email, Pageable pageable) {
        log.debug("ユーザー検索: userId={}, tel={}, name={}, email={}", userId, tel, name, email);
        return userRepository.searchUsers(userId, tel, name, email, pageable);
    }

    /**
     * IDでユーザーを取得
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Integer id) {
        log.debug("ユーザー取得: id={}", id);
        return userRepository.findById(id);
    }

    /**
     * ユーザーIDでユーザーを取得（ログイン認証用）
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUserId(String userId) {
        log.debug("ユーザーID取得: userId={}", userId);
        return userRepository.findByUserId(userId);
    }

    /**
     * 新規ユーザー作成（パスワードはハッシュ化して保存）
     */
    public User createUser(User user) {
        log.info("新規ユーザー作成: userId={}", user.getUserId());

        if (userRepository.existsByUserId(user.getUserId())) {
            throw new IllegalArgumentException("このユーザーIDはすでに使用されています");
        }

        // パスワードをハッシュ化
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    /**
     * ユーザーを更新
     * パスワードが入力された場合のみハッシュ化して更新（空欄の場合は既存パスワードを維持）
     */
    public User updateUser(Integer id, User userDetails) {
        log.info("ユーザー更新: id={}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません: id=" + id));

        if (userDetails.getName() != null) {
            user.setName(userDetails.getName());
        }
        if (userDetails.getTel() != null) {
            user.setTel(userDetails.getTel());
        }
        if (userDetails.getEmail() != null) {
            user.setEmail(userDetails.getEmail());
        }
        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }
        if (userDetails.getStatus() != null) {
            user.setStatus(userDetails.getStatus());
        }
        if (userDetails.getRemarks() != null) {
            user.setRemarks(userDetails.getRemarks());
        }
        // パスワードが空でない場合のみ更新（編集画面では任意入力とする想定）
        if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    /**
     * ユーザーを削除
     */
    public void deleteUser(Integer id) {
        log.info("ユーザー削除: id={}", id);

        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("ユーザーが見つかりません: id=" + id);
        }

        userRepository.deleteById(id);
    }
}
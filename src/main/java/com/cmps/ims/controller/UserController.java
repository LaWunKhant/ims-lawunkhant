package com.cmps.ims.controller;

import com.cmps.ims.entity.User;
import com.cmps.ims.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * ユーザー一覧ページ（検索付き）
     * GET /user → user/index.html
     */
    @GetMapping
    public String index(
            @RequestParam(value = "userid", required = false) String userId,
            @RequestParam(value = "tel", required = false) String tel,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "email", required = false) String email,
            @PageableDefault(size = 20, page = 0, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        log.debug("ユーザー一覧表示");

        Page<User> users = userService.searchUsers(userId, tel, name, email, pageable);

        model.addAttribute("users", users);
        model.addAttribute("userid", userId);
        model.addAttribute("tel", tel);
        model.addAttribute("name", name);
        model.addAttribute("email", email);

        return "user/index";
    }

    /**
     * ユーザー新規登録フォーム
     * GET /user/entry → user/entry.html
     */
    @GetMapping("/entry")
    public String entryNew(Model model) {
        log.debug("ユーザー新規登録フォーム表示");
        model.addAttribute("user", new User());
        return "user/entry";
    }

    /**
     * ユーザー編集フォーム
     * GET /user/entry/{id} → user/entry.html
     */
    @GetMapping("/entry/{id}")
    public String entryEdit(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        log.debug("ユーザー編集フォーム表示: id={}", id);

        Optional<User> user = userService.findById(id);
        if (user.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "ユーザーが見つかりません");
            return "redirect:/user";
        }

        model.addAttribute("user", user.get());
        return "user/entry";
    }

    /**
     * ユーザーを保存（新規登録 or 更新）
     * POST /user/entry
     */
    @PostMapping("/entry")
    public String entrySave(@Valid @ModelAttribute("user") User user,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        log.debug("ユーザー保存処理: id={}, userId={}", user.getId(), user.getUserId());

        if (bindingResult.hasErrors()) {
            log.warn("バリデーションエラー: {}", bindingResult.getAllErrors());
            return "user/entry";
        }

        try {
            if (user.getId() == null) {
                userService.createUser(user);
                log.info("ユーザー新規作成完了: userId={}", user.getUserId());
                redirectAttributes.addFlashAttribute("message", "ユーザーを登録しました");
            } else {
                userService.updateUser(user.getId(), user);
                log.info("ユーザー更新完了: id={}", user.getId());
                redirectAttributes.addFlashAttribute("message", "ユーザーを更新しました");
            }
            return "redirect:/user";
        } catch (IllegalArgumentException e) {
            log.warn("ユーザー保存エラー: {}", e.getMessage());
            bindingResult.reject("error.user.save", e.getMessage());
            return "user/entry";
        }
    }

    /**
     * ユーザーを削除
     * POST /user/delete/{id}
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        log.debug("ユーザー削除実行: id={}", id);

        try {
            userService.deleteUser(id);
            log.info("ユーザー削除完了: id={}", id);
            redirectAttributes.addFlashAttribute("message", "ユーザーを削除しました");
        } catch (Exception e) {
            log.error("ユーザー削除エラー", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/user";
    }
}
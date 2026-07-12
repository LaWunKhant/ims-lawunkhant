package com.cmps.ims.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cmps.ims.entity.Product;
import com.cmps.ims.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    /**
     * 商品マスタ一覧表示（検索対応）
     */
    @GetMapping
    public String index(
            @RequestParam(required = false) String str_code,
            @RequestParam(required = false) String end_code,
            @RequestParam(required = false) String product,
            @RequestParam(required = false) Integer customer,
            Model model) {

        // validate code range order
        if (str_code != null && !str_code.isEmpty() 
            && end_code != null && !end_code.isEmpty()
            && str_code.compareTo(end_code) > 0) {
            
            model.addAttribute("errorMessage", "商品コードの範囲が正しくありません。開始コードは終了コード以下を入力してください。");
            model.addAttribute("products", List.of());
            return "product/index";
        }

        List<Product> products = productService.search(str_code, end_code, product, customer);
        model.addAttribute("products", products);
        return "product/index";
    }

    /**
     * 商品登録画面表示
     */
    @GetMapping("/entry")
    public String entry(Model model) {
        model.addAttribute("product", new Product());
        return "product/entry";
    }

    /**
     * 商品登録・更新処理
     */
    @PostMapping("/entry")
    public String save(@Valid @ModelAttribute Product product,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "product/entry";
        }
        
        try {
            boolean isNew = product.getId() == null;
            if (isNew) {
                product.setStock(0);
                product.setCreatedMemberId(1);
            }
            product.setUpdateMemberId(1);
            productService.save(product);

            if (isNew) {
                redirectAttributes.addFlashAttribute("successMessage", "商品を新規登録しました。");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "商品情報を更新しました。");
            }
            return "redirect:/product";

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // duplicate product_code
            model.addAttribute("errorMessage", "この商品コードはすでに登録されています。別のコードを入力してください。");
            return "product/entry";
        }
    }

    /**
     * 商品編集画面表示
     */
    @GetMapping("/entry/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        return "product/entry";
    }

    /**
     * 商品削除処理
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        productService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "商品を削除しました。");
        return "redirect:/product";
    }
}
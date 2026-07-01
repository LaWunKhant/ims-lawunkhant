package com.cmps.ims.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.cmps.ims.entity.Product;
import com.cmps.ims.service.ProductService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    /**
     * 商品マスタ一覧表示
     */
    @GetMapping
    public String index(Model model) {
        List<Product> products = productService.findAll();
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
     * 商品登録処理
     */
    @PostMapping("/entry")
    public String save(@ModelAttribute Product product) {
        if (product.getId() == null) {
            product.setStock(0);
        }
        productService.save(product);
        return "redirect:/product";
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
    public String delete(@PathVariable Integer id) {
        productService.deleteById(id);
        return "redirect:/product";
    }
}
package com.cmps.ims.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.cmps.ims.entity.Product;
import com.cmps.ims.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 全件取得
     */
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    /**
     * IDで取得
     */
    public Product findById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    /**
     * 登録・更新
     */
    public void save(Product product) {
        productRepository.save(product);
    }

    /**
     * 削除
     */
    public void deleteById(Integer id) {
        productRepository.deleteById(id);
    }
}
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
    
    public List<Product> search(String strCode, String endCode, String productName, Integer category) {
        List<Product> all = productRepository.findAll();
        return all.stream()
            .filter(p -> (strCode == null || strCode.isEmpty() || p.getProductCode().compareTo(strCode) >= 0))
            .filter(p -> (endCode == null || endCode.isEmpty() || p.getProductCode().compareTo(endCode) <= 0))
            .filter(p -> (productName == null || productName.isEmpty() || p.getProductName().contains(productName)))
            .filter(p -> (category == null || category.equals(p.getCategory())))
            .collect(java.util.stream.Collectors.toList());
    }
}
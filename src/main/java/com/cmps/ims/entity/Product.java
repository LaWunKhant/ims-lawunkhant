package com.cmps.ims.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_code", length = 20, nullable = false, unique = true)
    private String productCode;

    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;

    @Column(name = "product_name_short", length = 50)
    private String productNameShort;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "stock")
    private Integer stock = 0;

    @Column(name = "price")
    private Integer price;
}
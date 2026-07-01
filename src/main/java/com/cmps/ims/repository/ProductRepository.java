package com.cmps.ims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cmps.ims.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
}
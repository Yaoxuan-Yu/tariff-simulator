package com.example.tariff_simulator.repository;


import com.example.tariff_simulator.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByProduct(String product);
}

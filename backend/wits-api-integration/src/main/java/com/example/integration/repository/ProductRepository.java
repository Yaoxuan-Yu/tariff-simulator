package com.example.integration.repository;
import com.example.integration.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // Basic methods needed for scheduler
}


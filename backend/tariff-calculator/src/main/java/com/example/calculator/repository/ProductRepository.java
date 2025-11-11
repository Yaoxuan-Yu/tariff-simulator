package com.example.calculator.repository;
import com.example.calculator.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(value = "SELECT * FROM \"Products (Test)\" WHERE LOWER(\"product\") = LOWER(?1)", nativeQuery = true)
    List<Product> findByName(String name);
    
    @Query(value = "SELECT * FROM \"Products (Test)\" WHERE \"hs_code\" = ?1", nativeQuery = true)
    List<Product> findByHsCode(String hsCode);
    
    @Query(value = "SELECT DISTINCT \"hs_code\" FROM \"Products (Test)\" ORDER BY \"hs_code\"", nativeQuery = true)
    List<String> findDistinctHsCodes();
    
    @Query(value = "SELECT DISTINCT \"product\" FROM \"Products (Test)\" ORDER BY \"product\"", nativeQuery = true)
    List<String> findDistinctProducts();
}


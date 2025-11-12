package com.example.product.repository;
import com.example.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

// provides a layer of abstraction for the database operations for the product entity, and provides a clean interface for the service layer to use
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


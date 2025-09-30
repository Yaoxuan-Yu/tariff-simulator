package com.example.tariff.repository;
import com.example.tariff.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByName(String name);
    List<Product> findByHsCode(String hsCode);
    List<Product> findByHsCodeAndBrand(String hsCode, String brand);
    List<Product> findByNameAndBrand(String name, String brand);
    
    @Query("SELECT DISTINCT p.hsCode FROM Product p ORDER BY p.hsCode")
    List<String> findDistinctHsCodes();
    
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.hsCode = ?1 ORDER BY p.brand")
    List<String> findDistinctBrandsByHsCode(String hsCode);
    
    @Query("SELECT DISTINCT p.name FROM Product p ORDER BY p.name")
    List<String> findDistinctProducts();
    
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.name = ?1 ORDER BY p.brand")
    List<String> findDistinctBrandsByProduct(String product);
}
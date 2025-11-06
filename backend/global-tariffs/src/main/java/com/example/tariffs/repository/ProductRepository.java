package com.example.tariffs.repository;
import com.example.tariffs.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(value = "SELECT * FROM \"Products (Test)\" WHERE LOWER(\"product\") = LOWER(?1)", nativeQuery = true)
    List<Product> findByName(String name);
    
    @Query(value = "SELECT * FROM \"Products (Test)\" WHERE \"hs_code\" = ?1", nativeQuery = true)
    List<Product> findByHsCode(String hsCode);
    
    @Query(value = "SELECT * FROM \"Products (Test)\" WHERE \"hs_code\" = ?1 AND \"brand\" = ?2", nativeQuery = true)
    List<Product> findByHsCodeAndBrand(String hsCode, String brand);
    
    @Query(value = "SELECT * FROM \"Products (Test)\" WHERE LOWER(\"product\") = LOWER(?1) AND LOWER(\"brand\") = LOWER(?2)", nativeQuery = true)
    List<Product> findByNameAndBrand(String name, String brand);
    
    @Query(value = "SELECT DISTINCT \"hs_code\" FROM \"Products (Test)\" ORDER BY \"hs_code\"", nativeQuery = true)
    List<String> findDistinctHsCodes();
    
    @Query(value = "SELECT DISTINCT \"brand\" FROM \"Products (Test)\" WHERE \"hs_code\" = ?1 ORDER BY \"brand\"", nativeQuery = true)
    List<String> findDistinctBrandsByHsCode(String hsCode);
    
    @Query(value = "SELECT DISTINCT \"product\" FROM \"Products (Test)\" ORDER BY \"product\"", nativeQuery = true)
    List<String> findDistinctProducts();
    
    @Query(value = "SELECT DISTINCT \"brand\" FROM \"Products (Test)\" WHERE \"product\" = ?1 ORDER BY \"brand\"", nativeQuery = true)
    List<String> findDistinctBrandsByProduct(String product);
}


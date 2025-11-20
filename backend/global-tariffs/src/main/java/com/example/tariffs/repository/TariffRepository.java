package com.example.tariffs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tariffs.entity.Tariff;
import com.example.tariffs.entity.TariffId;

// jpa repository providing access to tariff rates table
public interface TariffRepository extends JpaRepository<Tariff, TariffId> {
    @Query(value = "SELECT * FROM \"Tariff Rates (Test)\" WHERE \"country\" = ?1 AND \"partner\" = ?2", nativeQuery = true)
    Optional<Tariff> findByCountryAndPartner(String country, String partner);
    
    @Query(value = "SELECT DISTINCT \"country\" FROM \"Tariff Rates (Test)\" ORDER BY \"country\"", nativeQuery = true)
    List<String> findDistinctCountries();
    
    @Query(value = "SELECT DISTINCT \"partner\" FROM \"Tariff Rates (Test)\" ORDER BY \"partner\"", nativeQuery = true)
    List<String> findDistinctPartners();

    @Query(value = "SELECT DISTINCT \"country\" FROM \"Tariff Rates (Test)\"", nativeQuery = true)
    List<String> findAllDistinctCountries();

    @Query(value = "SELECT DISTINCT \"partner\" FROM \"Tariff Rates (Test)\"", nativeQuery = true)
    List<String> findAllDistinctPartners();

    // Update tariff rows for a specific product (hs_code) and country/partner pair
    // This updates only rows matching country + partner + hs_code (for all years of that product)
    @Modifying
    @Query(value = "UPDATE \"Tariff Rates (Test)\" " +
                   "SET \"ahs_weighted\" = :ahsWeighted, \"mfn_weighted\" = :mfnWeighted " +
                   "WHERE \"country\" = :country AND \"partner\" = :partner AND \"hs_code\" = :hsCode", 
           nativeQuery = true)
    int updateTariffRatesByProduct(@Param("country") String country, 
                                    @Param("partner") String partner,
                                    @Param("hsCode") String hsCode,
                                    @Param("ahsWeighted") Double ahsWeighted, 
                                    @Param("mfnWeighted") Double mfnWeighted);
    
    // Check if any tariff exists for country/partner/hs_code combination
    @Query(value = "SELECT COUNT(*) > 0 FROM \"Tariff Rates (Test)\" WHERE \"country\" = ?1 AND \"partner\" = ?2 AND \"hs_code\" = ?3", nativeQuery = true)
    boolean existsByCountryPartnerAndHsCode(String country, String partner, String hsCode);
    
    // Find tariff by country, partner, and hs_code
    @Query(value = "SELECT * FROM \"Tariff Rates (Test)\" WHERE \"country\" = ?1 AND \"partner\" = ?2 AND \"hs_code\" = ?3 LIMIT 1", nativeQuery = true)
    Optional<Tariff> findByCountryPartnerAndHsCode(String country, String partner, String hsCode);
    
    // Delete all tariff rows for a specific product (hs_code) and country/partner pair
    // This deletes all rows matching country + partner + hs_code (for all years of that product)
    @Modifying
    @Query(value = "DELETE FROM \"Tariff Rates (Test)\" " +
                   "WHERE \"country\" = :country AND \"partner\" = :partner AND \"hs_code\" = :hsCode", 
           nativeQuery = true)
    int deleteTariffRatesByProduct(@Param("country") String country, 
                                    @Param("partner") String partner,
                                    @Param("hsCode") String hsCode);
    
    // Find all distinct country/partner combinations for a specific HS code
    // Returns one row per country/partner combination (deduplicated)
    @Query(value = "SELECT DISTINCT \"country\", \"partner\", \"ahs_weighted\", \"mfn_weighted\" " +
                   "FROM \"Tariff Rates (Test)\" " +
                   "WHERE \"hs_code\" = ?1", 
           nativeQuery = true)
    List<Object[]> findDistinctCountryPartnerByHsCode(String hsCode);
    
    // Insert a new tariff row with country, partner, hs_code, year, and rates
    @Modifying
    @Query(value = "INSERT INTO \"Tariff Rates (Test)\" (\"country\", \"partner\", \"hs_code\", \"year\", \"ahs_weighted\", \"mfn_weighted\") " +
                   "VALUES (:country, :partner, :hsCode, :year, :ahsWeighted, :mfnWeighted)", 
           nativeQuery = true)
    int insertTariffRate(@Param("country") String country, 
                         @Param("partner") String partner,
                         @Param("hsCode") String hsCode,
                         @Param("year") Integer year,
                         @Param("ahsWeighted") Double ahsWeighted, 
                         @Param("mfnWeighted") Double mfnWeighted);
}


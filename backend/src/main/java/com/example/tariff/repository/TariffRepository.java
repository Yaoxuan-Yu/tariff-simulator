package com.example.tariff.repository;

import com.example.tariff.entity.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

// // provides a layer of abstraction for the database operations for the tariff entity, and provides a clean interface for the service layer to use
public interface TariffRepository extends JpaRepository<Tariff, Long> {
    Optional<Tariff> findByCountryAndPartner(String country, String partner);
    
    @Query("SELECT DISTINCT t.country FROM Tariff t ORDER BY t.country")
    List<String> findDistinctCountries();
    
    @Query("SELECT DISTINCT t.partner FROM Tariff t ORDER BY t.partner")
    List<String> findDistinctPartners();
}

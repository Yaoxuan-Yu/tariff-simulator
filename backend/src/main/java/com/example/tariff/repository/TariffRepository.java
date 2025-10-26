package com.example.tariff.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.tariff.entity.Tariff;
import com.example.tariff.entity.TariffId;


// // provides a layer of abstraction for the database operations for the tariff entity, and provides a clean interface for the service layer to use
public interface TariffRepository extends JpaRepository<Tariff, TariffId> {
    Optional<Tariff> findByCountryAndPartner(String country, String partner);
    
    @Query("SELECT DISTINCT t.country FROM Tariff t ORDER BY t.country")
    List<String> findDistinctCountries();
    
    @Query("SELECT DISTINCT t.partner FROM Tariff t ORDER BY t.partner")
    List<String> findDistinctPartners();

    @Query("SELECT DISTINCT t.country FROM Tariff t")
    List<String> findAllDistinctCountries();

    @Query("SELECT DISTINCT t.partner FROM Tariff t")
    List<String> findAllDistinctPartners();




}



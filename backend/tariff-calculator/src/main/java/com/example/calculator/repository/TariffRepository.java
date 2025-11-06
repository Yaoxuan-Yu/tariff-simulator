package com.example.calculator.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.calculator.entity.Tariff;
import com.example.calculator.entity.TariffId;

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
}


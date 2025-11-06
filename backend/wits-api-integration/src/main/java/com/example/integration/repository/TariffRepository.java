package com.example.integration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.integration.entity.Tariff;
import com.example.integration.entity.TariffId;

public interface TariffRepository extends JpaRepository<Tariff, TariffId> {
    @Query(value = "SELECT DISTINCT \"country\" FROM \"Tariff Rates (Test)\"", nativeQuery = true)
    List<String> findAllDistinctCountries();

    @Query(value = "SELECT DISTINCT \"partner\" FROM \"Tariff Rates (Test)\"", nativeQuery = true)
    List<String> findAllDistinctPartners();
}


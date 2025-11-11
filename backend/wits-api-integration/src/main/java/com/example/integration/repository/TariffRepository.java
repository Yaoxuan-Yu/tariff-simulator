package com.example.integration.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.integration.entity.Tariff;
import com.example.integration.entity.TariffId;

public interface TariffRepository extends JpaRepository<Tariff, TariffId> {

    // Get all distinct reporter countries
    @Query(value = "SELECT DISTINCT \"country\" FROM \"Tariff Rates (Test)\"", nativeQuery = true)
    List<String> findAllDistinctCountries();

    // Get all distinct partner countries
    @Query(value = "SELECT DISTINCT \"partner\" FROM \"Tariff Rates (Test)\"", nativeQuery = true)
    List<String> findAllDistinctPartners();

    // Get all tariffs for a specific country and year
    @Query(value = "SELECT * FROM \"Tariff Rates (Test)\" WHERE \"country\" = :country AND \"year\" = :year", nativeQuery = true)
    List<Tariff> findByCountryAndYear(@Param("country") String country, @Param("year") Integer year);

    // Get all tariffs for a country-partner pair and year
    @Query(value = "SELECT * FROM \"Tariff Rates (Test)\" WHERE \"country\" = :country AND \"partner\" = :partner AND \"year\" = :year", nativeQuery = true)
    List<Tariff> findByCountryPartnerAndYear(@Param("country") String country, @Param("partner") String partner, @Param("year") Integer year);

    // Get all tariffs for a specific hsCode across all years
    @Query(value = "SELECT * FROM \"Tariff Rates (Test)\" WHERE \"hs_code\" = :hsCode", nativeQuery = true)
    List<Tariff> findByHsCode(@Param("hsCode") String hsCode);

    Optional<Tariff> findByCountryAndPartnerAndHsCode(String country, String partner, String hsCode);

    // Optional: find by country, partner, hsCode, and year (returns single tariff)
    @Query(value = "SELECT * FROM \"Tariff Rates (Test)\" WHERE \"country\" = :country AND \"partner\" = :partner AND \"hs_code\" = :hsCode AND \"year\" = :year", nativeQuery = true)
    Tariff findOneByCountryPartnerHsCodeYear(@Param("country") String country, @Param("partner") String partner, @Param("hsCode") String hsCode, @Param("year") Integer year);
}



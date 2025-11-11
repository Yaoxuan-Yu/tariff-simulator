package com.example.tariff.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.tariff.entity.Tariff;

// provides a layer of abstraction for the database operations for the tariff entity
public interface TariffRepository extends JpaRepository<Tariff, Long> {

    Optional<Tariff> findByCountryAndPartner(String country, String partner);

    // Find all tariffs by country
    List<Tariff> findByCountry(String country);

    @Query("SELECT DISTINCT t.country FROM Tariff t ORDER BY t.country")
    List<String> findDistinctCountries();

    @Query("SELECT DISTINCT t.partner FROM Tariff t ORDER BY t.partner")
    List<String> findDistinctPartners();
}

package com.example.tariff.repository;

import com.example.tariff.entity.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TariffRepository extends JpaRepository<Tariff, Long> {
    Optional<Tariff> findByCountryAndPartner(String country, String partner);
}

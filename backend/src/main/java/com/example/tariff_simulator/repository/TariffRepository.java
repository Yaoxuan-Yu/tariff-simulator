package com.example.tariff_simulator.repository;

import com.example.tariff_simulator.entity.TariffEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TariffRepository extends JpaRepository<Tariff, Long> {
    Optional<Tariff> findByCountryAndPartner(String country, String partner);
}
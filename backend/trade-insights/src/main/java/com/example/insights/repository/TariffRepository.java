package com.example.insights.repository;

import com.example.insights.entity.Tariff;
import com.example.insights.entity.TariffId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, TariffId> {
}


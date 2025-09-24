package com.example.tariff_simulator.service;

import java.io.PrintWriter;

import org.springframework.web.bind.annotation.GetMapping;

import com.example.tariff_simulator.dto.TariffResponse;

public class CsvExportService {
    private TariffResponse tariffResponse;

    public CsvExportService (TariffResponse tariffResponse) {
        this.tariffResponse = tariffResponse;
    }

    @GetMapping("/export")
    public void exportTariffAsCSV () {
        // Set response headers
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=tariff.csv");

        // Write CSV data
        PrintWriter writer = response.getWriter();
        writer.println("Country1,Country2,HsCode,TariffRate");

        writer.printf("%s,%s,%s,%s%n",
                tariffResponse.getCountry1(),
                tariffResponse.getCountry2(),
                tariffResponse.getHsCode(),
                tariffResponse.getTariffRate());

        writer.flush();
        writer.close();
    }
}
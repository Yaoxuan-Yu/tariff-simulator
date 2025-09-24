package com.example.tariff.service;

import java.io.PrintWriter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.tariff.dto.TariffResponse;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class CsvExportService {

    public void exportTariffAsCSV(List<TariffResponse> tariffResponses, HttpServletResponse response) {
        try {
            // Set response headers
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=tariff_calculations.csv");

            // Write CSV data
            PrintWriter writer = response.getWriter();
            
            // Write CSV header
            writer.println("Import Country,Export Country,HS Code,Brand,Product Cost,AHS Rate,AHS Tariff Amount,MFN Rate,MFN Tariff Amount");

            // Write data rows
            for (TariffResponse tariffResponse : tariffResponses) {
                writer.printf("%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                        tariffResponse.getImportCountry(),
                        tariffResponse.getExportCountry(),
                        tariffResponse.getHsCode(),
                        tariffResponse.getBrand(),
                        tariffResponse.getProductCost(),
                        tariffResponse.getAhsRate(),
                        tariffResponse.getAhsTariffAmount(),
                        tariffResponse.getMfnRate(),
                        tariffResponse.getMfnTariffAmount());
            }

            writer.flush();
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("Error exporting CSV", e);
        }
    }

    public void exportSingleTariffAsCSV(TariffResponse tariffResponse, HttpServletResponse response) {
        try {
            // Set response headers
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=tariff_calculation.csv");

            // Write CSV data
            PrintWriter writer = response.getWriter();
            
            // Write CSV header
            writer.println("Import Country,Export Country,HS Code,Brand,Product Cost,AHS Rate,AHS Tariff Amount,MFN Rate,MFN Tariff Amount");

            // Write data row
            writer.printf("%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                    tariffResponse.getImportCountry(),
                    tariffResponse.getExportCountry(),
                    tariffResponse.getHsCode(),
                    tariffResponse.getBrand(),
                    tariffResponse.getProductCost(),
                    tariffResponse.getAhsRate(),
                    tariffResponse.getAhsTariffAmount(),
                    tariffResponse.getMfnRate(),
                    tariffResponse.getMfnTariffAmount());

            writer.flush();
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("Error exporting CSV", e);
        }
    }
}
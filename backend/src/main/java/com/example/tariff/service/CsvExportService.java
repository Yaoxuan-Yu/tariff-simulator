package com.example.tariff.service;

import java.io.PrintWriter;

import org.springframework.stereotype.Service;

import com.example.tariff.dto.TariffResponse;
import com.example.tariff.exception.ExportException;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class CsvExportService {

    public void exportSingleTariffAsCSV(TariffResponse tariffResponse, HttpServletResponse response) {
        try {

            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=tariff_calculation.csv");


            PrintWriter writer = response.getWriter();
            

            writer.println("Import Country,Export Country,HS Code,Brand,Product Cost,AHS Rate,AHS Tariff Amount,MFN Rate,MFN Tariff Amount");


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
            throw new ExportException("Failed to export tariff data to CSV", e);
        }
    }
}
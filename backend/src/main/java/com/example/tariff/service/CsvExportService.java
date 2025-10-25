
package com.example.tariff.service;

import com.example.tariff.dto.TariffResponse;
import com.example.tariff.exception.ExportException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


// service files in general are in between the controller and the repository and contains the business logic in terms of implementing tariff calculations and validations etc

@Service
public class CsvExportService {
    // Export multiple tariff history calculations
    public void exportHistoryAsCSV(List<TariffResponse> history, HttpServletResponse response) {
        configureResponse(response, "tariff_history.csv");
        try (PrintWriter writer = response.getWriter()) {
            if (history == null || history.isEmpty()) {
                writer.println("Error: No tariff history available for export");
                return;
            }

            writeHeader(writer);
            for (TariffResponse responseItem : history) {
                if (responseItem != null && responseItem.isSuccess() && responseItem.getData() != null) {
                    writeTariffData(writer, responseItem.getData());
                }
            }

        } catch (IOException e) {
            throw new ExportException("Failed to export session tariff history to CSV", e);
        }
    }

    // Helper method: set headers for file download
    private void configureResponse(HttpServletResponse response, String fileName) {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
    }

    // Helper method: write CSV header row
    private void writeHeader(PrintWriter writer) {
        writer.println("Product,Brand,Exporting From,Importing To,Quantity,Unit,Product Cost,Tariff Rate,Tariff Amount,Total Cost,Tariff Type");
    }

    // Helper method: write data row for a tariff
    private void writeTariffData(PrintWriter writer, TariffResponse.TariffCalculationData data) {
        writer.printf("%s,%s,%s,%s,%.2f,%s,%.2f,%.2f%%,%.2f,%.2f,%s%n",
                nullSafe(data.getProduct()),
                nullSafe(data.getBrand()),
                nullSafe(data.getExportingFrom()),
                nullSafe(data.getImportingTo()),
                data.getQuantity(),
                nullSafe(data.getUnit()),
                data.getProductCost(),
                data.getTariffRate(),
                data.getTotalCost() - data.getProductCost(),
                data.getTotalCost(),
                nullSafe(data.getTariffType()));
    }

    // Prevents NullPointerException when writing
    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}

package com.example.tariff.service;

import com.example.tariff.dto.CalculationHistoryDto;
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

    // ===== EXPORT CART AS CSV =====
    // Export all items in export cart as CSV
    public void exportCartAsCSV(List<CalculationHistoryDto> cartItems, HttpServletResponse response) {
        configureResponse(response, "export_cart_" + System.currentTimeMillis() + ".csv");
        try (PrintWriter writer = response.getWriter()) {
            if (cartItems == null || cartItems.isEmpty()) {
                writer.println("Error: Export cart is empty");
                return;
            }
            writeCartHeader(writer);
            for (CalculationHistoryDto calc : cartItems) {
                writeCartData(writer, calc);
            }
        } catch (IOException e) {
            throw new ExportException("Failed to export cart to CSV", e);
        }
    }

    // ===== EXPORT SINGLE TARIFF AS CSV (LEGACY - KEPT FOR BACKWARD COMPATIBILITY) =====
    // Export a single tariff calculation as CSV
    public void exportSingleTariffAsCSV(TariffResponse tariffResponse, HttpServletResponse response) {
        configureResponse(response, "tariff_calculation.csv");
        try (PrintWriter writer = response.getWriter()) {
            if (!tariffResponse.isSuccess() || tariffResponse.getData() == null) {
                writer.println("Error: " + (tariffResponse.getError() != null ? tariffResponse.getError() : "No data available"));
                return;
            }

            TariffResponse.TariffCalculationData data = tariffResponse.getData();
            writeHeader(writer);
            writeTariffData(writer, data);
        } catch (IOException e) {
            throw new ExportException("Failed to export tariff data to CSV", e);
        }
    }

    // ===== EXPORT HISTORY AS CSV (LEGACY - KEPT FOR BACKWARD COMPATIBILITY) =====
    // Export multiple tariff history calculations
    public void exportAsCSV(List<TariffResponse> history, HttpServletResponse response) {
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

    // ===== HELPER METHODS =====

    // Helper method: set headers for file download
    private void configureResponse(HttpServletResponse response, String fileName) {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
    }

    // Helper method: write CSV header row for single tariff export
    private void writeHeader(PrintWriter writer) {
        writer.println("Product,Brand,Exporting From,Importing To,Quantity,Unit,Product Cost,Tariff Rate,Tariff Amount,Total Cost,Tariff Type");
    }

    // Helper method: write CSV header row for cart export
    private void writeCartHeader(PrintWriter writer) {
        writer.println("ID,Product,Brand,Exporting From,Importing To,Quantity,Unit,Product Cost,Tariff Rate,Tariff Amount,Total Cost,Tariff Type,Created At");
    }

    // Helper method: write data row for a tariff
    private void writeTariffData(PrintWriter writer, TariffResponse.TariffCalculationData data) {
        writer.printf("%s,%s,%s,%s,%.2f,%s,%.2f,%.2f%%,%.2f,%.2f,%s%n",
                escapeCSV(data.getProduct()),
                escapeCSV(data.getBrand()),
                escapeCSV(data.getExportingFrom()),
                escapeCSV(data.getImportingTo()),
                data.getQuantity(),
                escapeCSV(data.getUnit()),
                data.getProductCost(),
                data.getTariffRate(),
                data.getTotalCost() - data.getProductCost(),
                data.getTotalCost(),
                escapeCSV(data.getTariffType()));
    }

    // Helper method: write data row for cart item
    private void writeCartData(PrintWriter writer, CalculationHistoryDto calc) {
        writer.printf("%s,%s,%s,%s,%s,%.2f,%s,%.2f,%.2f%%,%.2f,%.2f,%s,%s%n",
                escapeCSV(calc.getId()),
                escapeCSV(calc.getProductName()),
                escapeCSV(calc.getBrand()),
                escapeCSV(calc.getExportingFrom()),
                escapeCSV(calc.getImportingTo()),
                calc.getQuantity(),
                escapeCSV(calc.getUnit()),
                calc.getProductCost(),
                calc.getTariffRate(),
                calc.getTariffAmount(),
                calc.getTotalCost(),
                escapeCSV(calc.getTariffType()),
                calc.getCreatedAt());
    }

    // Helper method: escape CSV values that contain special characters
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
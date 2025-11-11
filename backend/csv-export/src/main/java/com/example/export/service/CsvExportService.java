package com.example.export.service;

import com.example.session.dto.CalculationHistoryDto;
import com.example.export.exception.ExportException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

// handles csv rendering for export cart entries
@Service
public class CsvExportService {

    // write cart contents to response as csv
    public void exportToCsv(List<CalculationHistoryDto> cartItems, HttpServletResponse response) {
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

    // set response headers for csv download
    private void configureResponse(HttpServletResponse response, String fileName) {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
    }

    // write csv header row
    private void writeCartHeader(PrintWriter writer) {
        writer.println("ID,Product,Brand,Exporting From,Importing To,Quantity,Unit,Product Cost,Tariff Rate,Tariff Amount,Total Cost,Tariff Type,Source,Created At");
    }

    // write csv row for a single calculation
    private void writeCartData(PrintWriter writer, CalculationHistoryDto calc) {
        writer.printf("%s,%s,%s,%s,%s,%.2f,%s,%.2f,%.2f%%,%.2f,%.2f,%s,%s,%s%n",
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
                escapeCSV(calc.getSource() != null ? calc.getSource() : "global"),
                calc.getCreatedAt());
    }

    // escape csv values that contain commas/quotes/newlines
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


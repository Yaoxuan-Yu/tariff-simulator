
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
            
            // Check if response is successful and has data
            if (!tariffResponse.isSuccess() || tariffResponse.getData() == null) {
                writer.println("Error: " + (tariffResponse.getError() != null ? tariffResponse.getError() : "No data available"));
                writer.flush();
                writer.close();
                return;
            }
            
            TariffResponse.TariffCalculationData data = tariffResponse.getData();
            
            writer.println("Product,Brand,Exporting From,Importing To,Quantity,Unit,Product Cost,Tariff Rate,Tariff Amount,Total Cost,Tariff Type");
            writer.printf("%s,%s,%s,%s,%.2f,%s,%.2f,%.2f%%,%.2f,%.2f,%s%n",
                    data.getProduct(),
                    data.getBrand(),
                    data.getExportingFrom(),
                    data.getImportingTo(),
                    data.getQuantity(),
                    data.getUnit(),
                    data.getProductCost(),
                    data.getTariffRate(),
                    data.getTotalCost() - data.getProductCost(),
                    data.getTotalCost(),
                    data.getTariffType());
            writer.flush();
            writer.close();
        } catch (Exception e) {
            throw new ExportException("Failed to export tariff data to CSV", e);
        }
    }
}

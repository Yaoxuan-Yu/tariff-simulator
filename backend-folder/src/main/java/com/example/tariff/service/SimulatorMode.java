package com.example.tariff.service;
import com.example.tariff.dto.TariffResponse;
import org.springframework.stereotype.Component;

@Component
public class SimulatorMode implements TariffMode {

    private double simulatedAhs;
    private double simulatedMfn;

    // Default constructor for Spring
    public SimulatorMode() {}

    // Setter for dynamic rates
    public void setRates(double ahsRate, double mfnRate) {
        this.simulatedAhs = ahsRate;
        this.simulatedMfn = mfnRate;
    }

    @Override
    public TariffResponse calculate(String importCountry, String exportCountry, String hsCode, String brand) {

        return new TariffResponse(
                importCountry,
                exportCountry,
                hsCode,
                brand,
                simulatedAhs,
                simulatedMfn
        );
    }
}


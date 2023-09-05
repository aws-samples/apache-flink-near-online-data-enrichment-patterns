package com.amazonaws.samples.stream.temperature.event;

public class EnrichedTemperature extends Temperature {

    private String brand;
    private String countryCode;

    public EnrichedTemperature(String sensorId, long timestamp, long temperature, String status, String brand, String countryCode) {
        super(sensorId, timestamp, temperature, status);
        this.brand = brand;
        this.countryCode = countryCode;
    }

    public EnrichedTemperature() {
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public String toString() {
        return "EnrichedTemperature{" +
                "sensorId='" + getSensorId() + '\'' +
                ", timestamp='" + getTimestamp() + '\'' +
                ", temperature='" + getTemperature() + '\'' +
                ", status='" + getStatus() + '\'' +
                ", brand='" + brand + '\'' +
                ", countryCode='" + countryCode + '\'' +
                "}";
    }
}

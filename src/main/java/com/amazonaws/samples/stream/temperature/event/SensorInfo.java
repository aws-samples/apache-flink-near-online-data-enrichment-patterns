package com.amazonaws.samples.stream.temperature.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorInfo {

    private String id;
    private String brand;
    private String countryCode;

    public SensorInfo() {
    }

    public SensorInfo(String id, String brand, String countryCode) {
        this.id = id;
        this.brand = brand;
        this.countryCode = countryCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        return "SensorInfoLight{" +
                "id='" + id + '\'' +
                ", brand='" + brand + '\'' +
                ", countryCode='" + countryCode + '\'' +
                '}';
    }
}

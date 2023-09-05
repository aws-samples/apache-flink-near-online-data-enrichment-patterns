package com.amazonaws.samples.stream.temperature.event;

public class Temperature {

    private String sensorId;
    private long timestamp;
    private long temperature;
    private String status;

    public Temperature(
            String sensorId,
            long timestamp,
            long temperature,
            String status) {
        this.sensorId = sensorId;
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.status = status;
    }

    public Temperature() {
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public long getTemperature() {
        return temperature;
    }

    public void setTemperature(long temperature) {
        this.temperature = temperature;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Temperature{" +
                "sensorId='" + sensorId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", temperature='" + temperature + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

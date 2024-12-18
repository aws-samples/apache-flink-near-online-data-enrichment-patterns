package com.amazonaws.samples.stream.temperature.enrichment.sync;

import com.amazonaws.samples.stream.temperature.event.EnrichedTemperature;
import com.amazonaws.samples.stream.temperature.event.SensorInfo;
import com.amazonaws.samples.stream.temperature.event.Temperature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;

public class SyncEnrichmentFunction extends RichMapFunction<Temperature, EnrichedTemperature> {

    private final String getRequestUrl;
    private final ObjectMapper objectMapper;
    private transient AsyncHttpClient client;

    @Override
    public void open(Configuration parameters) throws Exception {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl
                .config()
                .setConnectTimeout(Duration.ofMillis(750))
                .setMaxRequestRetry(3)
                .setKeepAlive(true);
        this.client = Dsl.asyncHttpClient(clientBuilder);

        super.open(parameters);
    }

    @Override
    public void close() throws Exception {
        this.client.close();

        super.close();
    }

    public SyncEnrichmentFunction(String getRequestUrl) {
        this.getRequestUrl = getRequestUrl;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public EnrichedTemperature map(Temperature temperature) throws Exception {
        String url = this.getRequestUrl + temperature.getSensorId();

        // Retrieve response from sensor info API
        Response response = client
                .prepareGet(url)
                .execute()
                .toCompletableFuture()
                .get();

        // Parse the sensor info
        SensorInfo sensorInfo = parseSensorInfo(response.getResponseBody());

        // Merge the temperature sensor data and sensor info data
        return getEnrichedTemperature(temperature, sensorInfo);
    }

    private EnrichedTemperature getEnrichedTemperature(Temperature temperature, SensorInfo sensorInfo) {
        return new EnrichedTemperature(
                temperature.getSensorId(),
                temperature.getTimestamp(),
                temperature.getTemperature(),
                temperature.getStatus(),
                sensorInfo.getBrand(),
                sensorInfo.getCountryCode());
    }

    private SensorInfo parseSensorInfo(String responseBody) throws JsonProcessingException {
        return objectMapper.readValue(responseBody, SensorInfo.class);
    }
}

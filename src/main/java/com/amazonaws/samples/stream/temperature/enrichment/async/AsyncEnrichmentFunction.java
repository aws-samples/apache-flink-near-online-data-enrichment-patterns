package com.amazonaws.samples.stream.temperature.enrichment.async;

import com.amazonaws.samples.stream.temperature.event.EnrichedTemperature;
import com.amazonaws.samples.stream.temperature.event.SensorInfo;
import com.amazonaws.samples.stream.temperature.event.Temperature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class AsyncEnrichmentFunction extends RichAsyncFunction<Temperature, EnrichedTemperature> {

    private static final long serialVersionUID = 2098635244857937717L;

    private final String getRequestUrl;
    private final ObjectMapper objectMapper;
    private transient AsyncHttpClient client;

    public AsyncEnrichmentFunction(String url) {
        this.getRequestUrl = url;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl
                .config()
                .setConnectTimeout(Duration.ofMillis(750))
                .setRequestTimeout(Duration.ofMillis(2500))
                .setMaxRequestRetry(3)
                .setKeepAlive(true);
        client = Dsl.asyncHttpClient(clientBuilder);
    }

    @Override
    public void close() throws Exception {
        super.close();
        client.close();
    }

    @Override
    public void asyncInvoke(final Temperature temperature, final ResultFuture<EnrichedTemperature> resultFuture) {
        String url = this.getRequestUrl + temperature.getSensorId();

        // Retrieve response from sensor info API
        Future<Response> future = client
                .prepareGet(url)
                .execute();
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        Response response = future.get();

                        // Parse the sensor info as soon as it is available
                        return parseSensorInfo(response.getResponseBody());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .thenAccept((SensorInfo sensorInfo) ->

                    // Merge the temperature sensor data and sensor info data
                    resultFuture.complete(getEnrichedTemperature(temperature, sensorInfo)));
    }

    private static Set<EnrichedTemperature> getEnrichedTemperature(Temperature temperature, SensorInfo sensorInfo) {
        return Collections.singleton(
                new EnrichedTemperature(
                        temperature.getSensorId(),
                        temperature.getTimestamp(),
                        temperature.getTemperature(),
                        temperature.getStatus(),
                        sensorInfo.getBrand(),
                        sensorInfo.getCountryCode()));
    }

    private SensorInfo parseSensorInfo(String responseBody) throws JsonProcessingException {
        return objectMapper.readValue(responseBody, SensorInfo.class);
    }
}

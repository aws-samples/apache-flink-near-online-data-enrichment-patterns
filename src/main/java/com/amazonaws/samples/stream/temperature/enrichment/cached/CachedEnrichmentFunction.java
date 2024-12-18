package com.amazonaws.samples.stream.temperature.enrichment.cached;

import com.amazonaws.samples.stream.temperature.event.EnrichedTemperature;
import com.amazonaws.samples.stream.temperature.event.SensorInfo;
import com.amazonaws.samples.stream.temperature.event.Temperature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;

import java.time.Duration;

public class CachedEnrichmentFunction extends KeyedProcessFunction<String, Temperature, EnrichedTemperature> {

    private static final long serialVersionUID = 2098635244857937717L;

    private final long ttl;
    private final String getRequestUrl;
    private final ObjectMapper objectMapper;
    private transient AsyncHttpClient client;
    private transient ValueState<SensorInfo> cachedSensorInfoLight;

    public CachedEnrichmentFunction(String url, String ttl) {
        this.getRequestUrl = url;
        this.ttl = Long.parseLong(ttl);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void open(Configuration configuration) throws Exception {
        super.open(configuration);
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl
                .config()
                .setConnectTimeout(Duration.ofMillis(750))
                .setMaxRequestRetry(3)
                .setKeepAlive(true);
        client = Dsl.asyncHttpClient(clientBuilder);

        ValueStateDescriptor<SensorInfo> descriptor =
                new ValueStateDescriptor<>("sensorInfo", TypeInformation.of(new TypeHint<>() {
                }));

        StateTtlConfig ttlConfig = StateTtlConfig
                .newBuilder(Time.seconds(this.ttl))
                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                .setStateVisibility(StateTtlConfig.StateVisibility.ReturnExpiredIfNotCleanedUp)
                .build();
        descriptor.enableTimeToLive(ttlConfig);

        cachedSensorInfoLight = getRuntimeContext().getState(descriptor);
    }

    @Override
    public void close() throws Exception {
        super.close();
        client.close();
    }

    @Override
    public void processElement(Temperature temperature, KeyedProcessFunction<String, Temperature, EnrichedTemperature>.Context ctx, Collector<EnrichedTemperature> out) throws Exception {
        SensorInfo sensorInfoCachedEntry = cachedSensorInfoLight.value();

        // Check if sensor info is cached
        if (sensorInfoCachedEntry != null) {
            out.collect(getEnrichedTemperature(temperature, sensorInfoCachedEntry));
        } else {
            String url = this.getRequestUrl + temperature.getSensorId();

            // Retrieve response from sensor info API
            Response response = client
                    .prepareGet(url)
                    .execute()
                    .toCompletableFuture()
                    .get();

            // Parse the sensor info
            SensorInfo sensorInfo = parseSensorInfo(response.getResponseBody());

            // Cache the sensor info
            cachedSensorInfoLight.update(sensorInfo);

            // Merge the temperature sensor data and sensor info data
            out.collect(getEnrichedTemperature(temperature, sensorInfo));
        }
    }

    private static EnrichedTemperature getEnrichedTemperature(Temperature temperature, SensorInfo sensorInfo) {
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

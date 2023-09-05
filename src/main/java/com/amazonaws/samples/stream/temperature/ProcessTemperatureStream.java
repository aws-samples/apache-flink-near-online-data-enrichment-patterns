package com.amazonaws.samples.stream.temperature;

import com.amazonaws.regions.Regions;
import com.amazonaws.samples.stream.temperature.enrichment.async.AsyncProcessTemperatureStreamStrategy;
import com.amazonaws.samples.stream.temperature.enrichment.cached.CachedProcessTemperatureStreamStrategy;
import com.amazonaws.samples.stream.temperature.enrichment.sync.SyncProcessTemperatureStreamStrategy;
import com.amazonaws.samples.stream.temperature.utils.ParameterToolUtils;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;

/**
 * Expected application config parameters
 * - InputStreamName (default: "raw-sensor-stream")
 * - OutputStreamName (default: "enriched-sensor-stream")
 * - Region (default: current region or "eu-central-1")
 * - EnrichmentStrategy: "SYNC"|"ASYNC"|"CACHED" (default: "SYNC")
 * - SensorApiUrl
 * - CacheTtlSeconds (default: 60)
 */
public class ProcessTemperatureStream {
    public static final Logger LOG = LoggerFactory.getLogger(ProcessTemperatureStream.class);

    protected static final int DEFAULT_LOCAL_PARALLELISM = 4;

    public static final String DEFAULT_INPUT_STREAM_NAME = "raw-sensor-stream";
    public static final String DEFAULT_OUTPUT_STREAM_NAME = "enriched-sensor-stream";

    public static final String DEFAULT_API_URL = "https://dummyjson.com/products/";
    public static final String DEFAULT_REGION_NAME = Regions.getCurrentRegion() == null ? "eu-central-1" : Regions
            .getCurrentRegion()
            .getName();

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        ParameterTool parameter = ParameterToolUtils.getParameters(args, env);

        String strategy = parameter.get("EnrichmentStrategy", "SYNC");
        switch (strategy) {
            case "SYNC":
                new SyncProcessTemperatureStreamStrategy().run(env, parameter);
                break;
            case "ASYNC":
                new AsyncProcessTemperatureStreamStrategy().run(env, parameter);
                break;
            case "CACHED":
                new CachedProcessTemperatureStreamStrategy().run(env, parameter);
                break;
            default:
                throw new InvalidParameterException("Please choose one of the existing enrichment strategies (SYNC|ASYNC|CACHED)");
        }
    }

    /**
     * Set up a local stream execution environment.
     * <br>
     * Configures the default parallelism, enables the flame graph and sets the Flink dashboard port to 8081.
     * Additionally, it sets the checkpointing to exactly once and the interval to 30 seconds.
     * <br>
     * You can access your local dashboard here: localhost:8081/
     *
     * @return A local stream execution environment
     */
    public static StreamExecutionEnvironment getLocalStreamExecutionEnvironment() {
        Configuration configuration = new Configuration();
        configuration.setString("parallelism.default", Integer.toString(DEFAULT_LOCAL_PARALLELISM));
        configuration.setString("rest.flamegraph.enabled", "true");
        configuration.setString("rest.port", "8081");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(configuration);
        env.enableCheckpointing(60000, CheckpointingMode.EXACTLY_ONCE);
        return env;
    }
}

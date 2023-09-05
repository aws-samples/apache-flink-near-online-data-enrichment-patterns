package com.amazonaws.samples.stream.temperature.utils;

import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ParameterToolUtils {

    /**
     * Gets the {@link ParameterTool} from the given {@link StreamExecutionEnvironment}.
     * If the environment is a {@link LocalStreamEnvironment}, then the parameters are read from the command line.
     * If the environment is a {@link KinesisAnalyticsRuntime}, then the parameters are read from the
     * {@link KinesisAnalyticsRuntime#getApplicationProperties()}
     * @param args The command line arguments
     *              (if the environment is a {@link LocalStreamEnvironment})
     * @param env The {@link StreamExecutionEnvironment}
     *             (if the environment is a {@link KinesisAnalyticsRuntime})
     * @return The {@link ParameterTool}
     * @throws IOException If the {@link KinesisAnalyticsRuntime#getApplicationProperties()}
     *                     cannot be read
     */
    public static ParameterTool getParameters(String[] args, StreamExecutionEnvironment env) throws IOException {
        ParameterTool parameter;

        if (env instanceof LocalStreamEnvironment) {
            //read the parameters specified from the command line
            parameter = ParameterTool.fromArgs(args);
        } else {
            //read the parameters from the Kinesis Analytics environment
            Map<String, Properties> applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties();

            Properties flinkProperties = applicationProperties.get("FlinkApplicationProperties");

            if (flinkProperties == null) {
                throw new RuntimeException("Unable to load FlinkApplicationProperties properties from the Kinesis Analytics Runtime.");
            }

            parameter = ParameterToolUtils.fromApplicationProperties(flinkProperties);
        }
        return parameter;
    }

    /**
     * Creates a {@link ParameterTool} from the given {@link Properties}.
     *
     * @param properties The properties to create the {@link ParameterTool}
     * @return The created {@link ParameterTool}
     */
    private static ParameterTool fromApplicationProperties(Properties properties) {
        Map<String, String> map = new HashMap<>(properties.size());

        properties.forEach((k, v) -> map.put((String) k, (String) v));

        return ParameterTool.fromMap(map);
    }
}

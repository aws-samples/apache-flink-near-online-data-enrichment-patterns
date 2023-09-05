package com.amazonaws.samples.stream.temperature.enrichment;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public interface EnrichmentStrategy {

    /**
     * Strategies have to implement the run method and implement the Flink job.
     *
     * @param env       The StreamExecutionEnvironment to run on
     * @param parameter The parameters to configure the application
     * @throws Exception Thrown if the application can't be started
     */
    public void run(StreamExecutionEnvironment env, ParameterTool parameter) throws Exception;
}

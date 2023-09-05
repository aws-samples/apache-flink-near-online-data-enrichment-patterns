package com.amazonaws.samples.stream.temperature.enrichment.sync;

import com.amazonaws.samples.stream.temperature.enrichment.EnrichmentStrategy;
import com.amazonaws.samples.stream.temperature.event.EnrichedTemperature;
import com.amazonaws.samples.stream.temperature.event.Temperature;
import com.amazonaws.samples.stream.temperature.serialize.TemperatureDeserializationSchema;
import com.amazonaws.samples.stream.temperature.serialize.TemperatureSerializationSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kinesis.sink.KinesisStreamsSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer;
import org.apache.flink.streaming.connectors.kinesis.config.AWSConfigConstants;
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants;

import java.util.Properties;

import static com.amazonaws.samples.stream.temperature.ProcessTemperatureStream.*;


public class SyncProcessTemperatureStreamStrategy implements EnrichmentStrategy {

    @Override
    public void run(StreamExecutionEnvironment env, ParameterTool parameter) throws Exception {
        Properties kinesisConsumerConfig = new Properties();
        //set the region the Kinesis stream is located in
        kinesisConsumerConfig.setProperty(AWSConfigConstants.AWS_REGION, parameter.get("Region", DEFAULT_REGION_NAME));
        //obtain credentials through the DefaultCredentialsProviderChain, which includes the instance metadata
        kinesisConsumerConfig.setProperty(AWSConfigConstants.AWS_CREDENTIALS_PROVIDER, "AUTO");
        //poll new events from the Kinesis stream once every second
        kinesisConsumerConfig.setProperty(ConsumerConfigConstants.SHARD_GETRECORDS_INTERVAL_MILLIS, "1000");

        //create Kinesis source
        DataStream<Temperature> temperatureDataStream =
                env
                        .addSource(new FlinkKinesisConsumer<>(
                                //read events from the Kinesis stream passed in as a parameter
                                parameter.get("InputStreamName", DEFAULT_INPUT_STREAM_NAME),
                                //deserialize events with EventSchema
                                new TemperatureDeserializationSchema(),
                                //using the previously defined properties
                                kinesisConsumerConfig))
                        .name("Temperature input")
                        .uid("Temperature input");

        DataStream<EnrichedTemperature> enrichedTemperatureDataStream =
                temperatureDataStream
                        .map(new SyncEnrichmentFunction(parameter.get("SensorApiUrl", DEFAULT_API_URL)))
                        .name("Sync enrichment")
                        .uid("Sync enrichment");


        Properties outputProperties = new Properties();
        outputProperties.setProperty(ConsumerConfigConstants.AWS_REGION, DEFAULT_REGION_NAME);
        outputProperties.setProperty("AggregationEnabled", "false");

        enrichedTemperatureDataStream
                .sinkTo(KinesisStreamsSink
                                .<EnrichedTemperature>builder()
                                .setKinesisClientProperties(outputProperties)
                                .setSerializationSchema(new TemperatureSerializationSchema())
                                .setPartitionKeyGenerator(element -> String.valueOf(element.hashCode()))
                                .setStreamName(parameter.get("OutputStreamName", DEFAULT_OUTPUT_STREAM_NAME))
                                .setFailOnError(false)
                                .setMaxBatchSize(500)
                                .setMaxInFlightRequests(500)
                                .setMaxBufferedRequests(10_000)
                                .setMaxTimeInBufferMS(5_000)
                                .build())
                .name("Sync output")
                .uid("Sync output");

        LOG.info("Reading events from stream");

        // execute program
        env.execute("Flink Streaming Temperature Synchronous Enrichment");
    }
}


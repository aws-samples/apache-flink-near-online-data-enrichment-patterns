package com.amazonaws.samples.stream.temperature.serialize;

import com.amazonaws.samples.stream.temperature.event.Temperature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

public class TemperatureDeserializationSchema implements DeserializationSchema<Temperature> {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Temperature deserialize(byte[] message) throws IOException {
        return objectMapper.readValue(message, Temperature.class);
    }

    @Override
    public boolean isEndOfStream(Temperature nextElement) {
        return false;
    }

    @Override
    public TypeInformation<Temperature> getProducedType() {
        return TypeInformation.of(Temperature.class);
    }
}

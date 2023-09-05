package com.amazonaws.samples.stream.temperature.event;

import org.junit.jupiter.api.Test;

public class EnrichedTemperatureTest {
    @Test
    public void testAssertSerializedAsPojo() {
        PojoTestUtils.assertSerializedAsPojo(EnrichedTemperature.class);
    }

    @Test
    public void testAssertSerializedAsPojoWithoutKryo() {
        PojoTestUtils.assertSerializedAsPojoWithoutKryo(EnrichedTemperature.class);
    }
}
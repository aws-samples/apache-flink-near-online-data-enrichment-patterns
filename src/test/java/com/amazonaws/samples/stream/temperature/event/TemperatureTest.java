package com.amazonaws.samples.stream.temperature.event;

import org.junit.jupiter.api.Test;

public class TemperatureTest {
    @Test
    public void testAssertSerializedAsPojo() {
        PojoTestUtils.assertSerializedAsPojo(Temperature.class);
    }

    @Test
    public void testAssertSerializedAsPojoWithoutKryo() {
        PojoTestUtils.assertSerializedAsPojoWithoutKryo(Temperature.class);
    }
}
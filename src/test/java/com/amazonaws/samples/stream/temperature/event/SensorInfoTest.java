package com.amazonaws.samples.stream.temperature.event;

import org.junit.jupiter.api.Test;

public class SensorInfoTest {
    @Test
    public void testAssertSerializedAsPojo() {
        PojoTestUtils.assertSerializedAsPojo(SensorInfo.class);
    }

    @Test
    public void testAssertSerializedAsPojoWithoutKryo() {
        PojoTestUtils.assertSerializedAsPojoWithoutKryo(SensorInfo.class);
    }
}
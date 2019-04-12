package com.liangx.spring.kafka.common;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class WaterLevelRecordDeserializer implements Deserializer<WaterLevelRecord> {
    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public WaterLevelRecord deserialize(String s, byte[] bytes) {
        return JSON.parseObject(bytes, WaterLevelRecord.class);
    }

    @Override
    public void close() {

    }
}

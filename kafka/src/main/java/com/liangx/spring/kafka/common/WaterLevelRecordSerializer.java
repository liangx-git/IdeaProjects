package com.liangx.spring.kafka.common;


import com.alibaba.fastjson.JSON;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;



public class WaterLevelRecordSerializer implements Serializer<WaterLevelRecord> {
    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public byte[] serialize(String s, WaterLevelRecord waterLevelRecord) {
        return JSON.toJSONBytes(waterLevelRecord);
    }

    @Override
    public void close() {

    }
}

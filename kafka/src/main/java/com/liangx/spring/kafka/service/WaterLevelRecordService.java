package com.liangx.spring.kafka.service;

import com.liangx.spring.kafka.common.WaterLevelRecord;

import java.util.List;

public interface WaterLevelRecordService {

    void insertRecord(WaterLevelRecord record);

    void insertRecords(List<WaterLevelRecord> records);

    WaterLevelRecord queryById(Integer id);
}
package com.liangx.spring.kafka.mapper;

import com.liangx.spring.kafka.common.WaterLevelRecord;

import java.sql.Timestamp;
import java.util.List;

public interface WaterLevelRecordMapper {

    void insertRecord(WaterLevelRecord waterLevelRecord);

    void insertRecords(List<WaterLevelRecord> records);

    WaterLevelRecord queryById(int id);

    double getAvgWaterLevelByInterval(Timestamp beginTime, Timestamp endTime);

}

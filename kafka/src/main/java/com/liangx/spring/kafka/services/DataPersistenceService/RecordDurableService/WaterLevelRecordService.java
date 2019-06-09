package com.liangx.spring.kafka.services.DataPersistenceService.RecordDurableService;

import com.liangx.spring.kafka.common.WaterLevelRecord;

import java.sql.Timestamp;
import java.util.List;

public interface WaterLevelRecordService {

    //插入单挑记录
    void insertRecord(WaterLevelRecord record);

    //批量插入数据
    void insertRecords(List<WaterLevelRecord> records);

    //根据时间戳范围获取平均水位
    double getAvgWaterLevelByInterval(Timestamp beginTime, Timestamp endTime);
}

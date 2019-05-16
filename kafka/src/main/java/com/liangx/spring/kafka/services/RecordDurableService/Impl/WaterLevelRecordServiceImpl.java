package com.liangx.spring.kafka.services.RecordDurableService.Impl;

import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.mapper.WaterLevelRecordMapper;
import com.liangx.spring.kafka.services.RecordDurableService.WaterLevelRecordService;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.List;

@Service
public class WaterLevelRecordServiceImpl implements WaterLevelRecordService {

    @Autowired
    private SqlSession sqlSession;

    @Override
    public void insertRecord(WaterLevelRecord record) {
        WaterLevelRecordMapper waterLevelRecordMapper = sqlSession.getMapper(WaterLevelRecordMapper.class);
        waterLevelRecordMapper.insertRecord(record);
    }

    @Override
    public void insertRecords(List<WaterLevelRecord> records) {
        WaterLevelRecordMapper waterLevelRecordMapper = sqlSession.getMapper(WaterLevelRecordMapper.class);
        waterLevelRecordMapper.insertRecords(records);
    }


    @Override
    public double getAvgWaterLevelByInterval(Timestamp beginTime, Timestamp endTime) {
        WaterLevelRecordMapper waterLevelRecordMapper = sqlSession.getMapper(WaterLevelRecordMapper.class);
        DecimalFormat df = new DecimalFormat("0.0");
        double avgWaterLevels = waterLevelRecordMapper.getAvgWaterLevelByInterval(beginTime, endTime);
        return Double.valueOf(df.format(avgWaterLevels));
    }


}

package com.liangx.spring.kafka.utils;

import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.common.WaterLevelRecordSerializer;
import com.liangx.spring.kafka.service.WaterLevelRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;

/**
 * 维护一个预取缓冲队列，提供给刚建立WebSocket连接用户，用于初始化填充图表
 */
@Component
@Slf4j
public class PreparedBufferUtil {

    @Autowired
    private WaterLevelRecordService waterLevelRecordService;

    //数据预缓存队列，只在session连接时推送一次，用于填充前端的图表
    private Queue<WaterLevelRecord> realBuffer; //real图表缓存


    private void checkRealBuffer(){
        if (null == realBuffer){
            realBuffer = new LinkedList<WaterLevelRecord>();
        }
    }

    /**
     * 更新real缓存队列数据，在WebListener中被调用
     * @param waterLevelRecord
     */
    public void updateRealBuffer(WaterLevelRecord waterLevelRecord){
        checkRealBuffer();
        realBuffer.offer(waterLevelRecord);
        //当队列达到上限（与前端图表x轴刻度对应），队头出队
        if (11 == realBuffer.size()){
            realBuffer.poll();
        }
    }

    /**
     * 获取real缓存队列数据
     * @return
     */
    public Queue<WaterLevelRecord> getRealBuffer(){
        checkRealBuffer();
        return realBuffer;
    }

    /**
     * 获取houly图表缓存
     * @return
     */
    public List<WaterLevelRecord> getHourlyBuffer(){
        Calendar calendar = Calendar.getInstance();
        Timestamp endTime = new Timestamp(calendar.getTimeInMillis());  //当前时间戳
        calendar.add(Calendar.HOUR, -1);
        Timestamp beginTime = new Timestamp(calendar.getTimeInMillis());    //一个小时前时间戳
        List<WaterLevelRecord> hourlyBuffer = new ArrayList<>();
        for (int i = 0; i < 11; ++i){
            log.info("beginTime = " + beginTime + " endTime = " + endTime);
            double avgWaterLevel = waterLevelRecordService.getAvgWaterLevelByInterval(beginTime, endTime);
            WaterLevelRecord waterLevelRecord = new WaterLevelRecord();
            waterLevelRecord.setSiteId(104);
            waterLevelRecord.setSiteName("hohai");
            waterLevelRecord.setId(i);
            waterLevelRecord.setTime(endTime);
            waterLevelRecord.setWaterLevel(avgWaterLevel);
            hourlyBuffer.add(waterLevelRecord);

            endTime = beginTime;
            calendar.add(Calendar.HOUR, -1);
            beginTime = new Timestamp(calendar.getTimeInMillis());
        }
        Collections.reverse(hourlyBuffer);
        return hourlyBuffer;
    }

    /**
     * 获取weekly图表缓存
     * @return
     */
    public List<WaterLevelRecord> getWeeklyBuffer(){
        Calendar calendar = Calendar.getInstance();
        Timestamp endTime = new Timestamp(calendar.getTimeInMillis());  //当前时间戳
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Timestamp beginTime = new Timestamp(calendar.getTimeInMillis());    //一天前时间戳
        List<WaterLevelRecord> weeklyBuffer = new ArrayList<>();
        for (int i = 0; i < 7; ++i){
            log.info("beginTime = " + beginTime + " endTime = " + endTime);
            double avgWaterLevel = waterLevelRecordService.getAvgWaterLevelByInterval(beginTime, endTime);
            WaterLevelRecord waterLevelRecord = new WaterLevelRecord();
            waterLevelRecord.setSiteId(104);
            waterLevelRecord.setSiteName("hohai");
            waterLevelRecord.setId(i);
            waterLevelRecord.setTime(endTime);
            waterLevelRecord.setWaterLevel(avgWaterLevel);
            weeklyBuffer.add(waterLevelRecord);

            endTime = beginTime;
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            beginTime = new Timestamp(calendar.getTimeInMillis());
        }
        Collections.reverse(weeklyBuffer);
        return weeklyBuffer;
    }


}

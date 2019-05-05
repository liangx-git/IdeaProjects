package com.liangx.spring.kafka.services.DurableListenerService;

import com.liangx.spring.kafka.common.SiteInformation;
import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.services.MyKafkaConsumer;
import com.liangx.spring.kafka.services.RecordDurableService.WaterLevelRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DurableKafkaConsumer implements MyKafkaConsumer {

//    private int count = 0;
    private double averageHourlyWaterLevel = 0;

    private int averageHourlyCount = 0;

    private Timer timer;

//    @Autowired
//    private SiteInformation siteInformation;

    //持久层服务
    @Autowired
    private WaterLevelRecordService waterLevelRecordService;

    public DurableKafkaConsumer(){

        //没小时保存一次平均水位
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                       storingDatasAtWholePoint();
                    }
                }, 0, 60 * 60, TimeUnit.SECONDS);


//        timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                log.info("[ DurableKafkaConsumer ] : 执行定时任务（storingDatasAtWholePoint）");
//                storingDatasAtWholePoint();
//            }
//        }, 0, 60 * 1000);
        log.info("[ DurableKafkaConsumer ] : 启动定时任务（storingDatasAtWholePoint）");

    }

    @KafkaListener(id = "durableListener", clientIdPrefix = "durable", topics = "${kafka.consumer.topic}", containerFactory = "batchKafkaListenerContainerFactory")
    public void durableListener(List<ConsumerRecord<String, WaterLevelRecord>> consumerRecords){

        updateSiteInformationIfDiff(consumerRecords.get(0).value());

        updateAverageHourWaterLevelAndAverageHourlyCount(consumerRecords);
    }

    private void updateSiteInformationIfDiff(WaterLevelRecord waterLevelRecord){
        int siteId = waterLevelRecord.getSiteId();
        String siteName = waterLevelRecord.getSiteName();

        if (SiteInformation.siteId == 0 || SiteInformation.siteId != siteId){
            SiteInformation.siteId = siteId;
            SiteInformation.siteName =siteName;
        }
    }

    private void storingDatasAtWholePoint(){
        DecimalFormat df = new DecimalFormat("0.0");
        if (averageHourlyWaterLevel != 0){
            //准备数据
            double waterLevel =Double.valueOf(df.format(averageHourlyWaterLevel / averageHourlyCount));
            WaterLevelRecord waterLevelRecord = new WaterLevelRecord(new Timestamp(System.currentTimeMillis()), SiteInformation.siteId, SiteInformation.siteName, waterLevel);

            //数据持久化
            waterLevelRecordService.insertRecord(waterLevelRecord);
            log.info("[ DurableKafkaConsumer ] : 数据持久化(" + waterLevelRecord + ")");

            averageHourlyWaterLevel = 0;
            averageHourlyCount = 0;
        }
    }

    private void updateAverageHourWaterLevelAndAverageHourlyCount(List<ConsumerRecord<String, WaterLevelRecord>> consumerRecords){
        for(ConsumerRecord<String, WaterLevelRecord> consumerRecord : consumerRecords){
            averageHourlyWaterLevel += consumerRecord.value().getWaterLevel();
            ++averageHourlyCount;
            log.info("[ DurableKafkaConsumer ] : averageHourlyWaterLevel = " + averageHourlyWaterLevel + " averageHourlyCount = " + averageHourlyCount);
        }
    }

}

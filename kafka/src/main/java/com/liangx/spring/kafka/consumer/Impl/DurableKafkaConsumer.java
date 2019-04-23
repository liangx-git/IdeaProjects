package com.liangx.spring.kafka.consumer.Impl;

import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.consumer.MyKafkaConsumer;
import com.liangx.spring.kafka.service.WaterLevelRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DurableKafkaConsumer implements MyKafkaConsumer {

    private int count = 0;

//    private double hourlyWaterVolume = 0;

//    private WaterLevelRecord waterLevelRecord;

    //持久层服务
    @Autowired
    private WaterLevelRecordService waterLevelRecordService;

//    public DurableKafkaConsumer()
//    {
//        waterLevelRecord = new WaterLevelRecord();
//        waterLevelRecord.setSiteId(-1);
//    }

    @KafkaListener(id = "durableListener", clientIdPrefix = "durable", topics = "${kafka.consumer.topic}", containerFactory = "batchKafkaListenerContainerFactory")
    public void durableListener(List<ConsumerRecord<String, WaterLevelRecord>> consumerRecords){
//        log.info(">>>>>>>>>>正在拉取第" + count + "批数据<<<<<<<<<<");

//        if (waterLevelRecord.getSiteId() == -1){
//            waterLevelRecord = consumerRecords.get(0).value();
//        }

//        Calendar calendar = Calendar.getInstance();
//        log.info("calendar.get(SECOND = " + calendar.get(Calendar.SECOND) + ")");
//        if (calendar.get(Calendar.SECOND) == 0 && hourlyWaterVolume != 0){
            //准备数据
//            double houlyAvgWaterLevel = hourlyWaterVolume / count;
//            waterLevelRecord.setWaterLevel(houlyAvgWaterLevel);
            //数据持久化
//            log.info(">>>>>>>>>>>>>>>>>>> DurableListener 数据持久化： hourlyAvgWaterLevelRecord = " + waterLevelRecord + " <<<<<<<<<<<<<<<<<<<<<");
//        waterLevelRecordService.insertRecord(waterLevelRecord);
//            count = 0;
//            hourlyWaterVolume = 0;
//        }

        List<WaterLevelRecord> waterLevelRecords = new ArrayList<>();
        for(ConsumerRecord<String, WaterLevelRecord> consumerRecord : consumerRecords){
            waterLevelRecords.add(consumerRecord.value());
//            if (consumerRecord.value().getSiteId() == this.waterLevelRecord.getSiteId()){
//                hourlyWaterVolume += consumerRecord.value().getWaterLevel();
//                ++count;
//            }
        }
        //将数据写入数据库中

        log.info(">>>>>>>>>>>>>> DurableListener 数据持久化： " + waterLevelRecords + " <<<<<<<<<<<<<<<<<<<<<");
        waterLevelRecordService.insertRecords(waterLevelRecords);

//        addCount();
    }

//    public synchronized void addCount(){
//        ++count;
//    }

//    private void initWaterRecord(WaterLevelRecord waterLevelRecord){
//        if (waterLevelRecord != null){
//            this.waterLevelRecord = waterLevelRecord;
//        }
//    }

}

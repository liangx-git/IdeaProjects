package com.liangx.spring.kafka.consumer.Impl;

import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.consumer.KafkaConsumer;
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
public class DurableKafkaConsumer implements KafkaConsumer {

    private int count = 0;
    //持久层服务
    @Autowired
    private WaterLevelRecordService waterLevelRecordService;

    @KafkaListener(id = "durableListener", clientIdPrefix = "durable", topics = "${kafka.consumer.topic}", containerFactory = "batchKafkaListenerContainerFactory")
    public void durableListener(List<ConsumerRecord<String, WaterLevelRecord>> consumerRecords){
        log.info(">>>>>>>>>>正在拉取第" + count + "批数据<<<<<<<<<<");

        List<WaterLevelRecord> waterLevelRecords = new ArrayList<>();
        for(ConsumerRecord<String, WaterLevelRecord> consumerRecord : consumerRecords){
            waterLevelRecords.add(consumerRecord.value());
        }
        //将数据写入数据库中
        waterLevelRecordService.insertRecords(waterLevelRecords);

        addCount();
    }

    public synchronized void addCount(){
        ++count;
    }

}

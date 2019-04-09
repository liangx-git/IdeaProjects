package com.liangx.spring.kafka.consumer;

import com.google.gson.Gson;
import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.service.WaterLevelRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class KafkaConsumer {

    private final Gson gson = new Gson();

    private static int count = 1;

    @Autowired
    private WaterLevelRecordService waterLevelRecordService;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    public void startListenerForWeb(){
        //判断监听容器是否启动，未启动则将其启动
        if (!registry.getListenerContainer("webListener").isRunning()){
            registry.getListenerContainer("webListener").start();
        }
        //开启监听

        if (registry.getListenerContainer("webListener").isContainerPaused()) {
            registry.getListenerContainer("webListener").resume();
        }
    }

    //当web提出请求时才开始监听
    @KafkaListener(id="webListener", clientIdPrefix="web", topics="${kafka.consumer.topic}", containerFactory="responseForWebKafkaListenerContainerFactory")
    public void webListener(ConsumerRecord<String, WaterLevelRecord> record){
        log.info("web-listener : " + record.toString());
    }

    //持续监听，采用批量获取方式，并将数据写入到持久层
    @KafkaListener(id="durableListener", clientIdPrefix="durable", topics="${kafka.consumer.topic}", containerFactory="batchKafkaListenerContainerFactory")
    public void durableListener(List<ConsumerRecord<String, WaterLevelRecord>> consumerRecords){
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>poll第 " + count++ + " 批数据<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<,");

        List<WaterLevelRecord> waterLevelRecords = new ArrayList<>();
        for (ConsumerRecord<String, WaterLevelRecord> consumerRecord : consumerRecords)
        {
            //将数据写入持久层
            //waterLevelRecordService.insertRecord(record.value());
//            log.info("开始存储： " +  record.toString() +'\n');
            waterLevelRecords.add((WaterLevelRecord) consumerRecord.value());
        }
        waterLevelRecordService.insertRecords(waterLevelRecords);
    }

}

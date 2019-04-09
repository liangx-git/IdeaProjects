package com.liangx.spring.kafka.producer;

import com.liangx.spring.kafka.common.WaterLevelRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

    @Autowired
    //@Qualifier("kafkaTemplate")
    private KafkaTemplate<String, WaterLevelRecord> kafkaTemplate;

    public void send(String topic, WaterLevelRecord message) {
        kafkaTemplate.send(topic, message);
    }

    public void send(String topic, String key, WaterLevelRecord record) {
        ProducerRecord<String, WaterLevelRecord> producerRecord = new ProducerRecord<>(topic, key, record);

//        long startTime = System.currentTimeMillis();
//        ListenableFuture<SendResult<String, WaterLevelRecord>> future =
        kafkaTemplate.send(producerRecord);
//        future.addCallback(new ProducerCallback(startTime, key, entity));
    }

}
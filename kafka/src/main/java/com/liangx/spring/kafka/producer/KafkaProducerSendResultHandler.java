package com.liangx.spring.kafka.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Component;

//生产者消息回调类
@Component
public class KafkaProducerSendResultHandler implements ProducerListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerSendResultHandler.class);

    @Override
    public void onError(ProducerRecord producerRecord, Exception exception) {
        log.info("[ KafkaProducerSendReslutHandler ] : Message send error: " + producerRecord.toString());
    }

    @Override
    public void onSuccess(ProducerRecord producerRecord, RecordMetadata recordMetadata) {
        log.info("[ KafkaProducerSendResultHandler ] : Message send success: " + producerRecord.toString());
    }
}

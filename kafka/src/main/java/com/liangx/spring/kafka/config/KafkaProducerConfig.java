package com.liangx.spring.kafka.config;


import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.common.WaterLevelRecordSerializer;
import com.liangx.spring.kafka.producer.KafkaSendResultHandler;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaProducerConfig {

    //链接地址
    @Value("${kafka.producer.servers}")
    private String servers;

    //重试,0为不启用重试机制
    @Value("${kafka.producer.retries}")
    private int retries;

    //控制批处理大小，单位为字节
    @Value("${kafka.producer.batch.size}")
    private int batchSize;

    //批量发送，延迟为linger毫秒，与batchSize配合使得生产者减少发送次数，增加发送量
    @Value("${kafka.producer.linger}")
    private int linger;

    //生产者可以使用的总内存字节来缓存待发送记录
    @Value("${kafka.producer.buffer.memory}")
    private int bufferMemory;

    @Value("${kafka.producer.key.serializer}")
    private String keySerializer;

    //配置生产着
    public Map<String, Object> producerConfigs(){
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        props.put(ProducerConfig.LINGER_MS_CONFIG, linger);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, WaterLevelRecordSerializer.class.getName());

        return props;
    }

    //创建KafkaTemplate
    @Bean("defaultKafkaTemplate")
    public KafkaTemplate<String, WaterLevelRecord> defaultKafkaTemplate(){
        KafkaTemplate<String, WaterLevelRecord> defaultTemplate = new KafkaTemplate<>(producerFactory());
        defaultTemplate.setDefaultTopic("liangx-message");
        defaultTemplate.setProducerListener(new KafkaSendResultHandler());
        return defaultTemplate;
    }

    public ProducerFactory<String, WaterLevelRecord> producerFactory(){
        DefaultKafkaProducerFactory factory = new DefaultKafkaProducerFactory(producerConfigs());

        //配置事务
        //factory.transactionCapable();   //开启事物功能
        //factory.setTransactionIdPrefix("tran-");    //设置生成Transactional.id的前缀
        return factory;
    }

    //通过在ProducerFactory中设置transactionCapable开启事务功能后，要求定义
    // 事务管理类
//    @Bean
//    public KafkaTransactionManager<String, WaterLevelRecord> transactionManager(){
//        KafkaTransactionManager<String, WaterLevelRecord> manager = new KafkaTransactionManager<>(producerFactory());
//        return manager;
//    }

}


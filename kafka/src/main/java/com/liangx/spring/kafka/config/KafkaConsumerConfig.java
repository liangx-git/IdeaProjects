package com.liangx.spring.kafka.config;

import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.common.WaterLevelRecordDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    //链接地址
    @Value("${kafka.consumer.servers}")
    private String servers;

    @Value("${kafka.consumer.topic}")
    private String topic;

    //是否自动提交
    @Value("${kafka.consumer.enable.auto.commit}")
    private boolean enableAutoCommit;

    //session超时设置
    @Value("${kafka.consumer.session.timeout}")
    private String sessionTimeout;

    @Value("${kafka.consumer.heartbeat.interval}")
    private String heartbeatInterval;

    //自动提交频率
    @Value("${kafka.consumer.auto.commit.interval}")
    private String autoCommitInterval;

//    消费者组id
//    @Value("${kafka.consumer.group.id}")
//    private String groupId;

    //
    @Value("${kafka.consumer.auto.offset.reset}")
    private String autoOffsetReset;

    @Value("${kafka.consumer.max.poll.records}")
    private String maxPollRecords;

    @Value("${kafka.consumer.max.poll.interval.ms}")
    private String maxPollIntervalMs;

    @Value("${kafka.consumer.fetch.max.wait.ms}")
    private String fetchMaxWaitMs;

    @Value("${kafka.consumer.fetch.min.size}")
    private String fetchMinSize;

    //key的反序列化方式
    @Value("${kafka.consumer.key.deserializer}")
    private String keyDeserializer;

    @Value("${kafka.consumer.concurrency}")
    private int concurrency;

    //消费者配置
    private Map<String, Object> consumerConfigs() {
        Map<String, Object> propsMap = new HashMap<>();
        propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        propsMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, autoCommitInterval);
        propsMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        propsMap.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, heartbeatInterval);
        propsMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        propsMap.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);    //设置一次批量拉取量
        propsMap.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);    //最大poll间隔
        propsMap.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, fetchMinSize);
        propsMap.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetchMaxWaitMs);
        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, WaterLevelRecordDeserializer.class.getName());
//        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
//        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);  //false表示禁用自动提交。通常为了启用ack模式，也就是consumer中的低级api进行消息offset保存
        return propsMap;
    }

    private ConsumerFactory<String, WaterLevelRecord> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    //监听容器工厂类,负责产生监听容器,同时在使用@KafakListener时定义containerFactory属性时需要
    //批量监听，用户监听数据并保存于数据库中
    @Bean("batchKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, WaterLevelRecord> concurrentKafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String, WaterLevelRecord> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(true);     //开启批量监听
        factory.getContainerProperties().setPollTimeout(30000);  //poll过期时间
        return factory;
    }

    //非自起监听器容器工厂，用于响应web请求
    @Bean("webKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, WaterLevelRecord> kafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String, WaterLevelRecord> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setAutoStartup(false);  //禁止监听器自动启动
        factory.setConcurrency(concurrency);    //设置监听并发数,通常要求小于Topic的Partition数量，否则会有监听客户端空转
        //factory.getContainerProperties().setPollTimeout(1500);
        return factory;
    }
}

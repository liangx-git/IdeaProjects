package com.liangx.spring.kafka.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaInitialConfiguration {

    //创建topic方法1.使用注解,创建TopicName为"liangx-message"，分区数为8, 副本为1
    @Bean
    public NewTopic initialTopic(){
        return new NewTopic("liangx-message", 2, (short)1);
    }

    //创建topic方法2, 手动编码创建topic
//    @Bean
//    public KafkaAdmin kafkaAdmin(@Value("${spring.kafka.producer.servers}") String service){
//        Map<String, Object> props = new HashMap<>();
//        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, service);
//        KafkaAdmin admin = new KafkaAdmin(props);
//        return admin;
//    }
//
//    @Bean
//    public AdminClient adminClient(@Autowired KafkaAdmin kafkaAdmin){
//        return AdminClient.create(kafkaAdmin.getConfig());
//    }

}

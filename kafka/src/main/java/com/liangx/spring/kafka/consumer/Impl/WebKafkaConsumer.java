package com.liangx.spring.kafka.consumer.Impl;

import com.alibaba.fastjson.JSON;
import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.consumer.KafkaConsumer;
import com.liangx.spring.kafka.utils.UserSessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class WebKafkaConsumer implements KafkaConsumer {

   //使用registry对象控制监听器的启动
    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private UserSessionUtil userSessionUtil;

    private int i = 0;

    //当web提出请求调用startWebListener时才开始监听kafka消息
    @KafkaListener(id="webListener", clientIdPrefix="web", topics="${kafka.consumer.topic}", containerFactory="webKafkaListenerContainerFactory")
    public void webListener(ConsumerRecord<String, WaterLevelRecord> consumerRecord) throws IOException {
        log.info(">>>>>>>>>>>>>>>>>>> web-listener : " + consumerRecord.toString());

        //准备数据
        WaterLevelRecord waterLevelRecord = consumerRecord.value();
        //通过session实现向客户端推送数据，当存在多个用户时逐个发送
        List<Session> userSessions = userSessionUtil.getUserSessions();
        for (Session userSession : userSessions){
            log.info(">>>>>>>>>>>>>> WebKafkaConsumer info: 给用户session(" + userSession.getId() + ")发送数据");
            userSession.getBasicRemote().sendText(JSON.toJSONString(waterLevelRecord));
        }
        //更新UserSessionUtil中的预缓存队列
        userSessionUtil.updatePrepareRecords(waterLevelRecord);
    }

    /**
     * 开启webListener监听kafka消息
     */
    public void startWebListener(){
        log.info(">>>>>>>>>>>>>>>>>>>> WebKafkaConsumer info: WebListener.start <<<<<<<<<<<<<<<<<<<<<<");

        //判断监听容器是否已经启动，否则启动
        if (!registry.getListenerContainer("webListener").isRunning()){
            registry.getListenerContainer("webListener").start();
        }
        registry.getListenerContainer("webListener").resume();
    }

    /**
     * 关闭/暂停WebKafkaListener
      */
    public void stopWebListener(){
        log.info(">>>>>>>>>>>>>>>>>>>> WebKafkaConsumer info: WebListener.pause <<<<<<<<<<<<<<<<<<<<<<");
        if (registry.getListenerContainer("webListener").isRunning()){
           registry.getListenerContainer("webListener").pause();
           //registry.getListenerContainer("webListener").stop();
        }
    }

    public boolean listenerIsWorking(){
//        log.info("**********************webKafkaConsumer.listenerIsRunning = " + registry.getListenerContainer("webListener").isRunning());
//        log.info("**********************webKafkaConsumer.listenerIsPauseRequested = " + registry.getListenerContainer("webListener").isPauseRequested());
//        log.info("**********************webKafkaConsumer.listenerIsContainerPaused = " + registry.getListenerContainer("webListener").isContainerPaused());

        return registry.getListenerContainer("webListener").isRunning()
                && !registry.getListenerContainer("webListener").isPauseRequested();
    }
}

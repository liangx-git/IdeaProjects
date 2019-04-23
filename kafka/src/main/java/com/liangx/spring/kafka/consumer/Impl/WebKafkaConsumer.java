package com.liangx.spring.kafka.consumer.Impl;

import com.alibaba.fastjson.JSON;
import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.consumer.MyKafkaConsumer;
import com.liangx.spring.kafka.utils.PreparedBufferUtil;
import com.liangx.spring.kafka.utils.UserSessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class WebKafkaConsumer implements MyKafkaConsumer {

   //使用registry对象控制监听器的启动
    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private UserSessionUtil userSessionUtil;

    @Autowired
    private PreparedBufferUtil preparedBufferUtil;

    public boolean isBack = false;

    private int i = 0;

    private int partition;

    private Long offset;

    @Autowired
    private KafkaListenerContainerFactory kafkaListenerContainerFactory;

//    @Autowired
//    private BackTrackingKafkaConsumer backTrackingKafkaConsumer;


    //当web提出请求调用startWebListener时才开始监听kafka消息
    @KafkaListener(id="webListener", clientIdPrefix="web", topics="${kafka.consumer.topic}", containerFactory="webKafkaListenerContainerFactory")
    public void webListener(ConsumerRecord<String, WaterLevelRecord> consumerRecord/*, Acknowledgment ack*/, Consumer consumer) throws IOException {
        log.info(">>>>>>>>>>>>>> messgae info : partition = " + consumerRecord.partition() + ", offset = " + consumerRecord.offset() + "<<<<<<<<<<<<<<<<<<<<<<<<");
        //准备数据
        WaterLevelRecord waterLevelRecord = consumerRecord.value();
        //发给echar_main数据
        List<Object> recordForEchartMain = new ArrayList<>();
        recordForEchartMain.add("REAL");
        recordForEchartMain.add(waterLevelRecord);

        //通过session实现向客户端推送数据，当存在多个用户时逐个发送
        Map<Session, Map<String, String>> userSessionsInfo = userSessionUtil.getUserSessions();
        for (Session userSession : userSessionsInfo.keySet()){
            Map<String, String> sessionProps = userSessionsInfo.get(userSession);
            if (sessionProps.get(UserSessionUtil.WEBSOCKET_STATUS) == "REAL"){
                log.info(">>>>>>>>>>>>>> WebKafkaConsumer info: consumer(" + Thread.currentThread().getName() + ")给用户session(" + userSession.getId() + ")发送数据 <<<<<<<<<<<<<<<<<<<");
                //多线程环境下避免多个线程操作同一个session
                synchronized (userSession){
                    userSession.getBasicRemote().sendText(JSON.toJSONString(recordForEchartMain));
                }
            }


        }
        //更新UserSessionUtil中的预缓存队列
        preparedBufferUtil.updateRealBuffer(waterLevelRecord);

        //提交offset
//        ack.acknowledge();

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

//        backTrackingKafkaConsumer.beginBackTracking();
    }

    /**
     * 关闭/暂停WebKafkaListener
      */
    public void stopWebListener(){
        log.info(">>>>>>>>>>>>>>>>>>>> WebKafkaConsumer info: WebListener.pause <<<<<<<<<<<<<<<<<<<<<<");
        if (registry.getListenerContainer("webListener").isRunning()){
           registry.getListenerContainer("webListener").pause();
//           registry.getListenerContainer("webListener").stop();
        }
    }

    /**
     * 判断WebListener是否在工作
     * @return 当WebListener为running && no pause状态时返回true
     */
    public boolean listenerIsWorking(){
//        log.info("**********************webKafkaConsumer.listenerIsRunning = " + registry.getListenerContainer("webListener").isRunning());
//        log.info("**********************webKafkaConsumer.listenerIsPauseRequested = " + registry.getListenerContainer("webListener").isPauseRequested());
//        log.info("**********************webKafkaConsumer.listenerIsContainerPaused = " + registry.getListenerContainer("webListener").isContainerPaused());

        return registry.getListenerContainer("webListener").isRunning()
                && !registry.getListenerContainer("webListener").isPauseRequested();
    }
}

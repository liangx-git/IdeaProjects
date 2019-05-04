package com.liangx.spring.kafka.services.RealMonitorListenerService;

import com.alibaba.fastjson.JSON;
import com.liangx.spring.kafka.common.MessageEntity;
import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.services.MyKafkaConsumer;
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

    @KafkaListener(id="webListener", clientIdPrefix="web", topics="${kafka.consumer.topic}", containerFactory="webKafkaListenerContainerFactory")
    public void webListener(ConsumerRecord<String, WaterLevelRecord> consumerRecord) {
        log.info("[ WebKafkaConsumer ] : 从kafka监听到数据（partition = " + consumerRecord.partition() + ", offset = " + consumerRecord.offset() + "）");

        //更新UserSessionUtil中的预缓存队列
        preparedBufferUtil.updateRealBuffer(consumerRecord.value());

        //放弃太旧的数据
        if (recordIsToOld(consumerRecord.value())){
            log.info("[ WebKafkaConsumer ] : 丢弃旧数据（" + consumerRecord.value() + ")");
            return;
        }

        //准备数据
        MessageEntity message = new MessageEntity(MessageEntity.REAL_MONITOR, consumerRecord.value());

        //对订阅了RealMonitorListener的用户session发送实时记录
        List<String> userSessionIds = userSessionUtil.getUserSessionIds();
        for (String userSessionId : userSessionIds){
            String userSessionRequestService = userSessionUtil.getUserSessionRequestService(userSessionId);
            if (userSessionRequestService.equals(UserSessionUtil.REAL_MONITOR_SERVICE)){
                userSessionUtil.setUserSessionMessageEntity(userSessionId, message, true);
                log.info("[ WebKafkaConsumer ] : consumer(" + Thread.currentThread().getName() + ")给用户session(" + userSessionId + ")发送数据");
            }else if (userSessionRequestService.equals(UserSessionUtil.REAL_MONITOR_SERVICE_START)){    //WebListener为刚加入监听队列的session推送预缓存队列
                if (preparedBufferUtil.realMonitorPreparedBufferIsReady()){
                    sendPrepraredBuffer(userSessionId);
                    log.info("[ WebKafkaConsumer ] : consumer(" + Thread.currentThread().getName() + ")给用户session(" + userSessionId + ")发送预缓存数据");
                }
                userSessionUtil.setUserSessionRequestService(userSessionId, UserSessionUtil.REAL_MONITOR_SERVICE);
            }
        }
    }

    private boolean recordIsToOld(WaterLevelRecord record){
        long timeMillis = record.getTime().getTime();
        long nowTimeMills = System.currentTimeMillis();
        return timeMillis + 6 * 1000 < nowTimeMills;
    }


    /**
     * 开启webListener监听kafka消息
     */
    public void startWebListener(){
        log.info("[ WebKafkaConsumer ] : WebListener.start");

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
        log.info("[ WebKafkaConsumer ] : WebListener.pause ");
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

    private void sendPrepraredBuffer(String userSessionId){
        Queue<WaterLevelRecord> realBuffer = preparedBufferUtil.getRealBuffer();
        if (!realBuffer.isEmpty()){
            MessageEntity message = new MessageEntity(MessageEntity.REAL_MONITOR, realBuffer);
            userSessionUtil.setUserSessionMessageEntity(userSessionId, message, true);
        }
    }

    private void sendMsg(Session session, String msg){
        try {
            synchronized (session){
                session.getBasicRemote().sendText(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.liangx.spring.kafka.services.RealMonitorService;

import com.liangx.spring.kafka.common.MessageEntity;
import com.liangx.spring.kafka.common.ServiceType;
import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.services.MyKafkaConsumer;
import com.liangx.spring.kafka.utils.PreparedBufferUtil;
import com.liangx.spring.kafka.utils.UserSessionUtil;
import lombok.extern.slf4j.Slf4j;
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

    @Autowired
    private KafkaListenerContainerFactory kafkaListenerContainerFactory;

    @KafkaListener(id="webListener", clientIdPrefix="web", topics="${kafka.consumer.topic}", containerFactory="webKafkaListenerContainerFactory")
    private void webListener(ConsumerRecord<String, WaterLevelRecord> consumerRecord) {
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
        List<String> userSessionIds = userSessionUtil.getSubcribedServicesUserSessionIds(ServiceType.REAL_MONITOR_SERVICE);
        for (String userSessionId : userSessionIds){
            userSessionUtil.setUserSessionMessageEntity(userSessionId, message, true);
            log.info("[ WebKafkaConsumer ] : consumer(" + Thread.currentThread().getName() + ")给用户session(" + userSessionId + ")发送数据");
        }
    }

    /**
     * 为userSession开启RealMonitor服务
     * @param userSessionId
     */
    public void startRealMonitorServiceForUserSession(String userSessionId){
        //启动RealMonitor前，发送预缓存
        sendRealMonitorPreparedBuffer(userSessionId);

        //当WebKafkaConsumer中的Listener线程未启动时启动
        if (!listenerIsWorking()){
            startWebListener();
        }
    }


    /**
     *取消订阅RealMonitorService
     * @param userSessionId
     */
    public void stopRealMonitorServiceForUserSession(String userSessionId) {

        //当前取消订阅的UserSession为最后一个时，真正关闭(暂停)RealMonitorService服务
        if (userSessionUtil.noUserSessionSubscribedService(ServiceType.REAL_MONITOR_SERVICE)){
            stopWebListener();
        }
    }


    private void sendRealMonitorPreparedBuffer(String userSessionId){
        if (preparedBufferUtil.realMonitorPreparedBufferIsReady()) {
            List<WaterLevelRecord> realMonitorPreparedBuffer = preparedBufferUtil.getRealBuffer();
            userSessionUtil.setUserSessionMessageEntity(userSessionId, new MessageEntity(MessageEntity.REAL_MONITOR, realMonitorPreparedBuffer), true);    //sendToFrontEnd设为true表示将缓存立即发送到前端

            log.info("[ RealMonitorService ] : 给用户session(" + userSessionId + ")发送RealPrepreadBuffer");
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
    private void startWebListener(){
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
    private void stopWebListener(){
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
    private boolean listenerIsWorking(){
        return registry.getListenerContainer("webListener").isRunning()
                && !registry.getListenerContainer("webListener").isPauseRequested();
    }

}

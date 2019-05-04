package com.liangx.spring.kafka.services.WebSocketService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.liangx.spring.kafka.common.MessageEntity;
import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.config.GeneralConsumerConfig;
import com.liangx.spring.kafka.services.BackTrackingService.BackTrackingKafkaTask;
import com.liangx.spring.kafka.services.RealMonitorListenerService.WebKafkaConsumer;
import com.liangx.spring.kafka.services.BackTrackingService.BackTrackingServiceManager;
import com.liangx.spring.kafka.utils.ApplicationContextUtil;
import com.liangx.spring.kafka.utils.PreparedBufferUtil;
import com.liangx.spring.kafka.utils.UserSessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

import static com.alibaba.fastjson.JSON.parseArray;

@ServerEndpoint(value = "/webSocket")
@Component
@Slf4j
public class WebSocketServer {

    /**
     * 连接成功回调函数
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        log.info("[ WebSocketService - onOpen() ] : 与Session(" + session.getId() + ")建立连接成功");

        //将新用户session注册到UserSessionUtil中管理
        UserSessionUtil userSessionUtil = getUserSessionUtil();
        userSessionUtil.registerUserSession(session, UserSessionUtil.REAL_MONITOR_SERVICE_START);
        userSessionUtil.setUserSessionMessageEntity(session.getId(), new MessageEntity(MessageEntity.REAL_MONITOR));

        //当WebListener未开启时开启或者开启了暂停时恢复
        WebKafkaConsumer webKafkaConsumer = getWebKafkaConsumer();
        if (!webKafkaConsumer.listenerIsWorking()){
            webKafkaConsumer.startWebListener();
        }
    }

    /**
     * 断开连接时调用
     */
    @OnClose
    public void onClose(Session session) {
        log.info("[ WebSocketService - onClose() ] : 与session(" + session.getId() + ")断开连接");

        //当用户断开WebSocket连接时，UserSessionUtil将不再管理其session，WebListener也将不再推送信息给该session
        UserSessionUtil userSessionUtil = getUserSessionUtil();
        userSessionUtil.unregisterUserSession(session.getId());

        //当前用户为最后连接的用户时才能关闭WebKafkaListener
        WebKafkaConsumer webKafkaConsumer = getWebKafkaConsumer();
        if (userSessionUtil.userSessionsIsEmpty()){
            webKafkaConsumer.stopWebListener();
        }
    }

    /**
     * 收到客户端发送的消息回调函数
     */
    @OnMessage
    public void onMessage(String message, Session session){
        log.info("[ WebSocketService - onMessage() ] : 与session(" + session.getId() + ")断开连接");

        JSONArray jsonArray = JSON.parseArray(message);
        String type = (String)jsonArray.get(0);
        if (type.equals(MessageEntity.BACK_TRACKING)){  //用户请求BackTrackingService
//            log.info("[ WebSocketServer ] : BACK_TRACKING");
            //将用户操作信息保存到userSessionUtil中管理
            long timeMillis = (long)jsonArray.get(1);
            UserSessionUtil userSessionUtil = getUserSessionUtil();
            userSessionUtil.setUserSessionMessageEntity(session.getId(), new MessageEntity(MessageEntity.BACK_TRACKING, timeMillis));

            //如果该用户未开启BackTrackingListener服务，则开启
            BackTrackingServiceManager backTrackingServiceManager = getBackTrackingServiceManager();
            if (!backTrackingServiceManager.backTrackingListenerForSessionIsStart(session.getId())){
                backTrackingServiceManager.startBackTrackingListener(session.getId());
            }
        }else if (type.equals(MessageEntity.BACK_TRACKING_DONE)){   //用户关闭BackTrackingService
//            log.info("[ WebSocketServer ] : BACK_TRACKING_DONE");
            BackTrackingServiceManager backTrackingServiceManager = getBackTrackingServiceManager();
            backTrackingServiceManager.stopBackTrackingListener(session.getId());

        } else if (type.equals(MessageEntity.DAILY_MONITOR)){
            log.info(">>>>>>>>>> daily_monitor <<<<<<<");
            sendPreparedBufferRecordsToDailyChart(session);
        } else if (type.equals(MessageEntity.WEEKLY_MONITOR)){
            log.info(">>>>>>>>> weekly_monitor <<<<<<<<<");
            sendPreparedBufferRecordsToWeeklyChart(session);
        }
    }

    /**
     * 错误时回调函数
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
        log.info("[ WebSocketService-onError ] : 与Session(" + session.getId() + ")链接发生错误");
//        getUserSessionUtil().unregisterUserSession(session.getId());
    }

    private void sendPrepraredBufferRecordsToRealChart(Session session){
        Queue<WaterLevelRecord> realBuffer = getPreparedBufferUtil().getRealBuffer();
        if (!realBuffer.isEmpty()){
            log.info("[ WebSocketService-onOpen ] : 发送Real preparedBuffer");
            List<Object> msg = new ArrayList<>();
            msg.add(MessageEntity.REAL_MONITOR);
            msg.add(realBuffer);
            String msgStr = JSON.toJSONString(msg);
            sendMsg(session, msgStr);
        }
    }

    private void sendPreparedBufferRecordsToDailyChart(Session session){
        //hourly表格buffer
        List<WaterLevelRecord>  hourlyBuffer = getPreparedBufferUtil().getHourlyBuffer();
        if (!hourlyBuffer.isEmpty()){
            log.info("[ WebSocketService-onOpen] : 发送hourly preparedBuffer : " + hourlyBuffer);
            List<Object> msg = new ArrayList<>();
            msg.add(MessageEntity.DAILY_MONITOR);
            msg.add(hourlyBuffer);
            String msgStr = JSON.toJSONString(msg);
            sendMsg(session, msgStr);
        }
    }

    private void sendPreparedBufferRecordsToWeeklyChart(Session session){
        //weekly表格buffer
        List<WaterLevelRecord>  weeklyBuffer = getPreparedBufferUtil().getWeeklyBuffer();
        if (!weeklyBuffer.isEmpty()){
            log.info("[WebSocketService-onOpen ] : 发送weekly preparedBuffer : " + weeklyBuffer);
            List<Object> msg = new ArrayList<>();
            msg.add(MessageEntity.WEEKLY_MONITOR);
            msg.add(weeklyBuffer);
            String msgStr = JSON.toJSONString(msg);
            sendMsg(session, msgStr);
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


    /**
     * 由于@ServerEndpoint中不能通过@Autowired方式自动装载类
     * 所以通过ApplicationContextAware方式获取UserSessionUtil和WebKafkaConsumer
     */
    private UserSessionUtil getUserSessionUtil(){
        return (UserSessionUtil)ApplicationContextUtil.getApplicationContext().getBean("userSessionUtil");
    }

    private WebKafkaConsumer getWebKafkaConsumer(){
        return (WebKafkaConsumer)ApplicationContextUtil.getApplicationContext().getBean("webKafkaConsumer");
    }

    private BackTrackingKafkaTask getBackTrackingKafkaConsumer(){
        return (BackTrackingKafkaTask)ApplicationContextUtil.getApplicationContext().getBean("backTrackingKafkaConsumer");
    }

    private PreparedBufferUtil getPreparedBufferUtil(){
        return (PreparedBufferUtil)ApplicationContextUtil.getApplicationContext().getBean("preparedBufferUtil");
    }

    private GeneralConsumerConfig getGeneralConsumerConfig(){
        return (GeneralConsumerConfig)ApplicationContextUtil.getApplicationContext().getBean("generalConsumerConfig");
    }

    private BackTrackingServiceManager getBackTrackingServiceManager(){
        return (BackTrackingServiceManager)ApplicationContextUtil.getApplicationContext().getBean("consumerThreadPool");
    }
}

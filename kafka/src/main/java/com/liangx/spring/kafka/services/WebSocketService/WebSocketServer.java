package com.liangx.spring.kafka.services.WebSocketService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.liangx.spring.kafka.common.MessageEntity;
import com.liangx.spring.kafka.common.ServiceType;
import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.config.GeneralConsumerConfig;
import com.liangx.spring.kafka.services.BackTrackingService.BackTrackingKafkaTask;
import com.liangx.spring.kafka.services.RealMonitorService.WebKafkaConsumer;
import com.liangx.spring.kafka.services.BackTrackingService.BackTrackingServiceManager;
import com.liangx.spring.kafka.utils.ApplicationContextUtil;
import com.liangx.spring.kafka.utils.PreparedBufferUtil;
import com.liangx.spring.kafka.utils.UserSessionUtil;
import com.sun.corba.se.impl.activation.ServerTableEntry;
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
        userSessionUtil.register(session);
//        userSessionUtil.register(session,
//                ServiceType.REAL_MONITOR_SERVICE        //订阅实时监控服务
//                        | ServiceType.DAILY_MONITOR_SERVICE     //订阅日监控服务
//                        | ServiceType.WEEKLY_MONITOR_SERVICE);  //订阅周监控服务
    }

    /**
     * 断开连接时调用
     */
    @OnClose
    public void onClose(Session session) {
        log.info("[ WebSocketService - onClose() ] : 与session(" + session.getId() + ")断开连接");

        //当用户断开WebSocket连接时，UserSessionUtil将不再管理其session，WebListener也将不再推送信息给该session
        UserSessionUtil userSessionUtil = getUserSessionUtil();
        userSessionUtil.unregister(session.getId());
    }

    /**
     * 收到客户端发送的消息回调函数
     */
    @OnMessage
    public void onMessage(String message, Session session){
        log.info("[ WebSocketService - onMessage() ] : 收到session(" + session.getId() + ")请求");

        UserSessionUtil userSessionUtil = getUserSessionUtil();
        int services = userSessionUtil.getSubscribedServices(session.getId());

        JSONArray msgArray = JSON.parseArray(message);
        String type = (String)msgArray.get(0);
        log.info("[ WebSocketService] : request requestType = " + type);

        switch (type){
            case MessageEntity.SUBSCRIBE_REAL_MONITOR_SERVICE :   //请求实时监控数据
            {
                services |= ServiceType.REAL_MONITOR_SERVICE;
                userSessionUtil.subscribeService(session.getId(), services);
                break;
            }
            case MessageEntity.SUBSCRIBE_BACK_TRACKING_SERVICE :
            {
                services |= ServiceType.BACK_TRACKING_SERVICE;      //订阅BackTrackingServices
                if ((services & ServiceType.REAL_MONITOR_SERVICE) != 0) {  //取消订阅RealMonitor服务
                    services &= ~ServiceType.REAL_MONITOR_SERVICE;
                }
                userSessionUtil.subscribeService(session.getId(), services);
                break;
            }
            case MessageEntity.BACK_TRACKING :  //请求回溯监控数据
            {
                //保存用户请求
                userSessionUtil.setUserSessionMessageEntity(
                        session.getId(),
                        new MessageEntity(MessageEntity.BACK_TRACKING,
                        (long)msgArray.get(1))     //请求的时间戳
                );
                break;
            }
            case MessageEntity.UNSUBSCRIBE_BACK_TRACKING_SERVICE :     //取消订阅BackTrackingService，重新加入RealMonitorService
            {
                services &= ~ServiceType.BACK_TRACKING_SERVICE;    //关闭BackTrackingService服务
                services |= ServiceType.REAL_MONITOR_SERVICE;     //开启RealMonitorService服务
                userSessionUtil.subscribeService(session.getId(), services);
                break;
            }
            case MessageEntity.SUBSCRIBE_DAILY_MONITOR_SERVICE :  //订阅日监控服务
            {
                services |= ServiceType.DAILY_MONITOR_SERVICE;
                userSessionUtil.subscribeService(session.getId(), services);
                break;
            }
            case MessageEntity.SUBSCRIBE_WEEKLY_MONITOR_SERVICE : //订阅周监控缓服务
            {
                services |= ServiceType.WEEKLY_MONITOR_SERVICE;
                userSessionUtil.subscribeService(session.getId(), services);
                break;
            }
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

        //将连接出错的session取消注册
        UserSessionUtil userSessionUtil = getUserSessionUtil();
        userSessionUtil.unregister(session.getId());
    }

    private void sendPrepraredBufferRecordsToRealChart(Session session){
        List<WaterLevelRecord> realBuffer = getPreparedBufferUtil().getRealBuffer();
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

    private PreparedBufferUtil getPreparedBufferUtil(){
        return (PreparedBufferUtil)ApplicationContextUtil.getApplicationContext().getBean("preparedBufferUtil");
    }

}

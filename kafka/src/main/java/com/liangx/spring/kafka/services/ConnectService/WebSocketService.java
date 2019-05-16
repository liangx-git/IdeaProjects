package com.liangx.spring.kafka.services.ConnectService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.liangx.spring.kafka.common.MessageEntity;
import com.liangx.spring.kafka.services.Manager.ServicesManager;
import com.liangx.spring.kafka.utils.IocUtil;
import com.liangx.spring.kafka.services.Manager.UserManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;


@ServerEndpoint(value = "/webSocket")
@Component
@Slf4j
public class WebSocketService {


    /**
     * 连接成功回调函数
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        log.info("[ WebSocketService - onOpen() ] : 与Session(" + session.getId() + ")建立连接成功");

        //将新用户session注册到UserSessionUtil中管理
        UserManager userManager = getUserManager();
        userManager.registerUser(session);
    }

    /**
     * 断开连接时调用
     */
    @OnClose
    public void onClose(Session session) {
        log.info("[ WebSocketService - onClose() ] : 与session(" + session.getId() + ")断开连接");

        //当用户断开WebSocket连接时，UserSessionUtil将不再管理其session，WebListener也将不再推送信息给该session
        UserManager userManager = getUserManager();
        userManager.unregisterUser(session.getId());
    }

    /**
     * 收到客户端发送的消息回调函数
     */
    @OnMessage
    public void onMessage(String message, Session session){

        JSONArray msgArray = JSON.parseArray(message);
        String type = (String)msgArray.get(0);
        log.info("[ WebSocketService - onMessage() ] : 收到session(" + session.getId() + ")请求(request type : " + type);

        UserManager userManager = getUserManager();
        ServicesManager servicesManager = getServicesManager();

        if (type.equals(MessageEntity.BACK_TRACKING)){  //回溯服务请求包含请求的时间戳
            userManager.setUserSessionMessageEntity(
                    session.getId(),
                    new MessageEntity(MessageEntity.BACK_TRACKING,
                            (long)msgArray.get(1))     //请求的时间戳
            );
        } else {    //订阅服务请求
            servicesManager.subscribeService(session.getId(), type);
        }


//        switch (type){
//            case MessageEntity.SUBSCRIBE_REAL_MONITOR_SERVICE :   //请求实时监控数据
//            {
//                userManager.subscribeService_(session.getId(), "realMonitorService");
//                break;
//            }
//            case MessageEntity.SUBSCRIBE_BACK_TRACKING_SERVICE :
//            {
//                userManager.unsubscribeService_(session.getId(), "realMonitorService");
//                userManager.subscribeService_(session.getId(), "backTrackingService");
//                break;
//            }
//            case MessageEntity.BACK_TRACKING :  //请求回溯监控数据
//            {
//                //保存用户请求
//                userManager.setUserSessionMessageEntity(
//                        session.getId(),
//                        new MessageEntity(MessageEntity.BACK_TRACKING,
//                                (long)msgArray.get(1))     //请求的时间戳
//                );
//                break;
//            }
//            case MessageEntity.UNSUBSCRIBE_BACK_TRACKING_SERVICE :     //取消订阅BackTrackingService，重新加入RealMonitorService
//            {
//                userManager.unsubscribeService_(session.getId(), "backTrackingService");
//                userManager.subscribeService_(session.getId(), "realMonitorService");
//                break;
//            }
//            case MessageEntity.SUBSCRIBE_SCHEDULED_MONITOR_SERVICE : //订阅周监控缓服务
//            {
//                userManager.subscribeService_(session.getId(), "scheduledMonitorService");
//                break;
//            }
//        }

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
        UserManager userManager = getUserManager();
        userManager.unregisterUser(session.getId());
    }


    /**
     * 由于@ServerEndpoint中不能通过@Autowired方式自动装载类
     * 所以通过ApplicationContextAware方式获取UserSessionUtil和WebKafkaConsumer
     */
    private UserManager getUserManager(){
        return (UserManager) IocUtil.getApplicationContext().getBean("userManager");
    }

    private ServicesManager getServicesManager(){
        return (ServicesManager) IocUtil.getApplicationContext().getBean("servicesManager");
    }
}

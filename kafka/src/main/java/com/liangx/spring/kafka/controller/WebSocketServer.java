package com.liangx.spring.kafka.controller;

import com.alibaba.fastjson.JSON;
import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.config.WebSocketConfig;
import com.liangx.spring.kafka.consumer.Impl.WebKafkaConsumer;
import com.liangx.spring.kafka.utils.ApplicationContextUtil;
import com.liangx.spring.kafka.utils.UserSessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.SpringConfigurator;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

@ServerEndpoint(value = "/webSocket")
@Component
@Slf4j
public class WebSocketServer {

    /**
     * 连接成功回调函数
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        log.info(">>>>>>>>>>>>>>>>> webSocket info: onOpen() 与Session(" + session.getId() + ")建立连接成功 <<<<<<<<<<<<<<<<<<<<");

        //将用户session加入到UserSessionUtil中管理
        UserSessionUtil userSessionUtil = getUserSessionUtil();
        userSessionUtil.addUserSession(session);
        //当缓存队列可用时，推送给新建立连接用户
        Queue<WaterLevelRecord> prepareBufferRecords = userSessionUtil.getPrepareRecords();
        if (!prepareBufferRecords.isEmpty()){
            session.getBasicRemote().sendText(JSON.toJSONString(prepareBufferRecords));
        }

        //当WebListener未开启时开启或者开启了暂停时恢复
        WebKafkaConsumer webKafkaConsumer = getWebKafkaConsumer();
        if (!webKafkaConsumer.listenerIsWorking()){
            webKafkaConsumer.startWebListener();
        }
        //当WebListener启动但被暂停事resume

//        log.info("**********************webKafkaConsumer.listenerIsRunning = " + webKafkaConsumer.listenerIsRunning());
    }

    /**
     * 断开连接时调用
     */
    @OnClose
    public void onClose(Session session) {
        log.info(">>>>>>>>>>>>>>>> webSocket info: onClose() 与session(" + session.getId() + ")断开连接 <<<<<<<<<<<<<<<<<<<<<<<,");

        //当用户断开WebSocket连接时，UserSessionUtil将不再管理其session，WebListener也将不再推送信息给该session
        UserSessionUtil userSessionUtil = getUserSessionUtil();
        userSessionUtil.removeUserSession(session.getId());

        //当前用户为最后连接的用户时才能关闭WebKafkaListener
        WebKafkaConsumer webKafkaConsumer = getWebKafkaConsumer();
        if (!userSessionUtil.hasUserSession()){
            webKafkaConsumer.stopWebListener();
        }
    }

    /**
     * 收到客户端发送的消息回调函数
     */
    @OnMessage
    public void onMessage(String message, Session session){
        log.info(">>>>>>>>>>>>>>>>>>>> webSocket info: onMessage() 收到Session(" + session.getId() + "消息 <<<<<<<<<<<<<<<<<<<<<");
    }

    /**
     * 错误时回调函数
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
        log.info(">>>>>>>>>>>>>>>>> webSocket info: onError() 与Session(" + session.getId() + ")链接发生错误 <<<<<<<<<<<<<<<<<<<<");

//        getUserSessionUtil().removeUserSession(session.getId());
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
}

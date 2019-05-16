package com.liangx.spring.kafka.services.Manager;

import com.liangx.spring.kafka.common.MessageEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.*;



/**
 * 管理通过WebSocket连接的用户的session
 */
@Component
@Slf4j
public class UserManager {

    @Autowired
    private ServicesManager servicesManager;

    private List <Session> userSessions = new ArrayList<>();    //保存所有注册的session信息,以及session请求的服务
    private Map<String, MessageEntity> userSessionInformationMap = new HashMap<>();


    /**
     * 将新用户session注册到UserSessionUtil
     * @param userSession : 新用户session
     */
    public synchronized void registerUser(Session userSession){
        userSessions.add(userSession);
        userSessionInformationMap.put(userSession.getId(), new MessageEntity());
        log.info("[ Manager ] : user session(" + userSession.getId() + ") registerUser, current user number: (" + userSessions.size() + ")");
    }


    /**
     * 将用户session从UserSessionUtil取消注册
     * @param userSessionId
     */
    public synchronized void unregisterUser(String userSessionId){
        Session userSession = getUserSessionById(userSessionId);
        if (userSession != null){   //清除userSessions和userSessionInformationMap中和userSessionId相关的信息
            userSessions.remove(userSession);
            userSessionInformationMap.remove(userSessionId);

            //注销userSessionId订阅的所有服务
            servicesManager.unsubscribeAllService(userSessionId);
            log.info("[ Manager ] : user session(" + userSessionId + ") unregisterUser, current user number: (" + userSessions.size() + ")");
        }
    }


    /**
     * 几乎所有service都是通过该方法给前端发送数据的
     * @param userSessionId
     * @param messageEntity
     * @param sendToFrontEnd true时，立即给当前session发送消息
     */
    public synchronized void setUserSessionMessageEntity(String userSessionId, MessageEntity messageEntity, boolean sendToFrontEnd){

        userSessionInformationMap.put(userSessionId, messageEntity);

        if (sendToFrontEnd){
            sendMessage(userSessionId);
        }

    }


    public synchronized void setUserSessionMessageEntity(String sessionId, MessageEntity messageEntity){
        setUserSessionMessageEntity(sessionId, messageEntity, false);
    }


    public MessageEntity getUserSessionMessageEntity(String sessionId){
        return userSessionInformationMap.get(sessionId);
    }


    private Session getUserSessionById(String userSessionId){
        for (Session session : userSessions){
            if ((session.getId()).equals(userSessionId)){
               return session;
            }
        }
        return null;
    }

    /**
     * 将sessionId对应的MessageEntity对象发送给前端
     * @param userSessionId
     */
    private void sendMessage(String userSessionId){
        Session userSession = getUserSessionById(userSessionId);
        try {
            if (userSession != null && userSession.isOpen()){
                synchronized (userSession){
                    MessageEntity message = userSessionInformationMap.get(userSessionId);
                    userSession.getBasicRemote().sendText(message.getJsonStr());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

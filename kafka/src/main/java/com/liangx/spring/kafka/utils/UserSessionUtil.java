package com.liangx.spring.kafka.utils;

import com.liangx.spring.kafka.common.MessageEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.*;

/**
 * 管理通过WebSocket连接的用户的session
 */
@Component
@Slf4j
public class UserSessionUtil {

    public final static String REAL_MONITOR_SERVICE = "real_monitor_service";
    public final static String REAL_MONITOR_SERVICE_START = "real_monitor_service_start";
    public final static String BACK_TRACKING_SERVICE = "back_tracking_service";

    //保存所有注册的session信息,以及session请求的服务
//    private List<Session> userSessions = new ArrayList<>();
    private Map<Session, String> userSessions = new HashMap<>();

    //保存session对应的消息缓存MessageEntity
    private Map<String, MessageEntity> userSessionsMessageEntity = new HashMap<>();


    /**
     * 将新用户session注册到UserSessionUtil
     * @param userSession : 新用户session
     * @param service : 请求的服务
     */
    public synchronized void registerUserSession(Session userSession, String service){
        userSessions.put(userSession, service);
        log.info("[ UserSessionUtil ] : add user session(" + userSession.getId() + ", " + service + "), current user number: (" + userSessions.size() + ")");
    }


    /**
     * 重载函数，默认请求"REAL_MONITOR_SERVICE"服务
     * @param userSession
     */
    public synchronized void registerUserSession(Session userSession){
        userSessions.put(userSession, REAL_MONITOR_SERVICE);
        log.info("[ UserSessionUtil ] : add user session(" + userSession.getId() + ", " + "REAL_MONITOR_SERVICE" + "), current user number: (" + userSessions.size() + ")");
    }


    /**
     * 将用户session从UserSessionUtil取消注册
     * @param userSessionId
     */
    public synchronized void unregisterUserSession(String userSessionId){
        Session userSessionToDelete = getUserSessionBySessionId(userSessionId);
        if (userSessionToDelete != null){
            userSessions.remove(userSessionToDelete);
            log.info("[ UserSessionUtil ] : remove user session(" + userSessionId + "), current user number: (" + userSessions.size() + ")");
        }
    }


    public String getUserSessionRequestService(String userSessionId){
        Session userSession = getUserSessionBySessionId(userSessionId);
        return userSessions.get(userSession);
    }

    public synchronized void setUserSessionRequestService(String userSessionId, String requestService){
        Session userSession = getUserSessionBySessionId(userSessionId);
        userSessions.put(userSession, requestService);
    }


    public List<String> getUserSessionIds(){
        List<String> userSessionIds = new ArrayList<>();
        for (Session userSession : userSessions.keySet()){
            log.info("Session = " + userSession);
            userSessionIds.add(userSession.getId());
        }
        return userSessionIds;
    }


    public synchronized boolean userSessionsIsEmpty(){
        return userSessions.isEmpty();
    }


    public synchronized void setUserSessionMessageEntity(String sessionId, MessageEntity messageEntity, boolean sendToFrontEnd){
//        String oldType = userSessionsMessageEntity.get(sessionId).getType();
//        if (oldType != messageEntity.getType()){
//
//        }
        userSessionsMessageEntity.put(sessionId, messageEntity);

//        String type = messageEntity.getType();

        //每次更新MessageEntity对象，如果sendToFrontEnd为true表明是后端更新，会主动将MessageEntity对象发送到前端
        if (sendToFrontEnd){
            sendMessage(sessionId);
        }
    }


    public synchronized void setUserSessionMessageEntity(String sessionId, MessageEntity messageEntity){
        setUserSessionMessageEntity(sessionId, messageEntity, false);
    }


    public MessageEntity getUserSessionMessageEntity(String sessionId){
        return userSessionsMessageEntity.get(sessionId);
    }


    /**
     * 查看userSessionId对应的session是否在UserSessionUtil中注册
     * @param userSessionId
     * @return
     */
    public boolean userSessionIsRegistered(String userSessionId){
       return (getUserSessionBySessionId(userSessionId) != null);
    }


    private Session getUserSessionBySessionId(String userSessionId){
        for (Session session : userSessions.keySet()){
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
        Session userSession = getUserSessionBySessionId(userSessionId);
        MessageEntity message = userSessionsMessageEntity.get(userSessionId);
        try {
            if (userSession != null && userSession.isOpen()){
                synchronized (userSession){
                    userSession.getBasicRemote().sendText(message.getJsonStr());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    public static void sendMessage(Session userSession, MessageEntity messageEntity){
//
//    }


}

package com.liangx.spring.kafka.utils;

import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.consumer.Impl.WebKafkaConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.*;

/**
 * 管理通过WebSocket连接的用户的session
 */
@Component
@Slf4j
public class UserSessionUtil {

    public final static String WEBSOCKET_STATUS = "session_status";
    public final static String BEGIN_TIMESTAMP = "begin_timestamp";

    //保存当前正在连接用户的session
//    private List<Session> userSessions = new ArrayList<Session>();
    private Map<Session, Map<String, String>> userSessionsInfo = new HashMap<>();

    @Autowired
    private WebKafkaConsumer webKafkaConsumer;

    public synchronized void addUserSession(Session session){
        Map<String, String> sessionProps = new HashMap<>();
        sessionProps.put(WEBSOCKET_STATUS, "REAL");
        userSessionsInfo.put(session, sessionProps);   //新加session默认为real态
        log.info(">>>>>>>>>>>>>>> UserSessionUtil info: add user session(" + session.getId() + ") , current user number: (" + userSessionsInfo.size() + ") <<<<<<<<<<<<<<<<<<<<");
    }

    public synchronized void removeUserSession(String id){
        for (Session userSession : userSessionsInfo.keySet()){
            if (userSession.getId() == id){
               userSessionsInfo.remove(userSession);
            }
        }
        log.info(">>>>>>>>>>>>>>> UserSessionUtil info: remove user session(" + id + "), current user number: (" + userSessionsInfo.size() + ")  <<<<<<<<<<<<<<<<<<<<");
    }

    public synchronized Map<Session, Map<String, String>> getUserSessions(){
        return userSessionsInfo;
    }

    public synchronized void updateUserSessionState(Session userSession, Map<String, String> sessionProps){
        if (userSessionsInfo.containsKey(userSession)){
            userSessionsInfo.put(userSession, sessionProps);
        }
    }

    public synchronized boolean hasUserSession(){
//        checkUserSession();
        return !userSessionsInfo.isEmpty();
    }

}

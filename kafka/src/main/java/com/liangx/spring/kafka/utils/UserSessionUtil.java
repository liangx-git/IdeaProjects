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

    //保存当前正在连接用户的session
    private List<Session> userSessions;

    @Autowired
    private WebKafkaConsumer webKafkaConsumer;

    private void checkUserSession(){
        if (null == userSessions){
            userSessions = new ArrayList<Session>();
        }
    }

    public synchronized void addUserSession(Session session){
        checkUserSession();
        userSessions.add(session);
        log.info(">>>>>>>>>>>>>>> UserSessionUtil info: add user session(" + session.getId() + ") , current user number: (" + userSessions.size() + ") <<<<<<<<<<<<<<<<<<<<");
    }

    public synchronized void removeUserSession(String id){
        checkUserSession();
        Iterator<Session> it = userSessions.iterator();
        while(it.hasNext()){
            if (id == (it.next()).getId()){
               it.remove();
            }
        }
        log.info(">>>>>>>>>>>>>>> UserSessionUtil info: remove user session(" + id + "), current user number: (" + userSessions.size() + ")  <<<<<<<<<<<<<<<<<<<<");
    }

    public synchronized List<Session> getUserSessions(){
        checkUserSession();
        return userSessions;
    }

    public synchronized boolean hasUserSession(){
        checkUserSession();
        return !userSessions.isEmpty();
    }

}

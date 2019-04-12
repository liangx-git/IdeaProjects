package com.liangx.spring.kafka.utils;

import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.consumer.Impl.WebKafkaConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.*;

@Component
@Slf4j
public class UserSessionUtil {

    //保存当前正在连接用户的session
    private List<Session> userSessions;

    //数据预缓存队列，只在session连接时推送一次，用于填充前端的图表
    private Queue<WaterLevelRecord> prepareRecords; //为刚建立连接用户提供的缓存数据，用于填充图表

    @Autowired
    private WebKafkaConsumer webKafkaConsumer;

    private void checkUserSession(){
        if (null == userSessions){
            userSessions = new ArrayList<Session>();
        }
    }

    private void checkPrepareRecords(){
        if (null == prepareRecords){
            prepareRecords = new LinkedList<WaterLevelRecord>();
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

    /**
     * 更新预缓存队列数据，在WebListener中被调用
     * @param waterLevelRecord
     */
    public void updatePrepareRecords(WaterLevelRecord waterLevelRecord){
        checkPrepareRecords();

        prepareRecords.offer(waterLevelRecord);
        //当队列达到上限（与前端图表x轴刻度对应），队头出队
        if (11 == prepareRecords.size()){
            prepareRecords.poll();
        }
    }

    /**
     * 获取预缓存队列数据
     * @return
     */
    public Queue<WaterLevelRecord> getPrepareRecords(){
        checkPrepareRecords();

        return prepareRecords;
    }

}

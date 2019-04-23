package com.liangx.spring.kafka.consumer.Impl;

import com.alibaba.fastjson.JSON;
import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.common.WaterLevelRecordDeserializer;
import com.liangx.spring.kafka.config.GeneralConsumerConfig;
import com.liangx.spring.kafka.consumer.MyKafkaConsumer;
import com.liangx.spring.kafka.utils.UserSessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.internals.Topic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.stereotype.Component;
import sun.java2d.loops.FillRect;

import javax.websocket.Session;
import java.io.IOException;
import java.nio.file.Watchable;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;

//@Component("backTrackingKafkaConsumer")
@Slf4j
public class BackTrackingKafkaConsumer implements Runnable, MyKafkaConsumer{

    private UserSessionUtil userSessionUtil;

//    @Autowired
//    private WebKafkaConsumer webKafkaConsumer;

    //    private Session userSession;
    private Queue<Session> Sessions = new LinkedList<>();

    private GeneralConsumerConfig generalConsumerConfig;

    //构造函数
    public BackTrackingKafkaConsumer(UserSessionUtil userSessionUtil, GeneralConsumerConfig generalConsumerConfig){
        this.userSessionUtil = userSessionUtil;
        this.generalConsumerConfig = generalConsumerConfig;
    }

    public void addUserSession(Session session){
        Sessions.add(session);
    }

    @Override
    public void run() {
        //获取用户session
        Session userSession = Sessions.poll();

        //新建BackTrackingListener线程
        KafkaConsumer<String, WaterLevelRecord> consumer = new KafkaConsumer<>(generalConsumerConfig.getGeneralConsumerConfigs());
        consumer.subscribe(Arrays.asList(generalConsumerConfig.getTopic()));
        consumer.poll(Duration.ofMillis(1000));     //由于未知原因，只有在调用poll之后BackTrackingListener才会自动分配分区

        Calendar calendar = Calendar.getInstance();
        Long stopTimeInMillis = null;
        //循环监听用户session变化获取时间戳并设置offset，以此获取相应消息
        while (true){
            Map<String, String> sessionProps = userSessionUtil.getUserSessions().get(userSession);
            if (sessionProps.get(UserSessionUtil.WEBSOCKET_STATUS) == "BACK"){
                //表示该次请求操作被处理
                updateSessionStatus(userSession, "BACK_PROCESSING");

                //根据timestamp设置各分区的offset
                Timestamp beginTimestamp = new Timestamp((Long) JSON.parse(sessionProps.get(UserSessionUtil.BEGIN_TIMESTAMP)));
                updateConsumerOffsetByTimestamp(consumer, beginTimestamp);

                //获取当前offset位置数据
                List<Object> msg = new ArrayList<>();
                msg.add("REAL");
                msg.add(getRecordByConsumerPoll(consumer));
                //返回前端数据
                sendMsgBySession(userSession, msg);

                //设置用户下次操作超时时间
                calendar.add(Calendar.SECOND, 5);
                stopTimeInMillis = calendar.getTimeInMillis();
           }
           else{    //轮询查看Session状态
                log.info(">>>>>>>>>>>>>>> BackTrackingListener info : 开始轮询 <<<<<<<<<<<<<<");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sessionProps = userSessionUtil.getUserSessions().get(userSession);
           }

           //用户超时，关闭BackTrackingListener,重新将当前session的WEBSOCKET_STATUS属性置为"REAL"，即当前session重新加入WebSocketListener监听队列
           if (stopTimeInMillis <= System.currentTimeMillis()){
               log.info(">>>>>>>>>>>>>>>>> BackTrackingListener info : 用户超时 <<<<<<<<<<<<<<<<<");
               //用户超时时跳出while(true)循环终止BackTrackingListener，当前session重新加入WebListener消息广播队列
               updateSessionStatus(userSession, "REAL");
               break;
           }
        }

    }

    private void sendMsgBySession(Session userSession, List<Object> msg){
        try {
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>> backTrackingConsumer info : 推送消息给Session(" + userSession.getId() + ") " + msg + "<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            synchronized (userSession){
                userSession.getBasicRemote().sendText(JSON.toJSONString(msg));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateSessionStatus(Session userSession, String newStatus){
        Map<String, String> sessionProps = userSessionUtil.getUserSessions().get(userSession);
        sessionProps.put(UserSessionUtil.WEBSOCKET_STATUS, newStatus);
        userSessionUtil.updateUserSessionState(userSession, sessionProps);
    }

    private void updateConsumerOffsetByTimestamp(Consumer<String, WaterLevelRecord> consumer, Timestamp timestamp){
        //获取当前consumer订阅topic分配的分区信息
        List<PartitionInfo> partitionInfos = consumer.partitionsFor(generalConsumerConfig.getTopic());
        Map<TopicPartition, Long> timestampToSearch = new HashMap<>();
        for (PartitionInfo partitionInfo : partitionInfos){
            timestampToSearch.put(new TopicPartition(generalConsumerConfig.getTopic(), partitionInfo.partition()), timestamp.getTime());
        }
        Map<TopicPartition, OffsetAndTimestamp> offsetAndTimestampMap = consumer.offsetsForTimes(timestampToSearch);
        //修改分区offset
        for (TopicPartition topicPartition : offsetAndTimestampMap.keySet()){
            Long offset = offsetAndTimestampMap.get(topicPartition).offset();
            log.info("topic = " + topicPartition.topic() + " partition = " + topicPartition.partition() +  " offset = " + offset);
            consumer.seek(topicPartition, offset);
        }
    }

    private List<WaterLevelRecord> getRecordByConsumerPoll(Consumer<String, WaterLevelRecord> consumer){
        //拉取各分区中的消息,保存到缓存队列waterRecordBuffer中
        List<WaterLevelRecord> waterLevelRecords = new ArrayList<>();
        ConsumerRecords<String, WaterLevelRecord> records = consumer.poll(Duration.ofMillis(1000));
        int count = 0;
        for (ConsumerRecord<String, WaterLevelRecord> record : records){
            waterLevelRecords.add(record.value());
            if (++count == 10)
                break;
        }
        return waterLevelRecords;
    }

}

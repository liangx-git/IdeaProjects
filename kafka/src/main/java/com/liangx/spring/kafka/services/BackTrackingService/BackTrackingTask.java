package com.liangx.spring.kafka.services.BackTrackingService;

import com.liangx.spring.kafka.common.MessageEntity;
import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.config.GeneralConsumerConfig;
import com.liangx.spring.kafka.services.UserSessionManager.UserSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;

@Component("backTrackingKafkaConsumer")
@Slf4j
public class BackTrackingTask implements Runnable{

    @Autowired
    private UserSessionManager userSessionManager;

    @Autowired
    private BackTrackingService backTrackingService;

    @Autowired
    private GeneralConsumerConfig generalConsumerConfig;

    private Queue<String> userSessionIds = new LinkedList<>();

    public void addUserSession(String sessionId){
        userSessionIds.add(sessionId);
    }

    @Override
    public void run() {
        log.info("[ BackTrackingService ] : 启动新的BackTrackingKafkaConsumer线程(" + Thread.currentThread().getThreadGroup() + ")");
        //拉取sessionId开始处理
        String userSessionId = userSessionIds.poll();

        //新建BackTrackingListener线程
        String consumerName = "consumer-" + Thread.currentThread().getName();
        KafkaConsumer<String, WaterLevelRecord> consumer = createNewConsumer(consumerName);
        log.info("[ BackTrackingService ] : 启动新的消费者进程： consumer(" + consumerName + ")");

        Long stopTimeInMillis = null;

        //循环监听用户session变化获取时间戳并设置offset，以此获取kafka中的历史消息
        while (true){

            //获取session当前传递的MessageEntity对象
            MessageEntity message = userSessionManager.getUserSessionMessageEntity(userSessionId);
            if ((message.getRequestType()).equals(MessageEntity.BACK_TRACKING)){

                //根据timestamp设置各分区的offset
//                Timestamp beginTimestamp = new Timestamp((Long) JSON.parse(sessionProps.get(UserSessionUtil.REQUEST_TIME)));
                Timestamp beginTimestamp = new Timestamp((message.getTime()));
                updateConsumerOffsetByTimestamp(consumer, beginTimestamp);

                //准备数据
                message.setRequestType(MessageEntity.REAL_MONITOR);
                List<WaterLevelRecord> waterLevelRecords = getRecordByConsumerPoll(consumer);
                message.setBuffer(waterLevelRecords);
                userSessionManager.setUserSessionMessageEntity(userSessionId, message, true);

                //表示该次请求操作被处理
                message.setRequestType(MessageEntity.BACK_TRACKING_PROCESSING);
                userSessionManager.setUserSessionMessageEntity(userSessionId,message);

                //设置用户下次操作超时时间
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND, 10);
                stopTimeInMillis = calendar.getTimeInMillis();
           }
           else{    //轮询查看Session状态
                log.info("[ BackTrackingService ] : 开始轮询 ( stopTime = " + new Date(stopTimeInMillis) + " newTime = " + new Date() + ")");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                message = userSessionManager.getUserSessionMessageEntity(userSessionId);
           }

           //用户超时，关闭BackTrackingListener,重新将当前session的WEBSOCKET_STATUS属性置为"REAL"，即当前session重新加入WebSocketListener监听队列
           if (stopTimeInMillis <= System.currentTimeMillis()){
               log.info("[ BackTrackingService ] : 用户超时");

               //关闭consumer进程
               consumer.close();

               //BackTrackingServiceManager，当前session结束BackTrackingListener服务
               backTrackingService.unsubscribe(userSessionId);
               break;
           }
        }

    }

    private KafkaConsumer<String, WaterLevelRecord> createNewConsumer(String consumerName){
        Map<String, Object> consumerConfig = generalConsumerConfig.getGeneralConsumerConfigs();
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, consumerName);
        KafkaConsumer<String, WaterLevelRecord> consumer = new KafkaConsumer<>(consumerConfig);
        consumer.subscribe(Arrays.asList(generalConsumerConfig.getTopic()));
        consumer.poll(Duration.ofMillis(1000));     //由于未知原因，只有在调用poll之后BackTrackingListener才会自动分配分区
        return consumer;
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
//            log.info("topic = " + topicPartition.topic() + " partition = " + topicPartition.partition() +  " offset = " + offset);
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
            if (++count == 20)
                break;
        }

        //对预缓存队列排序
        Collections.sort(waterLevelRecords, new Comparator<WaterLevelRecord>() {
            @Override
            public int compare(WaterLevelRecord w1, WaterLevelRecord w2) {
                if (w1.getTime().getTime() > w2.getTime().getTime()){
                    return 1;
                }else{
                    return -1;
                }
            }
        });

        return waterLevelRecords;
    }

}

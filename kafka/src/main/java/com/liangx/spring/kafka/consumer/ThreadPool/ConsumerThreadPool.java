package com.liangx.spring.kafka.consumer.ThreadPool;

import com.liangx.spring.kafka.config.GeneralConsumerConfig;
import com.liangx.spring.kafka.consumer.Impl.BackTrackingKafkaConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component("consumerThreadPool")
@Slf4j
public class ConsumerThreadPool {

    //创建线程池
    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Autowired
    private GeneralConsumerConfig generalConsumerConfig;

    public void beginBackTrackingListener(BackTrackingKafkaConsumer backTrackingKafkaConsumer){
        log.info(">>>>>>>>>> ConsumerThreadPool info : 启动 BackTrackingKafkaConsumer线程 <<<<<<<<<<<");
        cachedThreadPool.execute(backTrackingKafkaConsumer);
    }


}

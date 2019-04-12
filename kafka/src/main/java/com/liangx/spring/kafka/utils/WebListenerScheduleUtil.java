package com.liangx.spring.kafka.utils;

import com.liangx.spring.kafka.consumer.Impl.WebKafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Timer;
import java.util.TimerTask;

public class WebListenerScheduleUtil {

    private Timer timer = new Timer();

    @Autowired
    private WebKafkaConsumer webKafkaConsumer;

    /**
     * 定时关闭WebListener
     * @param time 等待时间s
     */
    public void shutdownListener(int time){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                webKafkaConsumer.stopWebListener();
            }
        }, time * 1000);
    }

}

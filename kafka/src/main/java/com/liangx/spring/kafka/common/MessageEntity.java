package com.liangx.spring.kafka.common;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Getter
@Setter
public class MessageEntity {

    public final static String REAL_MONITOR = "real_monitor";
    public final static String REAL_MONITOR_START = "real_monitor_start";
    public final static String REAL_MONITOR_STOP = "real_monitor_stop";

    public final static String BACK_TRACKING = "back_tracking";
    public final static String BACK_TRACKING_PROCESSING = "back_tracking_Processing";
    public final static String BACK_TRACKING_DONE = "back_tracking_done";

    public final static String DAILY_MONITOR = "daily_monitor";
    public final static String WEEKLY_MONITOR = "weekly_monitor";

//    private List<Object> buffer = new ArrayList<>();
    private String type = null;

    private long time = 0;

    private Object buffer = null;

    public MessageEntity(String type){
//        log.info(">>>>>>>>>>> MessageEntity info : 调用MessageEntity(String type) <<<<<<<<<<<<<<<<");
        this.type = type;
    }

    public MessageEntity(String type, long time){
//        log.info(">>>>>>>>>>> MessageEntity info : 调用MessageEntity(String type, long time) <<<<<<<<<<<<<<<<");
        this.type = type;
        this.time = time;
    }

    public MessageEntity(String type, Object buffer){
//        log.info(">>>>>>>>>>> MessageEntity info : 调用MessageEntity(String type, Object buffer) <<<<<<<<<<<<<<<<");
        this.type = type;
        this.buffer = buffer;
    }

    public MessageEntity(String type, long time, Object buffer){
//        log.info(">>>>>>>>>>> MessageEntity info : 调用MessageEntity(String type, long time, Object buffer) <<<<<<<<<<<<<<<<");
        this.type = type;
        this.time = time;
        this.buffer = buffer;
    }

    public String getJsonStr(){
        List<Object> requestList = new ArrayList<>();
        requestList.add(type);
        if (buffer != null){
            requestList.add(buffer);
        }
        if (time != 0){
            requestList.add(time);
        }

        return JSON.toJSONString(requestList);
    }

}


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

//    public final static String REAL_MONITOR_START = "real_monitor_start";
//    public final static String REAL_MONITOR_STOP = "real_monitor_stop";

    public final static String REAL_MONITOR = "real_monitor";

    public final static String BACK_TRACKING            = "back_tracking";
    public final static String BACK_TRACKING_PROCESSING = "back_tracking_Processing";
    public final static String BACK_TRACKING_DONE       = "back_tracking_done";

    public final static String DAILY_MONITOR    = "daily_monitor";
    public final static String WEEKLY_MONITOR   = "weekly_monitor";

    public final static String SUBSCRIBE_REAL_MONITOR_SERVICE   = "subscribe_real_monitor_service";
    public final static String SUBSCRIBE_BACK_TRACKING_SERVICE  = "subscribe_back_tracking_service";
    public final static String SUBSCRIBE_SCHEDULED_MONITOR_SERVICE  = "subscribe_scheduled_monitor_service";


    public final static String UNSUBSCRIBE_BACK_TRACKING_SERVICE = "unsubscribe_back_tracking_service";

//    private List<Object> buffer = new ArrayList<>();
    private String requestType = null;

    private long time = 0;

    private Object buffer = null;


    public MessageEntity(){

    }

    public MessageEntity(String requestType){
//        log.info(">>>>>>>>>>> MessageEntity info : 调用MessageEntity(String requestType) <<<<<<<<<<<<<<<<");
        this.requestType = requestType;
    }

    public MessageEntity(String requestType, long time){
//        log.info(">>>>>>>>>>> MessageEntity info : 调用MessageEntity(String requestType, long time) <<<<<<<<<<<<<<<<");
        this.requestType = requestType;
        this.time = time;
    }

    public MessageEntity(String requestType, Object buffer){
//        log.info(">>>>>>>>>>> MessageEntity info : 调用MessageEntity(String requestType, Object buffer) <<<<<<<<<<<<<<<<");
        this.requestType = requestType;
        this.buffer = buffer;
    }

    public MessageEntity(String requestType, long time, Object buffer){
//        log.info(">>>>>>>>>>> MessageEntity info : 调用MessageEntity(String requestType, long time, Object buffer) <<<<<<<<<<<<<<<<");
        this.requestType = requestType;
        this.time = time;
        this.buffer = buffer;
    }

    public String getJsonStr(){
        List<Object> requestList = new ArrayList<>();
        requestList.add(requestType);
        if (buffer != null){
            requestList.add(buffer);
        }
        if (time != 0){
            requestList.add(time);
        }

        return JSON.toJSONString(requestList);
    }

}


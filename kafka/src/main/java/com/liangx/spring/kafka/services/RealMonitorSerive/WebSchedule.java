//package com.liangx.spring.kafka.schedule;
//
//import com.alibaba.fastjson.JSON;
//import com.liangx.spring.kafka.common.WaterLevelRecord;
//import com.liangx.spring.kafka.services.RecordDurableService.WaterLevelRecordService;
//import com.liangx.spring.kafka.utils.UserSessionUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.catalina.User;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import javax.websocket.Session;
//import java.io.IOException;
//import java.sql.Timestamp;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//
///**
// * 定时发送数据到前端
// */
//@Component
//@Slf4j
//public class WebSchedule {
//
//    @Autowired
//    private WaterLevelRecordService waterLevelRecordService;
//
//    @Autowired
//    private UserSessionUtil userSessionUtil;
//
//    private int count = 1;
//
//    /**
//     * 每小时发送一次平均水位（/小时）数据
//     * @throws IOException
//     */
//    @Scheduled(cron = "0 0 * ? * ?")
//    public void SendHourlyRecords() throws IOException {
//        log.info(">>>>>>>>>>>>> WebSchedule info: 启动Hourly定时器 <<<<<<<<<<<<<<");
//
//        //从持久层中获取每小时的平均水位
//        Calendar calendar = Calendar.getInstance();
//        Timestamp endTime = new Timestamp(calendar.getTimeInMillis());  //当前时间戳
//        calendar.set(Calendar.HOUR, -1);
//        Timestamp beginTime = new Timestamp(calendar.getTimeInMillis());    //一个小时前时间戳
//        double avgWaterLevel = waterLevelRecordService.getAvgWaterLevelByInterval(beginTime, endTime);
//
//        //准备数据
//        List<Object> msg = new ArrayList<>();
//        msg.add("HOURLY");
//        WaterLevelRecord waterLevelRecord = new WaterLevelRecord();
//        waterLevelRecord.setSiteId(104);
//        waterLevelRecord.setSiteName("hohai");
//        waterLevelRecord.setId(count++);
//        waterLevelRecord.setTime(new Timestamp(System.currentTimeMillis()));
//        waterLevelRecord.setWaterLevel(avgWaterLevel);
//        msg.add(waterLevelRecord);
//
//        //给所有用户发送数据
//        List<Session> userSessions = userSessionUtil.getUserSessionsInfoContainer();
//        for (Session userSession : userSessions){
//            synchronized (userSession){
//                log.info(">>>>>>>>>>>>>> WebSchedule info: 给用户session(" + userSession.getId() + ")发送数据 : " + msg);
//                userSession.getBasicRemote().sendText(JSON.toJSONString(msg));
//            }
//        }
//
//    }
//
//    //每周一更新
//    @Scheduled(cron = "0 0 0 ? * 1")
//    public void SendWeeklyRecords() throws IOException {
//        log.info(">>>>>>>>>>>>> WebSchedule info: 启动Week定时器 <<<<<<<<<<<<<<");
//
//        //从持久层中获取每小时的平均水位
//        Calendar calendar = Calendar.getInstance();
//        Timestamp endTime = new Timestamp(calendar.getTimeInMillis());  //当前时间戳
//        calendar.set(Calendar.WEEK_OF_MONTH, -1);
//        Timestamp beginTime = new Timestamp(calendar.getTimeInMillis());    //一个小时前时间戳
//        double avgWaterLevel = waterLevelRecordService.getAvgWaterLevelByInterval(beginTime, endTime);
//
//        //准备数据
//        List<Object> msg = new ArrayList<>();
//        msg.add("WEEKLY");
//        WaterLevelRecord waterLevelRecord = new WaterLevelRecord();
//        waterLevelRecord.setSiteId(104);
//        waterLevelRecord.setSiteName("hohai");
//        waterLevelRecord.setId(count++);
//        waterLevelRecord.setTime(new Timestamp(System.currentTimeMillis()));
//        waterLevelRecord.setWaterLevel(avgWaterLevel);
//        msg.add(waterLevelRecord);
//
//        //给所有用户发送数据
//        List<Session> userSessions = userSessionUtil.getUserSessionsInfoContainer();
//        for (Session userSession : userSessions){
//            synchronized (userSession){
//                log.info(">>>>>>>>>>>>>> WebSchedule info: 给用户session(" + userSession.getId() + ")发送数据 : " + msg);
//                userSession.getBasicRemote().sendText(JSON.toJSONString(msg));
//            }
//        }
//
//    }
//
//}

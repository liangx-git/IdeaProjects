package com.liangx.spring.kafka.services.ScheduledMonitorSerive;

import com.liangx.spring.kafka.common.MessageEntity;
import com.liangx.spring.kafka.common.ServiceType;
import com.liangx.spring.kafka.common.SiteInformation;
import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.services.RecordDurableService.WaterLevelRecordService;
import com.liangx.spring.kafka.utils.PreparedBufferUtil;
import com.liangx.spring.kafka.utils.UserSessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

/**
 * 定时发送数据到前端
 */
@Component
@Slf4j
public class WebSchedule {

    @Autowired
    private WaterLevelRecordService waterLevelRecordService;

    @Autowired
    private UserSessionUtil userSessionUtil;

    @Autowired
    private PreparedBufferUtil preparedBufferUtil;


    private boolean dailyMonitorScheduleIsStarted = false;

    private boolean weeklyMonitorScheduleIsStarted = false;


    /**
     * 为userSessionId开启DailyMonitor服务
     * @param userSessionId
     */
    public void startDailyMonitorServieForUserSession(String userSessionId){

        //发送预缓存
        sendDailyMonitorPreparedBuffer(userSessionId);
        log.info("[ DailyMonitorService ] : 发送hourly preparedBuffer");

        //开启定时刷新服务
        dailyMonitorScheduleIsStarted = true;
    }

    /**
     * 如果DailyMonitorService预缓存可用，则在服务启动时发送给前端
     * @param userSessionId
     */
    private void sendDailyMonitorPreparedBuffer(String userSessionId){
        List<WaterLevelRecord>  hourlyBuffer = preparedBufferUtil.getHourlyBuffer();
        if (!hourlyBuffer.isEmpty()){
            MessageEntity messageEntity = new MessageEntity(MessageEntity.DAILY_MONITOR, hourlyBuffer);
            userSessionUtil.setUserSessionMessageEntity(userSessionId, messageEntity, true);    //sendToFrontEnd设为true表示将缓存立即发送到前端
        }
    }


    /**
     *取消订阅了DailyMonitorService
     * @param userSessionId
     */
    public void stopDailyMonitorServieForUserSession(String userSessionId){

        //最后一个UserSesson取消订阅DailyMonitorService时关闭服务
        if(userSessionUtil.noUserSessionSubscribedService(ServiceType.DAILY_MONITOR_SERVICE)){
            dailyMonitorScheduleIsStarted = false;
        }
    }


    /**
     * 为userSessionId开启WeeklyMonitor服务
     * @param userSessionId
     */
    public void startWeeklyMonitorServiceForUserSession(String userSessionId){

        //发送预缓存
        sendWeeklyMonitorPreparedBuffer(userSessionId);
        log.info("[ WeeklyMonitorService ] : 发送weekly preparedBuffer");

        //开启定时刷新任务
        weeklyMonitorScheduleIsStarted = true;

    }

    private void sendWeeklyMonitorPreparedBuffer(String userSessionId){
        List<WaterLevelRecord>  weeklyBuffer = preparedBufferUtil.getWeeklyBuffer();
        if (!weeklyBuffer.isEmpty()){
            MessageEntity messageEntity = new MessageEntity(MessageEntity.WEEKLY_MONITOR, weeklyBuffer);
            userSessionUtil.setUserSessionMessageEntity(userSessionId, messageEntity, true);    //sendToFrontEnd设为true表示将缓存立即发送到前端
        }
    }


    /**
     *取消订阅了WeeklyMonitorService
     * @param userSessionId
     */
    public void stopWeeklyMonitorServiceForUserSession(String userSessionId){

        //最后一个UserSesson取消订阅WeekyMonitorService时关闭服务
        if(userSessionUtil.noUserSessionSubscribedService(ServiceType.WEEKLY_MONITOR_SERVICE)){
            weeklyMonitorScheduleIsStarted = false;
        }
    }


    /**
     * 每小时发送一次平均水位（/小时）数据
     * @throws IOException
     */
    @Scheduled(cron = "0 0 * ? * ?")
    private void SendHourlyRecords() throws IOException {

        if (!dailyMonitorScheduleIsStarted){    //当dailyMonitorScheduleIsStarted为true时服务启动
            return;
        }

        //从持久层中获取每小时的平均水位
        double avgWaterLevel = getAvgWaterLevelByTime("hour", -1);

        //准备数据
        MessageEntity messageEntity = new MessageEntity(
                MessageEntity.DAILY_MONITOR,
                new WaterLevelRecord(new Timestamp(System.currentTimeMillis()),
                        SiteInformation.siteId,
                        SiteInformation.siteName,
                        avgWaterLevel)
        );

        //给所有订阅了DailyMonitorService用户发送数据
        List<String> sessionIds = userSessionUtil.getSubcribedServicesUserSessionIds(ServiceType.WEEKLY_MONITOR_SERVICE);
        sendMessageEntityToAllSubscribedSessions(sessionIds, messageEntity);
    }

    //每天00:00更新
    @Scheduled(cron = "0 0 0 * * ?")
    private void SendDailyRecords() throws IOException {

        if (!weeklyMonitorScheduleIsStarted){   //当weeklyMonitorScheduleIsStarted为true时服务启动
            return;
        }

        //从持久层中获取每小时的平均水位
        double avgWaterLevel = getAvgWaterLevelByTime("hour", -1);

        //准备数据
        MessageEntity messageEntity = new MessageEntity(
                MessageEntity.WEEKLY_MONITOR,
                new WaterLevelRecord(new Timestamp(System.currentTimeMillis()),
                        SiteInformation.siteId,
                        SiteInformation.siteName,
                        avgWaterLevel)
        );

        //给所有订阅了WeeklyMonitorService的用户发送数据
        List<String> sessionIds = userSessionUtil.getSubcribedServicesUserSessionIds(ServiceType.WEEKLY_MONITOR_SERVICE);
        sendMessageEntityToAllSubscribedSessions(sessionIds, messageEntity);
    }


    private double getAvgWaterLevelByTime(String timeType, int index){
        Calendar calendar = Calendar.getInstance();
        Timestamp endTime = new Timestamp(calendar.getTimeInMillis());  //当前时间戳
        if (timeType.equals("hour")){
            calendar.set(Calendar.HOUR, index);
        } else if (timeType.equals("day")){
            calendar.set(Calendar.DAY_OF_MONTH, index);
        }
        Timestamp beginTime = new Timestamp(calendar.getTimeInMillis());    //一个小时前时间戳
        return waterLevelRecordService.getAvgWaterLevelByInterval(beginTime, endTime);
    }

    private void sendMessageEntityToAllSubscribedSessions(List<String> userSessionIds, MessageEntity messageEntity){
        //给所有用户发送数据
        for (String userSessionId : userSessionIds){
           userSessionUtil.setUserSessionMessageEntity(userSessionId, messageEntity, true);
            log.info("[ WebScheduleService ] :更新session(" + userSessionId + ")的" + messageEntity.getRequestType() +" Chart" );
        }
    }
}

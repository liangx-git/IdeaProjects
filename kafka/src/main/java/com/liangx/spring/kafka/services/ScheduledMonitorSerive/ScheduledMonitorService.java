package com.liangx.spring.kafka.services.ScheduledMonitorSerive;

import com.liangx.spring.kafka.common.MessageEntity;
import com.liangx.spring.kafka.common.SiteInformation;
import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.services.BaseService.BaseService;
import com.liangx.spring.kafka.services.RecordDurableService.WaterLevelRecordService;
import com.liangx.spring.kafka.utils.PreparedBufferUtil;
import com.liangx.spring.kafka.services.Manager.UserManager;
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
@Component("scheduledMonitorService")
@Slf4j
public class ScheduledMonitorService extends BaseService {

    @Autowired
    private WaterLevelRecordService waterLevelRecordService;

    @Autowired
    private UserManager userManager;

    @Autowired
    private PreparedBufferUtil preparedBufferUtil;


    private boolean dailyMonitorScheduleIsStarted = false;

    private boolean weeklyMonitorScheduleIsStarted = false;

    private boolean scheduledMonitorServiceIsStarted = false;


    @Override
    public void subscribe(String userSessionId) {

        //如果用户发起请求时服务未启动，则启动
        if (!scheduledMonitorServiceIsStarted) {
            scheduledMonitorServiceIsStarted = true;
            log.info("[ ScheduledMonitorService ] : 开启服务");
        }

        //新增用户注册到服务中
        if (!isRegistered(userSessionId)){
            register(userSessionId);
            log.info("[ ScheduledMonitorService ] : UserSession(" + userSessionId + ")订阅服务成功");

            //对新订阅用户发送表格预缓存数据
            sendDailyMonitorPreparedBuffer(userSessionId);
            sendWeeklyMonitorPreparedBuffer(userSessionId);
            log.info("[ ScheduledMonitorService ] : 发送表格预缓存数据");
        }
    }

    @Override
    public void unsubscribe(String userSessionId) {

        //当最后一个用户取消订阅时关闭服务
        if (noUserRegistered()){
            scheduledMonitorServiceIsStarted = false;
            log.info("[ ScheduledMonitorService ] : 关闭服务");
        }

        //将用户从服务中注销
        if (isRegistered(userSessionId)){
            unregister(userSessionId);
            log.info("[ ScheduledMonitorService ] : UserSession(" + userSessionId + ")取消订阅服务");
        }
    }


    /**
     * 如果DailyMonitorService预缓存可用，则在服务启动时发送给前端
     * @param userSessionId
     */
    private void sendDailyMonitorPreparedBuffer(String userSessionId){
        log.info("[ ScheduledMonitorService ] : 发送DilyMonitor表格预缓存数据");
        List<WaterLevelRecord>  hourlyBuffer = preparedBufferUtil.getHourlyBuffer();
        if (!hourlyBuffer.isEmpty()){
            MessageEntity messageEntity = new MessageEntity(MessageEntity.DAILY_MONITOR, hourlyBuffer);
            userManager.setUserSessionMessageEntity(userSessionId, messageEntity, true);    //sendToFrontEnd设为true表示将缓存立即发送到前端
        }
    }


    private void sendWeeklyMonitorPreparedBuffer(String userSessionId){
        log.info("[ ScheduledMonitorService ] : 发送WeeklyMonitor表格预缓存数据");
        List<WaterLevelRecord>  weeklyBuffer = preparedBufferUtil.getWeeklyBuffer();
        if (!weeklyBuffer.isEmpty()){
            MessageEntity messageEntity = new MessageEntity(MessageEntity.WEEKLY_MONITOR, weeklyBuffer);
            userManager.setUserSessionMessageEntity(userSessionId, messageEntity, true);    //sendToFrontEnd设为true表示将缓存立即发送到前端
        }
    }


    /**
     * 每小时发送一次平均水位（/小时）数据
     * @throws IOException
     */
    @Scheduled(cron = "0 0 * ? * ?")
    private void SendHourlyRecords() throws IOException {

        if (!scheduledMonitorServiceIsStarted){
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
        broadcastMessage(messageEntity);
    }

    //每天00:00更新
    @Scheduled(cron = "0 0 0 * * ?")
    private void SendDailyRecords() throws IOException {

        if (!scheduledMonitorServiceIsStarted){
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
        broadcastMessage(messageEntity);
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


    private void broadcastMessage(MessageEntity messageEntity){
        List<String> registeredUserSessionIds = getRegisteredUserSessionIds();
        for (String userSessionId : registeredUserSessionIds){
            userManager.setUserSessionMessageEntity(userSessionId, messageEntity, true);
            log.info("[ ScheduleMonitorService ] :更新session(" + userSessionId + ")的" + messageEntity.getRequestType() +" Chart" );
        }
    }


}

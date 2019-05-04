package com.liangx.spring.kafka.services.BackTrackingService;

import com.alibaba.fastjson.JSON;
import com.liangx.spring.kafka.common.MessageEntity;
import com.liangx.spring.kafka.config.GeneralConsumerConfig;
import com.liangx.spring.kafka.utils.UserSessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Component("consumerThreadPool")
@Slf4j
public class BackTrackingServiceManager {

    //创建线程池
//    private ExecutorService executor = Executors.newCachedThreadPool();
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(
            4,
            10,
            200,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(5));

    //真正执行数据回溯服务的线程本体
    @Autowired
    private BackTrackingKafkaTask backTrackingKafkaTask;

    //用于BackTrackingTask线程运行期间获取用户提交的数据
    @Autowired
    private UserSessionUtil userSessionUtil;

    //用于BackTrackingKafkaTask线程中实例化consumer进程
    @Autowired
    private GeneralConsumerConfig generalConsumerConfig;

    //请求了BackTracking服务的用户都会把sessionId注册到此
    private List<String> registryTables = new ArrayList<>();


    /**
     * 开启BackTrackingTask线程
     * @param userSessionId ： 当前用户的session的id，
     */
    public void startBackTrackingListener(String userSessionId){
        //将sessionId注册到BackTrackingService中
        registryTables.add(userSessionId);

        //通知UserSessionUtil当前session订阅BackTrackingService
        userSessionUtil.setUserSessionRequestService(userSessionId, UserSessionUtil.BACK_TRACKING_SERVICE);

        backTrackingKafkaTask.addUserSession(userSessionId);
        executor.execute(backTrackingKafkaTask);
        log.info("[ BackTrackingServiceManager ]: 当前线程池中活动线程数为： " + executor.getActiveCount());
    }


    public void stopBackTrackingListener(String userSessionId){

        //从注册表中删除当前用户sessionId记录
        Iterator<String> iter = registryTables.iterator();
        while (iter.hasNext()){
            if ((iter.next()).equals(userSessionId)){
                //将当前sessionId从注册表中移除
                iter.remove();

                //将当前session重新加入到WebListener监听队列中
                userSessionUtil.setUserSessionRequestService(userSessionId, UserSessionUtil.REAL_MONITOR_SERVICE_START);

                //通知前端BackTrackingListener关闭监听
                userSessionUtil.setUserSessionMessageEntity(userSessionId, new MessageEntity(MessageEntity.BACK_TRACKING_DONE), true);
            }
        }
    }


    public boolean backTrackingListenerForSessionIsStart(String userSessionId){
        for (String sessionId : registryTables){
            if ((sessionId).equals(userSessionId))
                return true;
        }
        return false;
    }

}

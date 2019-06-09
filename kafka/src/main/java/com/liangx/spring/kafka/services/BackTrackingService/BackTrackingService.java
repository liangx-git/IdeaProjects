package com.liangx.spring.kafka.services.BackTrackingService;

import com.liangx.spring.kafka.common.MessageEntity;
import com.liangx.spring.kafka.config.GeneralConsumerConfig;
import com.liangx.spring.kafka.services.BaseService.BaseService;
import com.liangx.spring.kafka.services.Manager.UserManager;
import com.liangx.spring.kafka.services.RealMonitorService.RealMonitorService;
import com.liangx.spring.kafka.utils.IocUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@Component("backTrackingService")
@Slf4j
public class BackTrackingService extends BaseService {

    //真正执行数据回溯服务的线程本体
    @Autowired
    private BackTrackingTask backTrackingTask;

    //用于BackTrackingTask线程运行期间获取用户提交的数据
    @Autowired
    private UserManager userManager;

    //用于BackTrackingKafkaTask线程中实例化consumer进程
    @Autowired
    private GeneralConsumerConfig generalConsumerConfig;

    @Autowired
    private IocUtil iocUtil;

    @Autowired
    private RealMonitorService realMonitorService;

    //创建线程池
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(
            4,
            10,
            200,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(5));


    @Override
    public void subscribe(String userSessionId) {

        if (realMonitorService.isSubscribed(userSessionId)){
            realMonitorService.unsubscribe(userSessionId);
        }

        if (!isRegistered(userSessionId)){
            register(userSessionId);

            backTrackingTask.addUserSession(userSessionId);
            executor.execute(backTrackingTask);

            log.info("[ BackTrackingServiceManager ]: 当前线程池中活动线程数为： " + executor.getActiveCount());
        }
    }

    @Override
    public void unsubscribe(String userSessionId) {
        if (isRegistered(userSessionId)){
            unregister(userSessionId);

            //通知前端关闭BackTrackingService
            userManager.setUserSessionMessageEntity(
                    userSessionId,
                    new MessageEntity(MessageEntity.BACK_TRACKING_DONE),
                    true);
        }

    }


    public boolean isSubscribed(String userSessionId){
        return isRegistered(userSessionId);
    }

}

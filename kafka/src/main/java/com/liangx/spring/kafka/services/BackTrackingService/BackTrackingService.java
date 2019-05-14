package com.liangx.spring.kafka.services.BackTrackingService;

import com.liangx.spring.kafka.common.MessageEntity;
import com.liangx.spring.kafka.common.ServiceType;
import com.liangx.spring.kafka.config.GeneralConsumerConfig;
import com.liangx.spring.kafka.services.BaseService.BaseService;
import com.liangx.spring.kafka.services.RealMonitorService.RealMonitorService;
import com.liangx.spring.kafka.services.UserSessionManager.UserSessionManager;
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
    private UserSessionManager userSessionManager;

    //用于BackTrackingKafkaTask线程中实例化consumer进程
    @Autowired
    private GeneralConsumerConfig generalConsumerConfig;

    @Autowired
    private IocUtil iocUtil;

    //创建线程池
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(
            4,
            10,
            200,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(5));

    //请求了BackTracking服务的用户都会把sessionId注册到此
    private List<String> registryTables = new ArrayList<>();


    @Override
    public void subscribe(String userSessionId) {
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
            userSessionManager.setUserSessionMessageEntity(
                    userSessionId,
                    new MessageEntity(MessageEntity.BACK_TRACKING_DONE),
                    true);
        }

    }

//    protected void stopService(String userSessionId){
//       unsubscribe(userSessionId);
//
//       //订阅realMonitorService
//        RealMonitorService realMonitorService = (RealMonitorService) iocUtil.getApplicationContext().getBean("realMonitorService");
//        realMonitorService.subscribe(userSessionId);
//    }


    /**
     * 为userSessionId开启BackTrackingService服务
     * 需要将userSession注册到BackTrackingServiceManager中
     * @param userSessionId
     */
//    public void startBackTrackingService(String userSessionId){
//
//        //如果该用户未注册到BackTrackingServiceManger中，且未开启BackTrackingListener服务，则为其注册并开启服务
//        if (!backTrackingListenerForSessionIsStart(userSessionId)){
//            startBackTrackingListener(userSessionId);
//        }
//    }

    /**
     *取消订阅了BackTrackingService
     * @param userSessionId
     */
//    public void stopBackTrackingService(String userSessionId){
//
//        //关闭BackTrackingService线程
//        stopBackTrackingListener(userSessionId);
//
//        //通知前端关闭BackTrackingService
//        userSessionManager.setUserSessionMessageEntity(
//                userSessionId,
//                new MessageEntity(MessageEntity.BACK_TRACKING_DONE),
//                true);
//    }


    /**
     * 开启BackTrackingTask线程
     * @param userSessionId ： 当前用户的session的id，
     */
//    private void startBackTrackingListener(String userSessionId){
//
//        //将sessionId注册到BackTrackingService中
////        registryTables.add(userSessionId);
//
//        backTrackingTask.addUserSession(userSessionId);
//        executor.execute(backTrackingTask);
//
//        log.info("[ BackTrackingServiceManager ]: 当前线程池中活动线程数为： " + executor.getActiveCount());
//    }


//    public void stopBackTrackingListener(String userSessionId){
//
//        //从注册表中删除当前用户sessionId记录
//        Iterator<String> iter = registryTables.iterator();
//        while (iter.hasNext()){
//            if ((iter.next()).equals(userSessionId)){
//
//                //将当前sessionId从注册表中移除
//                iter.remove();
//
//                //取消当前用户对BackTrackingService的订阅，并订阅RealMonitorService
//                int service = userSessionManager.getSubscribedServices(userSessionId);
//                service &= ~ServiceType.BACK_TRACKING_SERVICE;  //取消订阅BackTrackingService
//                service |= ServiceType.REAL_MONITOR_SERVICE;    //订阅RealMonitorService
//                userSessionManager.subscribeService(userSessionId, service);
//            }
//        }
//    }


    /**
     * 存在相应的线程为session服务时返回true;
     * @param userSessionId
     * @return
     */
//    private boolean backTrackingListenerForSessionIsStart(String userSessionId){
//        for (String sessionId : registryTables){
//            if ((sessionId).equals(userSessionId))
//                return true;
//        }
//        return false;
//    }



}

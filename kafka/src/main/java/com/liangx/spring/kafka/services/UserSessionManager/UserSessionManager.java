package com.liangx.spring.kafka.services.UserSessionManager;

import com.liangx.spring.kafka.common.MessageEntity;
import com.liangx.spring.kafka.common.ServiceType;
import com.liangx.spring.kafka.services.BackTrackingService.BackTrackingService;
import com.liangx.spring.kafka.services.BaseService.BaseService;
import com.liangx.spring.kafka.services.RealMonitorService.RealMonitorService;
import com.liangx.spring.kafka.services.ScheduledMonitorSerive.ScheduledMonitorService;
import com.liangx.spring.kafka.utils.IocUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.*;



/**
 * 管理通过WebSocket连接的用户的session
 */
@Component
@Slf4j
public class UserSessionManager{

    @Autowired
    private RealMonitorService realMonitorService;

//    @Autowired
//    private PreparedBufferUtil preparedBufferUtil;

    @Autowired
    private ScheduledMonitorService scheduledMonitorService;

    @Autowired
    private BackTrackingService backTrackingService;


    private List <Session> userSessions = new ArrayList<>();    //保存所有注册的session信息,以及session请求的服务
//    private Map<String, UserSessionInformation> userSessionInformationMap = new HashMap<>();    //保存session对应的消息缓存MessageEntity
    private Map<String, MessageEntity> userSessionInformationMap = new HashMap<>();

    private List<String> services = new ArrayList<>();

    public UserSessionManager(){

        //经响应用户请求的服务注册到UserSessionManager
        services.add("realMonitorService");
        services.add("scheduledMonitorService");
        services.add("backTrackingService");

    }


    /**
     * 将新用户session注册到UserSessionUtil
     * @param userSession : 新用户session
     */
    public synchronized void register(Session userSession){
        userSessions.add(userSession);
        userSessionInformationMap.put(userSession.getId(), new MessageEntity());
        log.info("[ UserSessionManager ] : user session(" + userSession.getId() + ") register, current user number: (" + userSessions.size() + ")");
    }


    /**
     * 将用户session从UserSessionUtil取消注册
     * @param userSessionId
     */
    public synchronized void unregister(String userSessionId){
        Session userSession = getUserSessionById(userSessionId);
        if (userSession != null){   //清除userSessions和userSessionInformationMap中和userSessionId相关的信息
            userSessions.remove(userSession);
            userSessionInformationMap.remove(userSessionId);

            //注销userSessionId订阅的所有服务
            unsubscribeAllServices(userSessionId);
            log.info("[ UserSessionManager ] : user session(" + userSessionId + ") unregister, current user number: (" + userSessions.size() + ")");
        }
    }

    private void unsubscribeAllServices(String userSessionId){
        for (String serviceName : services){
            BaseService service = getServiceInstanceFromIoc(serviceName);
            service.unsubscribe(userSessionId);
        }
    }


    /**
     * 为userSeesionId订阅serviceType对应的服务
     * @param userSessionId
     * @param serviceType
     */
    public void subscribeService_(String userSessionId, String serviceType){
        BaseService serviceInstance = getServiceInstanceFromIoc(serviceType);
        serviceInstance.subscribe(userSessionId);
    }


    /**
     *
     * @param userSessionId
     * @param serviceType
     */
    public void unsubscribeService_(String userSessionId, String serviceType){
        BaseService serviceInstance = getServiceInstanceFromIoc(serviceType);
        serviceInstance.unsubscribe(userSessionId);
    }


    /**
     * 通过JavaBean的名字从Ioc容器中获取服务实例
     * @param serviceName
     * @return
     */
    private BaseService getServiceInstanceFromIoc(String serviceName){
        return (BaseService) IocUtil.getApplicationContext().getBean(serviceName);
    }


    /**
     * 为userSessionId开启订阅的服务，
     * @param userSessionId
     * @param services
     */
//    public void subscribeService(String userSessionId, int services){
//
//        int oldSubscribedService = getSubscribedServices(userSessionId);
//        int diffService = oldSubscribedService ^ services;
//
//        //（正真订阅服务的地方）
//        updateUserSessionSubscribedServices(userSessionId, services);
//
//        //userSessionId取消订阅的服务,如果需要时彻底关闭服务
//        int unsubscribedServices = diffService & oldSubscribedService;
//        stopServiceForUserSession(userSessionId, unsubscribedServices);
//
//        //userSessionId订阅新的服务，如果请求的服务未开启时开启
//        int newSubscribedServices = diffService & services;
//        startServiceForUserSession(userSessionId, newSubscribedServices);
//
//    }


//    private void updateUserSessionSubscribedServices(String userSessionId, int services){
//
//        //获取userSessionId对应的UserSessionInformation
//        UserSessionInformation userSessionInformation = userSessionInformationMap.get(userSessionId);
//        if (userSessionInformation == null){
//            userSessionInformation = new UserSessionInformation(services);
//        }
//        userSessionInformation.setSubscribedServices(services);
//
//        //更新UserSessionInformation
//        userSessionInformationMap.put(userSessionId, userSessionInformation);
//    }


    /**
     * 执行userSessionId订阅的服务（services）开启所需的操作
     * @param userSessionId
     * @param services  可以是多个服务
     */
//    private void startServiceForUserSession(String userSessionId, int services){
//
//        //开启RealMonitorService
//        if ((services & ServiceType.REAL_MONITOR_SERVICE) != 0){
//            realMonitorService.startRealMonitorServiceForUserSession(userSessionId);
//        }
//
//        //开启DailyMonitorService
//        if ((services & ServiceType.DAILY_MONITOR_SERVICE) != 0){
//            scheduledMonitorService.startDailyMonitorServieForUserSession(userSessionId);
//        }
//
//        //开启WeeklyMonitorService
//        if ((services & ServiceType.WEEKLY_MONITOR_SERVICE) != 0){
//            scheduledMonitorService.startWeeklyMonitorServiceForUserSession(userSessionId);
//        }
//
//        //开启BackTrackingService
//        if ((services & ServiceType.BACK_TRACKING_SERVICE) != 0){
//            backTrackingService.startBackTrackingService(userSessionId);
//        }
//    }


    /**
     * 停止为userSession提供services中指定服务
     * @param userSessionId
     * @param services
     */
//    private void stopServiceForUserSession(String userSessionId, int services){
//        //userSession在取消订阅服务时，系统会检查可以关闭的服务（没有UserSession订阅,并且不需要一直开启的服务），并关闭
//
//        //取消订阅RealMonitorService
//        if ((services & ServiceType.REAL_MONITOR_SERVICE) != 0){
//            realMonitorService.stopRealMonitorServiceForUserSession(userSessionId);
//        }
//
//        //取消订阅了DailyMonitorService
//        if ((services & ServiceType.DAILY_MONITOR_SERVICE) != 0){
//            scheduledMonitorService.stopDailyMonitorServieForUserSession(userSessionId);
//        }
//
//        //取消订阅了WeeklyMonitorService
//        if ((services & ServiceType.WEEKLY_MONITOR_SERVICE) != 0){
//            scheduledMonitorService.stopWeeklyMonitorServiceForUserSession(userSessionId);
//        }
//
//        //取消订阅了BackTrackingService
//        if ((services & ServiceType.BACK_TRACKING_SERVICE) != 0){
//            backTrackingService.stopBackTrackingService(userSessionId);
//        }
//    }


    /**
     * 当没有userSession订阅services指定的服务时返回true
     * @param services
     * @return
     */
//    public boolean noUserSessionSubscribedService(int services){
//        for (UserSessionInformation userSessionInformation : userSessionInformationMap.values()){
//            if ((userSessionInformation.getSubscribedServices() & services) != 0){
//                return false;
//            }
//        }
//        return true;
//    }


    /**
     * 获取当前UserSession订阅的服务
     * @param userSessionId
     * @return
     */
//    public int getSubscribedServices(String userSessionId){
//        if (userSessionInformationMap.get(userSessionId) == null){
//            return 0;
//        }
//
//        return userSessionInformationMap.get(userSessionId).getSubscribedServices();
//    }


    /**
     * 获取所有订阅了services中指定服务的UserSession的id信息
     * @return
     */
//    public List<String> getSubcribedServicesUserSessionIds(int services){
//       List<String> userSessionIds = new ArrayList<>();
//       for (String userSessionId : userSessionInformationMap.keySet()){
//           if ((userSessionInformationMap.get(userSessionId).getSubscribedServices() & services) != 0){
//               userSessionIds.add(userSessionId);
//           }
//       }
//       return userSessionIds;
//    }

    /**
     * 几乎所有service都是通过该方法给前端发送数据的
     * @param userSessionId
     * @param messageEntity
     * @param sendToFrontEnd true时，立即给当前session发送消息
     */
    public synchronized void setUserSessionMessageEntity(String userSessionId, MessageEntity messageEntity, boolean sendToFrontEnd){

//        UserSessionInformation userSessionInformation = userSessionInformationMap.get(userSessionId);
//        if (userSessionInformation != null){
//            userSessionInformation.setMessageEntity(messageEntity);
//            userSessionInformationMap.put(userSessionId, userSessionInformation);
//
//            //每次更新MessageEntity对象，如果sendToFrontEnd为true表明是后端更新，会主动将MessageEntity对象发送到前端
//            if (sendToFrontEnd) {
//                sendMessage(userSessionId);
//            }
//        }

        userSessionInformationMap.put(userSessionId, messageEntity);

        if (sendToFrontEnd){
            sendMessage(userSessionId);
        }

    }


    public synchronized void setUserSessionMessageEntity(String sessionId, MessageEntity messageEntity){
        setUserSessionMessageEntity(sessionId, messageEntity, false);
    }


    public MessageEntity getUserSessionMessageEntity(String sessionId){
        return userSessionInformationMap.get(sessionId);
    }


    private Session getUserSessionById(String userSessionId){
        for (Session session : userSessions){
            if ((session.getId()).equals(userSessionId)){
               return session;
            }
        }
        return null;
    }

    /**
     * 将sessionId对应的MessageEntity对象发送给前端
     * @param userSessionId
     */
    private void sendMessage(String userSessionId){
        Session userSession = getUserSessionById(userSessionId);
        try {
            if (userSession != null && userSession.isOpen()){
                synchronized (userSession){
                    MessageEntity message = userSessionInformationMap.get(userSessionId);
                    userSession.getBasicRemote().sendText(message.getJsonStr());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

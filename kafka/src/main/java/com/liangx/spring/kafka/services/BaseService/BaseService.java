//package com.liangx.spring.kafka.services.BaseService;
//
//import lombok.Getter;
//import lombok.Setter;
//
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;
//
//@Setter
//@Getter
//public abstract class BaseService {
//
//    //服务类型
//    private String serviceType;
//
//    //记录所有i订阅了该服务的userSessionId
//    private LinkedList<String> subscribedSessionIds = new LinkedList<>();
//
//
//    /**
//     * 每个服务都对外提供统一启动服务的接口
//     */
//    public abstract void startService();
//
//    /**
//     * 统一停止服务接口
//     */
//    public abstract void stopService();
//
//    /**
//     * 当用户订阅服务时调用
//     * @param userSessionId
//     */
//    public void subscribeService(String userSessionId){
//        subscribedSessionIds.add(userSessionId);
//    }
//
//
//    /**
//     * 当用户取消订阅服务时调用
//     * @param userSessionId
//     */
//    public void unsubscribeService(String userSessionId){
//        if (subscribedSessionIds.contains(userSessionId)){
//            subscribedSessionIds.remove(userSessionId);
//        }
//    }
//
//
//    public LinkedList<String> getSubscribedList(){
//        return subscribedSessionIds;
//    }
//
//
//    public boolean userSessionIsRegistered(String userSessionId){
//        return subscribedSessionIds.contains(userSessionId);
//    }
//
//    public boolean noUserSessionSubscribedService(){
//        return subscribedSessionIds.isEmpty();
//    }
//}

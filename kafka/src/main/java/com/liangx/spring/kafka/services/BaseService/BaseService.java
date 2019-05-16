package com.liangx.spring.kafka.services.BaseService;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;

@Setter
@Getter
public abstract class BaseService {

    //服务类型
    private String serviceType;

    //记录所有i订阅了该服务的userSessionId
    private LinkedList<String> registeredUserSessionIds = new LinkedList<>();


    public abstract void subscribe(String userSessionId);

    public abstract void unsubscribe(String userSessionId);


    /**
     * 当用户订阅服务时调用
     * @param userSessionId
     */
    protected void register(String userSessionId){
        if (!registeredUserSessionIds.contains(userSessionId)){
            synchronized (registeredUserSessionIds){
                registeredUserSessionIds.add(userSessionId);
            }
        }
    }


    /**
     * 当用户取消订阅服务时调用
     * @param userSessionId
     */
    protected void unregister(String userSessionId){
        if (registeredUserSessionIds.contains(userSessionId)){
            synchronized (registeredUserSessionIds){
                registeredUserSessionIds.remove(userSessionId);
            }
        }
    }


    protected LinkedList<String> getRegisteredUserSessionIds(){
        synchronized (registeredUserSessionIds){
            return registeredUserSessionIds;
        }
    }


    protected boolean isRegistered(String userSessionId){
        synchronized (registeredUserSessionIds){
            return registeredUserSessionIds.contains(userSessionId);
        }
    }

    protected boolean noUserRegistered(){
        synchronized (registeredUserSessionIds){
            return registeredUserSessionIds.isEmpty();
        }
    }

}

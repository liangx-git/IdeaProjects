package com.liangx.spring.kafka.services.Manager;

import com.liangx.spring.kafka.common.MessageEntity;
import com.liangx.spring.kafka.services.BaseService.BaseService;
import com.liangx.spring.kafka.utils.IocUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ServicesManager {

    private Map<String, String> services = new HashMap<>();


    public ServicesManager(){

        //注册所有面向用户的服务
        registerService(MessageEntity.SUBSCRIBE_REAL_MONITOR_SERVICE, "realMonitorService");
        registerService(MessageEntity.SUBSCRIBE_SCHEDULED_MONITOR_SERVICE ,"scheduledMonitorService");
        registerService(MessageEntity.SUBSCRIBE_BACK_TRACKING_SERVICE ,"backTrackingService");
    }


    public void registerService(String requestType, String serviceName){
        if (!services.containsKey(requestType)){
           services.put(requestType, serviceName);
        }
    }



    public void subscribeService(String userSessionId, String serviceType){
        String serviceName = services.get(serviceType);
        BaseService serviceInstance = getServiceInstanceFromIoc(serviceName);
        serviceInstance.subscribe(userSessionId);
    }


    public void unsubscribeAllService(String userSessionId){
        for (String serviceName : services.values()){
            BaseService serviceInstance = getServiceInstanceFromIoc(serviceName);
            serviceInstance.unsubscribe(userSessionId);
        }

    }


    private BaseService getServiceInstanceFromIoc(String serviceName){
        return (BaseService) IocUtil.getApplicationContext().getBean(serviceName);
    }

}
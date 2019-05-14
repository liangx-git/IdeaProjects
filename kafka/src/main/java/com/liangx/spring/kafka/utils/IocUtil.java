package com.liangx.spring.kafka.utils;

import javafx.application.Application;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 *实现ApplicationAware接口，提供给诸如@ServerEndPoint注解的类中无法使用@Autowired自动装载对象的情况，
 * 通过该类中的静态方法可以获取IoC容器
 */
@Component
public class IocUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }
}

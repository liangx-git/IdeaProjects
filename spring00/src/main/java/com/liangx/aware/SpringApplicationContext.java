package com.liangx.aware;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringApplicationContext implements ApplicationContextAware, BeanNameAware {

    private String beanName;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("SpringApplicationContext: " + applicationContext.getBean(beanName).hashCode());

    }

    @Override
    public void setBeanName(String s) {
        System.out.println("SetBeanName");
        beanName = s;
    }
}

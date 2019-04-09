package com.liangx.ioc.interfaces;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args)
    {
        //创建ioc容器
        ApplicationContext ac = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");

        //从ioc容器中获取bean实例

    }
}

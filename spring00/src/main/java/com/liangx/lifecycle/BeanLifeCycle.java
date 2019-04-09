package com.liangx.lifecycle;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.DisposableBean;

//implements InitializingBean, DisposableBean
public class BeanLifeCycle {

//    @Override
//    public void destroy() throws Exception {
//        System.out.println("Bean destroy");
//
//    }
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        System.out.println("Bean afterPropertiesSet");
//    }

    public void start()
    {
        System.out.println("Bean start");
    }

    public void stop()
    {
        System.out.println("Bean stop");
    }


}

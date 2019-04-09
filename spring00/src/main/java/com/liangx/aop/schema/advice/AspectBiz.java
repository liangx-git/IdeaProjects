package com.liangx.aop.schema.advice;

public class AspectBiz {

    public void biz()
    {
        System.out.println("AspectBiz biz.");
        //throw new RuntimeException();
    }

    public void init(String bizName, int times){
        System.out.println("Aspect init: " + bizName + " " + times);
    }
}

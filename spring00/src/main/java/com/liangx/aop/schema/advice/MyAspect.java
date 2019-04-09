package com.liangx.aop.schema.advice;

import org.aspectj.lang.ProceedingJoinPoint;

public class MyAspect {

    public void before()
    {
        System.out.println("MyAspect before.");
    }

    public void afterReturning()
    {
        System.out.println("MyAspect afterReturning.");
    }

    public void afterThrowing()
    {
        System.out.println("MyAspect afterThrowing.");
    }

    public void after()
    {
        System.out.println("MyAspect after.");
    }

    public Object around(ProceedingJoinPoint pjp)
    {

        Object obj = null;
        try{
            System.out.println("MyAspect around begin.");
            obj = pjp.proceed();
            System.out.println("MyAspect around end.");

        }catch (Throwable e){
            e.printStackTrace();
        }
        return obj;
    }
    public Object aroundInit(ProceedingJoinPoint pjp, String bizName, int times)
    {

        System.out.println(bizName + " " + times);
        Object obj = null;
        try{
            System.out.println("MyAspect aroundInit begin.");
            obj = pjp.proceed();
            System.out.println("MyAspect aroundInit end.");

        }catch (Throwable e){
            e.printStackTrace();
        }
        return obj;
    }




}

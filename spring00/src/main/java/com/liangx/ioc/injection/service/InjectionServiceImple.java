package com.liangx.ioc.injection.service;

import com.liangx.ioc.injection.dao.InjectionDAO;

public class InjectionServiceImple implements InjectionService{

    private InjectionDAO injectionDAO;

    //构造注入
    public InjectionServiceImple(InjectionDAO injectionDAO)
    {
        this.injectionDAO = injectionDAO;
    }

    //设值注入
    public void setInjectionDAO(InjectionDAO injectionDAO)
    {
        this.injectionDAO = injectionDAO;
    }

    public void save(String arg)
    {
        //模拟业务操作
        System.out.println("Service接收参数：" + arg);
        arg = arg + this.hashCode();
        injectionDAO.save(arg);
    }
}

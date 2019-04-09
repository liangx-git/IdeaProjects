package com.liangx.beanannotation.injection.service;

import com.liangx.beanannotation.injection.dao.InjectionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowire;


@Service
public class InjectionServiceImpl implements InjectionService{

    @Autowired
    private InjectionDAO injectionDAO;

    //@Autowired
//    public InjectionServiceImpl(InjectionDAO injectionDAO)
//    {
//        this.injectionDAO = injectionDAO;
//    }

    //@Autowired
//    public void setInjectionDAO(InjectionDAO injectionDAO)
//    {
//        this.injectionDAO = injectionDAO;
//    }

    public void save(String arg)
    {
        //模拟业务
        System.out.println("Service接受参数：" + arg);
        arg = arg + ":" + this.hashCode();
        injectionDAO.save(arg);
    }
}

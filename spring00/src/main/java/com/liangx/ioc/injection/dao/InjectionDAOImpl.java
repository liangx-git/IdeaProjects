package com.liangx.ioc.injection.dao;


public class InjectionDAOImpl implements InjectionDAO{

    public void save(String arg)
    {
        //模拟数据库保存数据操作
        System.out.println("保存数据:" + arg);
    }
}

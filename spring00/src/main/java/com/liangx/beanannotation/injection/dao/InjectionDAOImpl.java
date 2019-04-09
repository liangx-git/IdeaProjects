package com.liangx.beanannotation.injection.dao;

import org.springframework.stereotype.Repository;

@Repository
public class InjectionDAOImpl implements InjectionDAO{
    @Override
    public void save(String arg) {
        //模拟存储参数
        System.out.println("存储参数: " + arg);
    }
}

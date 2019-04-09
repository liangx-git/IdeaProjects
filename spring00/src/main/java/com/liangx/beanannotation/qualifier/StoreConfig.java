package com.liangx.beanannotation.qualifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StoreConfig {

    //@Autowired
    //private Store<String> s1;

    @Autowired
    private Store<Integer> s2;

    @Bean
    public StringStore stringStore()
    {
        return new StringStore();
    }

    @Bean
    public IntegerStore integerStore()
    {
        return new IntegerStore();
    }

    @Bean(name = "stringStoreTest")
    public StringStore stringStoreTest()
    {
        //System.out.println("s1: " + s1.getClass().getName());
        System.out.println("s2: " + s2.getClass().getName());
        return new StringStore();
    }
}

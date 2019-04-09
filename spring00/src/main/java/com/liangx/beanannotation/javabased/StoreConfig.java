package com.liangx.beanannotation.javabased;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@ImportResource("classpath:config.xml")
public class StoreConfig {

    @Value("${jdbc.url}")
    private String url;

    @Value("${jdbc.username}")
    private String username;

    @Value("${jdbc.password}")
    private String password;

//    @Bean(name = "stringStore", initMethod="init", destroyMethod="destroy")
//    public Store getStringStore()
//    {
//        return new StringStore();
//    }

    @Bean
    public MyDriverManager myDriverManager()
    {
        return new MyDriverManager(url, username, password);
    }

}

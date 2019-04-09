package com.liangx.resource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import java.io.IOError;
import java.io.IOException;


public class SpringResource implements ApplicationContextAware
{
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext =  applicationContext;
    }

    public void resource() throws IOException
    {
        //get resource by classpath
        Resource resource = applicationContext.getResource("classpath:config.txt");

        //get resource by file path
        //Resource resource = applicationContext.getResource("file:/home/liangx/IdeaProjects/spring00/src/main/resource/config.txt");

        //get resource by http
        //Resource resource = applicationContext.getResource("url:http://docs.spring.io/spring/docs/4.0.5.RELEASE/spring-framework-reference/htmlsingle/");

        System.out.println(resource.getFilename());
        System.out.println(resource.contentLength());
    }
}

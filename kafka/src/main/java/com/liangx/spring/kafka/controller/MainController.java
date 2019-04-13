package com.liangx.spring.kafka.controller;

import com.google.gson.Gson;
import com.liangx.spring.kafka.consumer.Impl.WebKafkaConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@RequestMapping("/waterLevelInformation")
public class MainController{

    @RequestMapping("/monitor")
    public ModelAndView hello(){
        ModelAndView model = new ModelAndView();
        model.setViewName("monitor");
        return model;
    }

}

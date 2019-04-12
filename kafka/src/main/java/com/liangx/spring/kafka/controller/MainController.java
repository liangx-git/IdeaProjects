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

    private Gson gson = new Gson();

    @Autowired
    private WebKafkaConsumer webKafkaConsumer;

    @RequestMapping("/monitor")
    public ModelAndView hello(){
        ModelAndView model = new ModelAndView();
        model.setViewName("monitor");
        return model;
    }

    @RequestMapping("/echart")
    public ModelAndView chart(){
        ModelAndView model = new ModelAndView("echart");
        return model;

    }

    @RequestMapping("/records")
    public ModelAndView testjs(){
        ModelAndView model = new ModelAndView();
        model.addObject("3");
        model.addObject("9");
        model.addObject("23");
        model.addObject("28");
        return model;
    }
}

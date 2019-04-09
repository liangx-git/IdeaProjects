package com.liangx.spring.kafka.controller;

import com.google.gson.Gson;
import com.liangx.spring.kafka.common.ErrorCode;
import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.common.Response;
import com.liangx.spring.kafka.consumer.KafkaConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@RequestMapping("/waterLevelInformation")
public class MainController{

    private Gson gson = new Gson();

    @Autowired
    private KafkaConsumer kafkaConsumer;

    @RequestMapping(value="/monitor", method= RequestMethod.GET)
    public String monitor(Model model){
        kafkaConsumer.startListenerForWeb();
        return "monitor";
    }

    @RequestMapping("/hello")
    public ModelAndView hello(){
        ModelAndView model = new ModelAndView();
        model.addObject("hello", "hello world");
        model.setViewName("hello");
        return model;
    }
}

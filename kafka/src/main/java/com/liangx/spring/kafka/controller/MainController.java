package com.liangx.spring.kafka.controller;

import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.utils.PreparedBufferUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.jws.WebParam;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/waterLevelInformation")
public class MainController{

    @Autowired
    private PreparedBufferUtil preparedBufferUtil;

    @RequestMapping("/monitor")
    public ModelAndView hello(){
        ModelAndView model = new ModelAndView();
        model.setViewName("monitor.html");
        return model;
    }

    @RequestMapping("/hourlymonitor")
    public ModelAndView hourlyMonitor(){
        ModelAndView model = new ModelAndView();
        model.setViewName("hourlymonitor.html");
        return model;
    }

}

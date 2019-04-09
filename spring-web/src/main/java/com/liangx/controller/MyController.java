package com.liangx.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller("myController")
@RequestMapping("/my")
public class MyController {


    @ResponseBody
    @RequestMapping("/index")
    public ModelAndView index(){

        //模型和视图
        ModelAndView mv = new ModelAndView();

        //视图逻辑为index
        mv.setViewName("index");

        //返回模型和试图
        return mv;
    }

    @RequestMapping("/hello")
    public String helloMvc()
    {
        return "home";
    }
}

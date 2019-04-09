package com.liangx.autowiring;

public class AutoWiringService {

    private AutoWiringDAO autoWiringDAO;

    //构造器自动装配
    public AutoWiringService(AutoWiringDAO autoWiringDAO)
    {
        System.out.println("AutoWiringService constructor");
        this.autoWiringDAO = autoWiringDAO;
    }

    //非构造器自动装配
    public void setAutoWiringDAO(AutoWiringDAO autoWiringDAO)
    {
        this.autoWiringDAO = autoWiringDAO;
    }

    public void say(String word)
    {
        autoWiringDAO.say(word);
    }
}

package com.liangx.beanannotation;

import org.springframework.stereotype.Component;

@Component
public class BeanAnnotation {

    public void say(String word)
    {
        System.out.println("Beanannotation: " + word);
    }
}

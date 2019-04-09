package com.liangx.beanannotation;

import com.liangx.beanannotation.javabased.MyDriverManager;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.Test;
import com.liangx.base.UnitTestBase;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestBeanannotation extends UnitTestBase{

    public TestBeanannotation()
    {
        super("classpath:/META-INF/spring-beanannotation.xml");
    }

    @Test
    public void testSay()
    {
        BeanAnnotation beanAnnotation = super.getBean("beanAnnotation");
        beanAnnotation.say("This is a test");
    }

    @Test
    public void testMyDriverManager()
    {
        MyDriverManager manager = super.getBean("myDriverManager");
        System.out.println(manager.getClass().getName());
    }

}

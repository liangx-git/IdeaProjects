package com.liangx.aware;

import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.Test;

import com.liangx.base.UnitTestBase;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestAware extends UnitTestBase{

    public TestAware()
    {
        super("classpath:META-INF/spring-aware.xml");
    }

    @Test
    public void testApplicationContext()
    {
        System.out.println("testApplicationContext: " + super.getBean("springApplicationContext").hashCode());
    }
}

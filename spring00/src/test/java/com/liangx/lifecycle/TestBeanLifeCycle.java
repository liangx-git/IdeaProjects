package com.liangx.lifecycle;

import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.Test;
import com.liangx.base.UnitTestBase;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestBeanLifeCycle extends UnitTestBase{

    public TestBeanLifeCycle()
    {
        super("classpath:META-INF/spring-lifecycle.xml");
    }

    @Test
    public void test()
    {
        BeanLifeCycle beanLifeCycle = super.getBean("beanLifeCycle");
    }
}

package com.liangx.bean;

import com.liangx.base.UnitTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestBeanScop extends UnitTestBase {

    public TestBeanScop()
    {
        super("/META-INF/spring-beanscope.xml");
    }

    @Test
    public void testSay()
    {
        BeanScope testBean = super.getBean("beanScope");
        testBean.say();

        BeanScope testBean2 = super.getBean("beanScope");
        testBean2.say();
    }


}

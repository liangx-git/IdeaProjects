package com.liangx.beanannotation.injection;

import com.liangx.beanannotation.multibean.BeanInterface;
import com.liangx.beanannotation.multibean.BeanInvoker;
import jdk.nashorn.internal.ir.Block;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.Test;

import com.liangx.base.UnitTestBase;
import com.liangx.beanannotation.injection.service.InjectionService;
import org.springframework.context.annotation.Bean;


@RunWith(BlockJUnit4ClassRunner.class)
public class TestInjection extends UnitTestBase{

    public TestInjection()
    {
        super("classpath:META-INF/spring-beanannotation.xml");
    }

    @Test
    public void testAutowired()
    {
        InjectionService service = super.getBean("injectionServiceImpl");
        service.save("This is a aware");

    }

    @Test
    public void testMultiBean()
    {
        BeanInvoker invoker = super.getBean("beanInvoker");
        invoker.say();
    }
}

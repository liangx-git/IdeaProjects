package com.liangx.ioc.interfaces;

import com.liangx.base.UnitTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestOneInterface extends UnitTestBase {
    public TestOneInterface()
    {
        super("classpath:META-INF/applicationContext.xml");
    }

    @Test
    public void testHello()
    {
        OneInterface oneInterface = super.getBean("oneInterface");
        System.out.println(oneInterface.hello("spring"));
    }
}

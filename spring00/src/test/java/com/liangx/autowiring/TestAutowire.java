package com.liangx.autowiring;

import com.liangx.base.UnitTestBase;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.Test;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestAutowire extends UnitTestBase{

    public TestAutowire()
    {
        super("classpath*:/META-INF/spring-bean.xml");
    }

    @Test
    public void testSay()
    {
        AutoWiringService service = super.getBean("autoWiringService");
        service.say("this is a test");
    }
}

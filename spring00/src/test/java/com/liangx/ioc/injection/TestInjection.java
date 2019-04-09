package com.liangx.ioc.injection;

import com.liangx.base.UnitTestBase;
import com.liangx.ioc.injection.service.InjectionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestInjection extends UnitTestBase
{

    public TestInjection()
    {
        super("classpath*:/META-INF/applicationContext.xml");
    }

    @Test
    public void testSetter()
    {
        InjectionService service = super.getBean("injectionService");
        service.save("这是要保存的数据");
    }

    @Test
    public void testCons()
    {
        InjectionService service = super.getBean("injectionService");
        service.save("gg");
    }
}

package com.liangx.resource;

import com.liangx.base.UnitTestBase;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.Test;

import javax.swing.*;
import java.io.IOException;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestSpringResource extends UnitTestBase{

    public TestSpringResource()
    {
        super("classpath:/META-INF/spring-bean.xml");
    }


    @Test
    public void testResource() throws IOException {
        SpringResource springResouce = super.getBean("springResource");
        springResouce.resource();
    }

}

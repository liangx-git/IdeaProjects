package com.liangx.beanannotation.qualifier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import com.liangx.base.UnitTestBase;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestQualifier extends UnitTestBase{

    public TestQualifier()
    {
        super("classpath:META-INF/spring-beanannotation-qualifier.xml");
    }

    @Test
    public void testG()
    {
        StringStore store = super.getBean("stringStoreTest");
    }
}

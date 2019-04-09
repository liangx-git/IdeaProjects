package com.liangx.beanannotation.javabased;


import com.liangx.base.UnitTestBase;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.Test;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestJavaBased extends UnitTestBase{

    public TestJavaBased()
    {
        super("classpath:META-INF/spring-beanannotation.xml");
    }

    @Test
    public void test()
    {
        Store store = super.getBean("stringStore");
        System.out.println(store.getClass().getName());
    }

}

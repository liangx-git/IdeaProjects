package com.liangx.aop;

import com.liangx.aop.schema.advice.AspectBiz;
import com.liangx.aop.schema.advice.Fit;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.Test;

import com.liangx.base.UnitTestBase;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestAopSchemaAdvice extends UnitTestBase{

    public TestAopSchemaAdvice()
    {
        super("classpath:META-INF/spring-aop-schema-advice.xml");
    }

    @Test
    public void testBiz() {
        AspectBiz biz = super.getBean("aspectBiz");
        biz.biz();
    }

    @Test
    public void testInit() {
        AspectBiz biz = super.getBean("aspectBiz");
        biz.init("myService", 3);

    }

    @Test
    public void testFit(){
        Fit fit = (Fit)super.getBean("aspectBiz");
        fit.filter();
    }

}

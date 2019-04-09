package com.liangx.beanannotation.multibean;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.List;
import java.util.Map;

@Component
public class BeanInvoker {

    @Autowired
    private List<BeanInterface> list;

    @Autowired
    private Map<String, BeanInterface> map;

    @Qualifier("beanImplOne")
    @Autowired
    private BeanInterface beanInterfaceImpl;

    public void say()
    {
        if (null != list && 0 != list.size())
        {
            System.out.println("list....");
            for (BeanInterface bean : list)
            {
                System.out.println(bean.getClass().getName());
            }
        }
        else
        {
            System.out.println("List<BeanInterface> list is null!!!");
        }

        if (null != map && 0 != map.size())
        {
            System.out.println("map...");
            for (Map.Entry<String, BeanInterface> entry : map.entrySet())
            {
                System.out.println(entry.getKey() + "  " + entry.getValue().getClass().getName());
            }
        }
        else
        {
            System.out.println("Map<String, BeanInterface> map is null !!!");
        }

        System.out.println();
        if (null != beanInterfaceImpl)
        {
            System.out.println(beanInterfaceImpl.getClass().getName());
        }
        else
        {
            System.out.println("beanInterfaceImpl is null");
        }
    }
}

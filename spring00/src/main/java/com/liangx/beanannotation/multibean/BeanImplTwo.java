package com.liangx.beanannotation.multibean;

import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

@Order(1)
@Component
public class BeanImplTwo implements BeanInterface{
}

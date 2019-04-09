package com.liangx.beanannotation.multibean;

import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

@Order(2)
@Component
public class BeanImplOne implements BeanInterface{
}

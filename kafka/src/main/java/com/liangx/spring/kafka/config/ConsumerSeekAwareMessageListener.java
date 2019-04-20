package com.liangx.spring.kafka.config;

import org.springframework.kafka.listener.AcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.listener.MessageListener;

public interface ConsumerSeekAwareMessageListener<K, V> extends AcknowledgingConsumerAwareMessageListener<K, V>, ConsumerSeekAware {
}

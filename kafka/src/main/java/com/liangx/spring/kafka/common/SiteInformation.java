package com.liangx.spring.kafka.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class SiteInformation {

    private int siteId = 0;

    private String siteName = "";
}

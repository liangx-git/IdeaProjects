package com.liangx.spring.kafka.common;

//import jdk.nashorn.internal.objects.annotations.Constructor;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;

//@Getter、@Setter、@AllArgConstructor、@NoArgsConstructor、@EqualAndHashCode有lombok插件提供
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class WaterLevelRecord implements Serializable {

    private Timestamp time;

    private int siteId;

    private String siteName;

    private Double waterLevel;

    @Override
    public String toString(){
        return "WaterLevelRecord{" +
                "time = ‘" + time + '\'' +
                "siteId = ‘" + siteId + '\'' +
                "siteName = ‘" + siteName + '\'' +
                "waterLevel = ‘" + waterLevel + '\'' +
                '}';
    }
}

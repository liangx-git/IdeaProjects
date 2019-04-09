package com.liangx.spring.kafka.producer;

import com.google.gson.Gson;
import com.liangx.spring.kafka.common.WaterLevelRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;

import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

public class ProducerCallback implements ListenableFutureCallback<SendResult<String, WaterLevelRecord>> {

    private final long startTime;
    private final String key;
    private final WaterLevelRecord message;

    private final Gson gson = new Gson();

    public ProducerCallback(long startTime, String key, WaterLevelRecord message){
        this.startTime = startTime;
        this.key = key;
        this.message = message;
    }

    @Override
    public void onFailure(Throwable throwable){
        throwable.printStackTrace();
    }

    @Override
    public void onSuccess(SendResult<String, WaterLevelRecord> result) {
       if (null == result){
           return;
       }

       long elapsedTime = System.currentTimeMillis() - startTime;
       RecordMetadata recordMetadata = result.getRecordMetadata();
       if (null == recordMetadata){
           StringBuilder record = new StringBuilder();
           record.append("message(")
                   .append("key = ").append(key).append(",")
                   .append("message = ").append(gson.toJson(message)).append(")")
                   .append("send to partition(").append(recordMetadata.partition()).append(")")
                   .append("with offset(").append(recordMetadata.offset()).append(")")
                   .append("in").append(elapsedTime).append("ms");
           log.info(record.toString());
       }


    }
}

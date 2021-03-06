package com.liangx.spring.kafka;

import com.liangx.spring.kafka.common.WaterLevelRecord;
import com.liangx.spring.kafka.services.DataPersistenceService.RecordDurableService.WaterLevelRecordService;
import com.liangx.spring.kafka.producer.KafkaProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KafkaApplicationTests {

    //Autowired使用Type匹配方式自动装配，使用@Qualifier可以使得装载按照Name方式，
    // 从而避免自动装载时多个类型相同的实例产生歧义。等效与@Resource
    @Autowired
    @Qualifier("defaultKafkaTemplate")
    private KafkaTemplate kafkaTemplate;
//
//    private WaterLevelRecord message = new WaterLevelRecord("title-test", "body-SpringBootKafka");
//
//    //@Autowired
//    //private AdminClient adminClient;
//
//    //用于消息回调的监听类
//    @Autowired
//    private KafkaSendResultHandler producerListener;
//
//    @Test
//    public void contextLoads() {
//        WaterLevelRecord message = new WaterLevelRecord();
//        message.setTitle("test");
//        message.setBody("SpringBoot-kafka");
//        kafkaTemplate.send("test-message", message);
//    }
//
////    @Test
////    public void testCreateTopic() throews InterruptedException {
////        NewTopic topic = new NewTopic("topic-by-coding", 1, (short)1);
////        adminClient.createTopics(Arrays.asList(topic));
////        Thread.sleep(1000);
////    }
//
//    @Test
//    public void testDefaultKafkaTemplate(){
//        kafkaTemplate.sendDefault(message);
//    }
//
//    @Test
//    public void testProoducerListen() throws InterruptedException, TimeoutException, ExecutionException {
//        kafkaTemplate.setProducerListener(producerListener);
//
//        //send方法默认是异步发送消息，如果需要同步发送消息则在send()方法后面调用get()即可。
//        //kafkaTemplate.send("liangx-message", new WaterLevelRecord("test", "ProducerListen"));
//        //kafkaTemplate.send("liangx-message", new WaterLevelRecord("test", "kafkaTemplate.send().get()")).get(1000, TimeUnit.MICROSECONDS);
//        kafkaTemplate.send("liangx-message", new WaterLevelRecord("test", "kafkaTemplate.send().get()")).get();
//        Thread.sleep(1000);
//    }
//
//    //测试事务
////    @Test
////    @Transactional
////    public void testTransactionlAnnotation(){
////        kafkaTemplate.send("liangx-message", new WaterLevelRecord("test", "test transactionl annotation"));
////        //throw new RuntimeException("fail");
////    }
//    //使用KafkaTemplate.executeInTransaction开启事务
////    @Test
////    public void testExecuteInTransaction(){
////        kafkaTemplate.executeInTransaction(new KafkaOperations.OperationsCallback(){
////            @Override
////            public Object doInOperations(KafkaOperations kafkaOperations) {
////                kafkaOperations.send("liangx-message", new WaterLevelRecord("test", "test executeInTrasaction"));
////                return null;
////            }
////        });
////    }
//
//    @Test
//    public void testKafkaListenerContainer(){
//        kafkaTemplate.send("liangx-message", new WaterLevelRecord("test", "test KafkaListenerContainer"));
//    }

    @Autowired
    private KafkaProducer kafkaProducer;

    //模拟producer发送数据
    @Test
    public void testSimulateSensorCreateMessage() throws InterruptedException {
        Random random = new Random();
        DecimalFormat df = new DecimalFormat("0.0");
        int i = 0;
        double water_level = 50.0;
        while(true){
            double inc = (random.nextDouble()*(15 - 0)) - 7;
            water_level += (water_level + inc) > 20 ? ((water_level + inc) < 100 ? inc : 0) : 0;     //随机生成的water_leve区间为(20, 80)
            water_level = Double.valueOf(df.format(water_level));
            WaterLevelRecord waterLevelRecord = new WaterLevelRecord(new Timestamp(System.currentTimeMillis()), 104, "hohai", water_level);
            kafkaProducer.send("liangx-message", waterLevelRecord);
            Thread.sleep(3000);
        }
    }

    @Test
    public void createTimestamp() throws InterruptedException {
        Calendar calendar = Calendar.getInstance();
        for (int i = 1; i <= 10; ++i){
            calendar.add(Calendar.MONTH, -1);    //每次循环倒退一个月
           for(int j = 1; j <= 10; ++j){
               calendar.add(Calendar.DAY_OF_MONTH, -1);
//               System.out.println(new Timestamp(calendar.getTimeInMillis()));
               for (int k = 1; k <= 5; ++k){
                   calendar.add(Calendar.HOUR, -1);
                   createMessage(new Timestamp(calendar.getTimeInMillis()));
               }
           }
        }
    }

    public void createMessage(Timestamp timestamp) throws InterruptedException {
        Random random = new Random();
        DecimalFormat df = new DecimalFormat("0.0");
        int i = 0;
        double water_level = 50.0;
        double inc = (random.nextDouble()*(15 - 0)) - 7;
        water_level += (water_level + inc) > 20 ? ((water_level + inc) < 100 ? inc : 0) : 0;     //随机生成的water_leve区间为(20, 80)
        water_level = Double.valueOf(df.format(water_level));
        WaterLevelRecord waterLevelRecord = new WaterLevelRecord(timestamp, 104, "hohai", water_level);
        kafkaProducer.send("liangx-message", waterLevelRecord);
        Thread.sleep(10);
    }


    @Autowired
    private WaterLevelRecordService waterlevelRecordService;


    @Test
    public void testGetAvgWaterLevel(){

        Calendar calendar = Calendar.getInstance();
        Timestamp endTime = new Timestamp(calendar.getTimeInMillis());
        calendar.add(Calendar.HOUR, - 1);
        Timestamp beginTime = new Timestamp(calendar.getTimeInMillis());
        System.out.println("beginTime = " + beginTime + " endTime = " + endTime);
        Double avgWaterLevel = waterlevelRecordService.getAvgWaterLevelByInterval(beginTime, endTime);
        System.out.println("beginTime = " + beginTime + " endTime = " + endTime + " water level = " + avgWaterLevel);
//        System.out.println("timestamp = " + System.currentTimeMillis() + " avgWaterLevel = " + avgWaterLevel);
//        System.out.println(new Timestamp(System.currentTimeMillis()));
    }


    @Test
    public void testDatas(){
       Calendar calendar = Calendar.getInstance();
       Timestamp timestamp1 = new Timestamp(calendar.getTimeInMillis());
       calendar.add(Calendar.SECOND, -10);
       Timestamp timestamp2 = new Timestamp(calendar.getTimeInMillis());
       boolean gt = timestamp1.getTime() == timestamp2.getTime() + 10000;
       System.out.println("timestamp1 = " + timestamp1 + " timestamp2 = " + timestamp2);
       System.out.println("timestamp1 == timestamp2 + 10000 = " + gt);



    }
}



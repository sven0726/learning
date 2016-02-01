package info.geedoo;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by qiniu on 16/1/11.
 */
public class AvroConsumer {

    private void consumer(Schema schema) throws Exception {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<String, byte[]>(props);
        consumer.subscribe("test1","test2");
        Map<String,ConsumerRecords<String, byte[]>> records = consumer.poll(1000);
        for(String key : records.keySet() ){
            System.out.println(key);
            ConsumerRecords<String,byte[]> res = records.get(key);
            System.out.print("---topic : " + res.topic() );
            for (ConsumerRecord<String,byte[]> cr : res.records()){
                System.out.print("-------value: " + new String(cr.value(),"UTF-8"));
            }

        }
//        consumer.subscribe(Arrays.asList("test1"));
//        List<GenericRecord> res = new ArrayList<GenericRecord>(10);
//        while (true) {
//            ConsumerRecords<String, byte[]> records = consumer.poll(100);
//            for (ConsumerRecord<String, byte[]> record : records ){
//                System.out.printf("offset = %d, key = %s, value = %s", record.offset(), record.key(), record.value());
//            }
//
//        }

    }

    public static void main(String[] args) throws Exception{
        AvroConsumer consumer = new AvroConsumer();
        Schema schema = new Schema.Parser().parse(new File("test_schema.avsc"));
        consumer.consumer(schema);
    }
}

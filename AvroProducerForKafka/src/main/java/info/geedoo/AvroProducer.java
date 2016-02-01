package info.geedoo;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.producer.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by qiniu on 16/1/11.
 */
public class AvroProducer {


    private void producer(Schema schema) throws IOException{
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");  //new api 2.9.0
        props.put("serializer.class", "kafka.serializer.DefaultEncoder");
        props.put("request.required.acks", "1");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        DatumWriter<GenericRecord> writer = new SpecificDatumWriter<GenericRecord>(schema);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream,null);
        Producer<String, byte[]> producer = new KafkaProducer(props);
        for(int i = 0; i < 100; i++){
            GenericRecord payload = new GenericData.Record(schema);
            payload.put("desc", "testdata" + i);
            payload.put("name", "name" + i);
            payload.put("id", i);
            System.out.println("Original Message : " + payload);

            writer.write(payload, encoder);

            byte[] serializedBytes = outputStream.toByteArray();
            System.out.println("Sending message in bytes : " + serializedBytes);
            //String serializedHex = Hex.encodeHexString(serializedBytes);
            //System.out.println("Serialized Hex String : " + serializedHex);

            producer.send(new ProducerRecord<String, byte[]>("test1", Integer.toString(i), serializedBytes));

        }
        encoder.flush();
        outputStream.close();
        producer.close();

    }

    public static void main(String[] args) throws IOException{
        AvroProducer producer = new AvroProducer();
        Schema schema = new Schema.Parser().parse(new File("test_schema.avsc"));
        producer.producer(schema);
    }
}
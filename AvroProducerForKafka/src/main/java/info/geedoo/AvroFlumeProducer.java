package info.geedoo;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.flume.source.avro.AvroFlumeEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by qiniu on 16/1/11.
 */
public class AvroFlumeProducer {

    public static final String SERVICE = "service";
    public static final String IDC = "idc";
    public static final String TIMESTAMP = "timestamp";

    private void producer(Properties p) throws IOException{
        Properties props = new Properties();
        //props.put("bootstrap.servers", "localhost:9092");   //new api  2.9.0
        props.put("metadata.broker.list", p.getProperty("metadata.broker.list","localhost:9092"));
        props.put("serializer.class", "kafka.serializer.DefaultEncoder");
        props.put("request.required.acks", "1");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        //props.put("value.serializer", "info.geedoo.AvroFlumeSerializer");


        //Producer<String, AvroFlumeEvent> producer = new KafkaProducer(props);
        Producer<String, byte[]> producer = new Producer(new ProducerConfig(props));
        for(int i = 0; i < 10000; i++){
            AvroFlumeEvent event = buildEvent(i);
            System.out.println("Original Message : " + event);

            DatumWriter<AvroFlumeEvent> writer = new SpecificDatumWriter<AvroFlumeEvent>(AvroFlumeEvent.class);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            writer.write(event, encoder);
            encoder.flush();
            out.close();

            byte[] serializedBytes = out.toByteArray();

            //producer.send(new ProducerRecord<String, AvroFlumeEvent>("all-log", Integer.toString(i), event));
            KeyedMessage<String,byte[]> message =  new KeyedMessage<String,byte[]>("all-log", serializedBytes);
            producer.send(message);

        }
        producer.close();

    }

    private static Map<String, String> toStringMap(Map<CharSequence, CharSequence> charSeqMap) {
        Map<String, String> stringMap = new HashMap<String, String>();
        for (Map.Entry<CharSequence, CharSequence> entry : charSeqMap.entrySet()) {
            stringMap.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return stringMap;
    }
    private static AvroFlumeEvent buildEvent(int i) {
        AvroFlumeEvent event = new AvroFlumeEvent();
        Map<CharSequence, CharSequence> headers = new HashMap<CharSequence, CharSequence>();
        headers.put(SERVICE, i%3==0?"io":"up");
        headers.put(IDC, i%2==0?"bc":"nb");
        headers.put(TIMESTAMP, String.valueOf(System.currentTimeMillis()));

        event.setHeaders(headers);

        String body = "Body: [SERVICE:" + headers.get(SERVICE) + " IDC=" + headers.get(IDC) + " NUM=" + i + " TIMESTAMP=" +
                headers.get(TIMESTAMP) + "]";
        event.setBody(ByteBuffer.wrap(body.getBytes()));
        return event;
    }

    public static void main(String[] args) throws IOException{
        Properties properties = new Properties();
        if (args.length==1){
            properties.setProperty("metadata.broker.list",args[0].trim());
        }
        AvroFlumeProducer producer = new AvroFlumeProducer();
        //Schema schema = new Schema.Parser().parse(new File("test_schema.avsc"));
        producer.producer(properties);
    }
}
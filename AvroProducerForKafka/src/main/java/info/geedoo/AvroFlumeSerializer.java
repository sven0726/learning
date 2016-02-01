package info.geedoo;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.flume.source.avro.AvroFlumeEvent;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by qiniu on 16/1/11.
 */
public class AvroFlumeSerializer implements Serializer<AvroFlumeEvent> {
    DatumWriter<AvroFlumeEvent> writer;
    ByteArrayOutputStream outputStream ;
    BinaryEncoder encoder ;
    public void configure(Map configs, boolean isKey) {
        writer = new SpecificDatumWriter<AvroFlumeEvent>(AvroFlumeEvent.class);


    }

    public byte[] serialize(String topic, AvroFlumeEvent data) {
        outputStream = new ByteArrayOutputStream();
        encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
        try {
            writer.write(data, encoder);
            encoder.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    public void close() {
        try{
            outputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

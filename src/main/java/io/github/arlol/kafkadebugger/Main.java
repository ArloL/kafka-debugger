package io.github.arlol.kafkadebugger;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

public class Main {

	public static void main(String[] args) throws Exception {
		System.out.println("Connecting to " + args[0] + " and producing to " + args[1]);
		Properties props = new Properties();
		props.put("bootstrap.servers", args[0]);
		props.put(
				"key.serializer",
				"org.apache.kafka.common.serialization.StringSerializer"
		);
		props.put(
				"value.serializer",
				"org.apache.kafka.common.serialization.StringSerializer"
		);

		KafkaProducer<String, String> producer = new KafkaProducer<>(props);

		ProducerRecord<String, String> record = new ProducerRecord<>(
				args[1],
				0,
				"hello-world",
				"This is a test message!"
		);
		producer.send(record).get();

		producer.close();
	}

}

package io.github.arlol.kafkadebugger;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

@Testcontainers(disabledWithoutDocker = true)
class MainTest {

	private static final String TOPIC = "kafka-debugger-test";

	@Container
	private static final KafkaContainer BROKER = new KafkaContainer(
			"apache/kafka:4.3.1"
	);

	@Test
	void producesRecordToTopic() throws Exception {
		String bootstrapServers = BROKER.getBootstrapServers();
		createTopic(bootstrapServers);

		Main.main(new String[] { bootstrapServers, TOPIC });

		List<ConsumerRecord<String, String>> records = consume(
				bootstrapServers
		);

		assertThat(records).hasSize(1);
		ConsumerRecord<String, String> record = records.get(0);
		assertThat(record.topic()).isEqualTo(TOPIC);
		assertThat(record.partition()).isEqualTo(0);
		assertThat(record.key()).isEqualTo("hello-world");
		assertThat(record.value()).isEqualTo("This is a test message!");
	}

	private static void createTopic(String bootstrapServers) throws Exception {
		try (Admin admin = Admin
				.create(Map.of("bootstrap.servers", bootstrapServers))) {
			admin.createTopics(List.of(new NewTopic(TOPIC, 1, (short) 1)))
					.all()
					.get();
		}
	}

	private static List<ConsumerRecord<String, String>> consume(
			String bootstrapServers
	) {
		Properties props = new Properties();
		props.put("bootstrap.servers", bootstrapServers);
		props.put(
				"key.deserializer",
				"org.apache.kafka.common.serialization.StringDeserializer"
		);
		props.put(
				"value.deserializer",
				"org.apache.kafka.common.serialization.StringDeserializer"
		);

		List<ConsumerRecord<String, String>> result = new ArrayList<>();
		TopicPartition partition = new TopicPartition(TOPIC, 0);
		try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(
				props
		)) {
			consumer.assign(List.of(partition));
			consumer.seekToBeginning(List.of(partition));
			long deadline = System.nanoTime()
					+ Duration.ofSeconds(30).toNanos();
			while (result.isEmpty() && System.nanoTime() < deadline) {
				consumer.poll(Duration.ofSeconds(1))
						.records(partition)
						.forEach(result::add);
			}
		}
		return result;
	}

}

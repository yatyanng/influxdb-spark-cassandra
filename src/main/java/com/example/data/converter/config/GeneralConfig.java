package com.example.data.converter.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.example.data.converter.Constants;
import com.example.data.converter.StreamingTimerTask;
import com.example.data.converter.model.UsageFrequencyDeserializer;

@Configuration
@Lazy
@ComponentScan(basePackages = { Constants.BASE_PACKAGE })
public class GeneralConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;
	
	@Bean(name = Constants.USAGE_FREQUENCY_DESERIALIZER_PARAMS)
	public Map<String, Object> usageFrequencyDeserializerParams() {
		Map<String, Object> kafkaParams = new HashMap<>();
		kafkaParams.put("bootstrap.servers", bootstrapServers);
		kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UsageFrequencyDeserializer.class);
		kafkaParams.put("group.id", "consumer-group-usage-frequency");
		kafkaParams.put("auto.offset.reset", "latest");
		kafkaParams.put("enable.auto.commit", false);
		return kafkaParams;
	}
	
	@Bean 
	public StreamingTimerTask streamingTimerTask() {
		return new StreamingTimerTask();
	}
}

package com.example.data.converter.model;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class UsageFrequencyDeserializer implements Deserializer<UsageFrequency> {

	private static final Logger log = LoggerFactory.getLogger(UsageFrequencyDeserializer.class);

	@Override
	public void close() {
	}

	@Override
	public void configure(Map<String, ?> arg0, boolean arg1) {
	}

	@Override
	public UsageFrequency deserialize(String arg0, byte[] arg1) {
		ObjectMapper mapper = new ObjectMapper();
		UsageFrequency usageFrequency = null;
		try {
			usageFrequency = mapper.readValue(arg1, UsageFrequency.class);
		} catch (Exception e) {
			log.error("[deserialize]", e);
		}
		return usageFrequency;
	}

}

package com.example.data.converter.model;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class UsageFrequencySerializer implements Serializer<UsageFrequency> {

	private static final Logger log = LoggerFactory.getLogger(UsageFrequencySerializer.class);

	@Override
	public void configure(Map<String, ?> arg0, boolean arg1) {
	}

	@Override
	public byte[] serialize(String arg0, UsageFrequency arg1) {
		byte[] retVal = null;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			retVal = objectMapper.writeValueAsString(arg1).getBytes();
		} catch (Exception e) {
			log.error("[serialize]", e);
		}
		return retVal;
	}

	@Override
	public void close() {
	}
}

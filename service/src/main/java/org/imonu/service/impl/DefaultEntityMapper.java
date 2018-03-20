package org.imonu.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import org.imonu.service.EntityMapper;

public class DefaultEntityMapper implements EntityMapper {

	private final ObjectMapper objectMapper = new ObjectMapper();

	public DefaultEntityMapper() {
		this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		this.objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	}

	public Try<String> mapToJson(Object obj) {
		return Try.of(() -> this.objectMapper.writeValueAsString(obj));
	}

	public <T> Try<T> mapToObject(String json, Class<T> clazz) {
		return Try.of(() ->  this.objectMapper.readValue(json, clazz));
	}
}

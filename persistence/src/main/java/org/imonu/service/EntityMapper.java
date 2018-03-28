package org.imonu.service;

import io.vavr.control.Try;

public interface EntityMapper {

	Try<String> mapToJson(Object obj);
	<T> Try<T> mapToObject(String json, Class<T> clazz);

}

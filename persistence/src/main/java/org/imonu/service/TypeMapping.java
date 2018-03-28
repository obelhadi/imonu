package org.imonu.service;

public interface TypeMapping {

	<T> String getType(Class<T> clazz);
	<T> String getIndexName(Class<T> clazz);

}

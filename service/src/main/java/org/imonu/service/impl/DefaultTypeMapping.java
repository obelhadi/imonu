package org.imonu.service.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.imonu.domain.Event;
import org.imonu.domain.Profile;
import org.imonu.domain.Session;
import org.imonu.service.TypeMapping;

public class DefaultTypeMapping implements TypeMapping {

	Map<Class, String> typeMap = new ConcurrentHashMap<>();

	public DefaultTypeMapping() {
		this.typeMap.put(Event.class, Event.builder().build().getItemType());
		this.typeMap.put(Profile.class, Profile.builder().build().getItemType());
		this.typeMap.put(Session.class, Session.builder().build().getItemType());
		//this.typeMap.put(Segment.class, Segment.builder().build().getItemType());
	}

	@Override
	public <T> String getType(Class<T> clazz) {
		return this.typeMap.get(clazz);
	}

	@Override
	public <T> String getIndexName(Class<T> clazz) {
		return "imonu";
	}
}

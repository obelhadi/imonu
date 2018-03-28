package org.imonu.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.imonu.domain.Event;
import org.imonu.domain.Item;
import org.imonu.domain.Profile;
import org.imonu.domain.Session;
import org.imonu.service.TypeMapping;

import org.springframework.objenesis.ObjenesisHelper;

public class DefaultTypeMapping implements TypeMapping {

	private final Map<Class, String> typeMap = new ConcurrentHashMap<>();
	private final String indexName;
	private final List<String> itemsMonthlyIndexed;
	private final Map<String, String> indexNames;

	public DefaultTypeMapping(String indexName, List<String> itemsMonthlyIndexed, Map<String, String> indexNames) {
		this.indexName = indexName;
		this.itemsMonthlyIndexed = itemsMonthlyIndexed;
		this.indexNames = indexNames;

		this.typeMap.put(Event.class, Event.builder().build().getItemType());
		this.typeMap.put(Profile.class, Profile.builder().build().getItemType());
		this.typeMap.put(Session.class, Session.builder().build().getItemType());
	}

	@Override
	public <T> String getType(Class<T> clazz) {
		final String type = this.typeMap.get(clazz);
		if(type == null) {
			final Item item = (Item) ObjenesisHelper.newInstance(clazz);
			return item.getItemType();
		}
		return type;
	}

	@Override
	public <T> String getIndexName(Class<T> clazz) {
		final String itemType = getType(clazz);
		return indexNames.containsKey(itemType) ? indexNames.get(itemType) :
				(itemsMonthlyIndexed.contains(itemType) ? indexName + "-*" : indexName);
	}
}

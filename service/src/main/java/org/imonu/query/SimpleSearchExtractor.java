package org.imonu.query;

import java.util.List;
import java.util.function.Function;

import io.vavr.Tuple;
import io.vavr.Tuple3;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHits;
import org.imonu.domain.Item;
import org.imonu.service.EntityMapper;

public class SimpleSearchExtractor<T extends Item> implements Function<SearchResponse, Tuple3<List<T>, String, Long>>, ItemsExtractor {

	final private EntityMapper entityMapper;

	final private Class<T> clazz;

	public SimpleSearchExtractor(EntityMapper entityMapper, Class<T> clazz) {
		this.entityMapper = entityMapper;
		this.clazz = clazz;
	}

	@Override
	public Tuple3<List<T>, String, Long> apply(SearchResponse searchResponse) {
		final SearchHits hits = searchResponse.getHits();
		final List<T> items = extractItems(hits, this.clazz, this.entityMapper);
		return Tuple.of(items, searchResponse.getScrollId(), hits.getTotalHits());
	}

}

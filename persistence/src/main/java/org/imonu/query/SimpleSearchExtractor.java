package org.imonu.query;

import java.util.List;
import java.util.function.Function;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHits;
import org.imonu.service.EntityMapper;

public class SimpleSearchExtractor<T> implements Function<SearchResponse, SearchResult<T>>, ItemsExtractor {

	final private EntityMapper entityMapper;

	final private Class<T> clazz;

	public SimpleSearchExtractor(EntityMapper entityMapper, Class<T> clazz) {
		this.entityMapper = entityMapper;
		this.clazz = clazz;
	}


	@Override
	public SearchResult<T> apply(SearchResponse searchResponse) {
		final SearchHits hits = searchResponse.getHits();
		final List<T> items = extractItems(hits, this.clazz, this.entityMapper);
		return new <T>SearchResult<T>(items, searchResponse.getScrollId(), hits.getTotalHits());
	}
}

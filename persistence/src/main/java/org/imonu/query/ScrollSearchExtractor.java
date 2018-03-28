package org.imonu.query;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHits;
import org.imonu.service.EntityMapper;

public class ScrollSearchExtractor<T> implements Function<SearchResponse, SearchResult<T>>, ItemsExtractor {

	final private Client client;

	final private TimeValue keepAlive;

	final private EntityMapper entityMapper;

	final private Class<T> clazz;


	public ScrollSearchExtractor(Client client, TimeValue keepAlive, EntityMapper entityMapper, Class<T> clazz) {
		this.client = client;
		this.keepAlive = keepAlive;
		this.entityMapper = entityMapper;
		this.clazz = clazz;
	}


	@Override
	public SearchResult<T> apply(SearchResponse response) {
		List<T> result = new ArrayList<>();
		while (true) {
			final SearchHits hits = response.getHits();
			final List<T> items = extractItems(hits, this.clazz, this.entityMapper);
			result.addAll(items);
			response = this.client.prepareSearchScroll(response.getScrollId())
					.setScroll(this.keepAlive)
					.execute()
					.actionGet();
			if (response.getHits().getHits().length == 0) {
				break;
			}
		}
		client.prepareClearScroll().addScrollId(response.getScrollId()).execute().actionGet();
		return new SearchResult<>(result);
	}
}

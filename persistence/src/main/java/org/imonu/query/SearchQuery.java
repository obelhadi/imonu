package org.imonu.query;

import java.util.List;
import java.util.Optional;

import io.vavr.Tuple2;
import lombok.Getter;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.imonu.domain.PartialList;
import org.imonu.service.EntityMapper;

public class SearchQuery {

	public static final int defaultQueryLimit = 10;

	private final Client client;

	private final EntityMapper entityMapper;

	@Getter
	private QueryBuilder query;


	private QueryModifier queryModifier = new QueryModifier();


	private SearchQuery(Client client, EntityMapper entityMapper) {
		this.client = client;
		this.entityMapper = entityMapper;
	}


	public static SearchQuery of(Client client, EntityMapper entityMapper) {
		return new SearchQuery(client, entityMapper);
	}

	public SearchQuery index(String index) {
		this.queryModifier = this.queryModifier.withIndex(index);
		return this;
	}

	public SearchQuery type(String type) {
		this.queryModifier = this.queryModifier.withType(type);
		return this;
	}

	public SearchQuery offset(Integer offset) {
		this.queryModifier = this.queryModifier.withOffset(offset);
		return this;
	}

	public SearchQuery size(Integer size) {
		this.queryModifier = this.queryModifier.withSize(size);
		return this;
	}

	public SearchQuery routing(List<String> routing) {
		this.queryModifier = this.queryModifier.withRouting(routing);
		return this;
	}

	public SearchQuery queryBuilder(QueryBuilder query) {
		this.query = query;
		return this;
	}

	public <T> ScrollSearch<T> scroll(Class<T> clazz) {
		return scroll(null, clazz);
	}

	public <T> ScrollSearch<T> scroll(String scrollTimeValidity, Class<T> clazz) {
		return new ScrollSearch<>(scrollTimeValidity, clazz);
	}

	// Simple Search
	public <T> PartialList<T> search(Class<T> clazz) {
		return new SimpleSearch<>(clazz).list();

	}

	public <T> List<T> list(Class<T> clazz) {
		return this.search(clazz).getList();
	}

	public class ScrollSearch<T> {

		private String scrollTimeValidity;

		private Class<T> clazz;

		public ScrollSearch(String scrollTimeValidity, Class<T> clazz) {
			this.scrollTimeValidity = scrollTimeValidity;
			this.clazz = clazz;

		}

		public PartialList<T> fetchAll() {
			final TimeValue keepAlive = getScrollTimeOrDefault(scrollTimeValidity);
			final SearchRequestBuilder searchRequestBuilder = buildScrollQuery(keepAlive);
			final SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
			return new ScrollSearchExtractor<>(SearchQuery.this.client, keepAlive, SearchQuery.this.entityMapper, clazz)
					.andThen(result -> PartialList.<T>builder()
							.list(result.getItems())
							.totalSize(0L)
							.offset(Long.valueOf(SearchQuery.this.queryModifier.getOffset()))
							.pageSize(-1L)
							.build()
					).apply(searchResponse);
		}

		public PartialList<T> fetch() {
			final TimeValue keepAlive = getScrollTimeOrDefault(scrollTimeValidity);
			final SearchRequestBuilder searchRequestBuilder = buildScrollQuery(keepAlive);
			final SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
			return new SimpleSearchExtractor<>(SearchQuery.this.entityMapper, clazz)
					.andThen(result -> PartialList.<T>builder()
							.list(result.getItems())
							.scrollIdentifier(result.getScrollId())
							.scrollTimeValidity(scrollTimeValidity)
							.totalSize(result.getTotalHits())
							.offset(Long.valueOf(SearchQuery.this.queryModifier.getOffset()))
							.pageSize(Long.valueOf(SearchQuery.this.queryModifier.getSize()))
							.build()
					).apply(searchResponse);
		}


		private TimeValue getScrollTimeOrDefault(String scrollTimeValidity) {
			return Optional.ofNullable(scrollTimeValidity)
					.map(s -> TimeValue.parseTimeValue(s, TimeValue.timeValueHours(1), "scrollTimeValidity"))
					.orElse(TimeValue.timeValueHours(1));
		}

		private SearchRequestBuilder buildScrollQuery(TimeValue keepAlive) {

			if (SearchQuery.this.queryModifier.getOffset() == null) {
				SearchQuery.this.queryModifier = SearchQuery.this.queryModifier.withOffset(0);
			}
			if (SearchQuery.this.queryModifier.getSize() == null) {
				SearchQuery.this.queryModifier = SearchQuery.this.queryModifier.withSize(100);
			}

			SearchRequestBuilder searchRequestBuilder = client.prepareSearch(SearchQuery.this.queryModifier.getIndex())
					.setTypes(SearchQuery.this.queryModifier.getType())
					.setQuery(getQuery())
					.setScroll(keepAlive)
					.setFetchSource(true)
					.setFrom(SearchQuery.this.queryModifier.getOffset())
					.setSize(SearchQuery.this.queryModifier.getSize())
					.setVersion(true);

			if (SearchQuery.this.queryModifier.isRoutingNotEmpty()) {
				searchRequestBuilder.setRouting(SearchQuery.this.queryModifier.getRoutingAsArray());
			}
			searchRequestBuilder = addSortBy(searchRequestBuilder);
			return searchRequestBuilder;
		}
	}

	private SearchRequestBuilder addSortBy(SearchRequestBuilder searchRequestBuilder) {
		if (SearchQuery.this.queryModifier.getSortBy() != null) {
			final SortByExtractor sortByExtractor = SortByExtractor.from(SearchQuery.this.queryModifier.getSortBy()).build();
			for (GeoDistanceSortBuilder geoDistanceSortBuilder : sortByExtractor.getGeoDistancesSort()) {
				searchRequestBuilder = searchRequestBuilder.addSort(geoDistanceSortBuilder);
			}
			for (Tuple2<String, SortOrder> fieldSortOrder : sortByExtractor.getFieldsSort()) {
				searchRequestBuilder = searchRequestBuilder.addSort(fieldSortOrder._1(), fieldSortOrder._2());
			}
		}
		return searchRequestBuilder;
	}

	public class SimpleSearch<T> {
		private Class<T> clazz;

		public SimpleSearch(Class<T> clazz) {
			this.clazz = clazz;
		}

		public PartialList<T> list() {
			if (SearchQuery.this.queryModifier.getOffset() == null) {
				SearchQuery.this.queryModifier = SearchQuery.this.queryModifier.withOffset(0);
			}
			if (SearchQuery.this.queryModifier.getSize() == null) {
				SearchQuery.this.queryModifier = SearchQuery.this.queryModifier.withSize(defaultQueryLimit);
			}
			SearchRequestBuilder searchRequestBuilder = client.prepareSearch(queryModifier.getIndex())
					.setTypes(queryModifier.getType())
					.setQuery(query)
					.setFetchSource(true)
					.setFrom(SearchQuery.this.queryModifier.getOffset())
					.setSize(SearchQuery.this.queryModifier.getSize())
					.setVersion(true);

			if (SearchQuery.this.queryModifier.getRouting() != null && !SearchQuery.this.queryModifier.getRouting().isEmpty()) {
				searchRequestBuilder.setRouting(SearchQuery.this.queryModifier.getRouting().toArray(new String[SearchQuery.this.queryModifier.getRouting().size()]));
			}
			searchRequestBuilder = addSortBy(searchRequestBuilder);

			final SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
			return new SimpleSearchExtractor<>(SearchQuery.this.entityMapper, clazz)
					.andThen(result -> PartialList.<T>builder()
							.list(result.getItems())
							.scrollIdentifier(result.getScrollId())
							.totalSize(result.getTotalHits())
							.offset(Long.valueOf(SearchQuery.this.queryModifier.getOffset()))
							.pageSize(Long.valueOf(SearchQuery.this.queryModifier.getSize()))
							.build()
					).apply(searchResponse);
		}
	}
}

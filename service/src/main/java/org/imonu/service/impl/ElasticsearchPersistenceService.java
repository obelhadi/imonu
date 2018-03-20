
package org.imonu.service.impl;

import java.util.List;
import java.util.Optional;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.imonu.domain.Item;
import org.imonu.domain.PartialList;
import org.imonu.query.ScrollSearchBuilder;
import org.imonu.query.ScrollSearchExtractor;
import org.imonu.query.SimpleSearchExtractor;
import org.imonu.service.EntityMapper;
import org.imonu.service.PersistenceService;
import org.imonu.service.TypeMapping;

import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

@Slf4j
public class ElasticsearchPersistenceService implements PersistenceService {

	private final Client client;

	private final ElasticsearchTemplate elasticsearchTemplate;

	private final EntityMapper entityMapper;

	private final TypeMapping typeMapping;


	public ElasticsearchPersistenceService(Client client, ElasticsearchTemplate elasticsearchTemplate) {
		this.elasticsearchTemplate = elasticsearchTemplate;
		this.client = client;
		this.entityMapper = new DefaultEntityMapper();
		typeMapping = new DefaultTypeMapping();
	}

	public <T extends Item> List<T> getAllItems(Class<T> clazz) {
		return queryScroll(QueryBuilders.matchAllQuery(), clazz, 0, null, null).getList();
	}

	public Try<Item> save(Item item) {
		return this.entityMapper.mapToJson(item)
				.flatMap(jsonSource ->
						Try.of(() -> this.client.prepareIndex()
								.setId(item.getItemId())
								.setIndex(this.typeMapping.getIndexName(item.getClass()))
								.setType(this.typeMapping.getType(item.getClass()))
								.setSource(jsonSource, XContentType.JSON)
								.execute()
								.actionGet())
				).map(d -> item);
	}

	public <T extends Item> Optional<T> load(String itemId, Class<T> clazz) {
		return Try.of(() ->
				client.prepareGet(this.typeMapping.getIndexName(clazz), this.typeMapping.getType(clazz), itemId)
						.execute()
						.actionGet())
				.filter(GetResponse::isExists)
				.map(GetResponse::getSourceAsString)
				.flatMap(source -> this.entityMapper.mapToObject(source, clazz))
				.toJavaOptional();
	}


	private <T extends Item> PartialList<T> query(final QueryBuilder query, final Class<T> clazz, final int offset, final int size, final List<String> routing, final String scrollTimeValidity) {

		final SearchResponse searchResponse = new ScrollSearchBuilder()
				.withQuery(query)
				.withClient(client)
				.withIndexName(getIndexName(clazz))
				.withType(getType(clazz))
				.withOffset(offset)
				.withSize(size)
				.withRouting(routing)
				.withScrollTimeValidity(scrollTimeValidity)
				.build()
				.execute()
				.actionGet();

		return new SimpleSearchExtractor<>(entityMapper, clazz)
				.andThen(tuple3 -> PartialList.<T>builder()
						.list(tuple3._1)
						.scrollIdentifier(tuple3._2)
						.totalSize(tuple3._3)
						.offset((long) offset)
						.pageSize((long) size)
						.build()
				).apply(searchResponse);

	}

	private <T extends Item> PartialList<T> queryScroll(final QueryBuilder query, final Class<T> clazz, final int offset, final List<String> routing, final String scrollTimeValidity) {

		final SearchResponse searchResponse = new ScrollSearchBuilder()
				.withQuery(query)
				.withClient(client)
				.withIndexName(getIndexName(clazz))
				.withType(getType(clazz))
				.withOffset(offset)
				.withRouting(routing)
				.withScrollTimeValidity(scrollTimeValidity)
				.build()
				.execute()
				.actionGet();

		final List<T> items = new ScrollSearchExtractor<>(client, TimeValue.timeValueHours(1), entityMapper, clazz).apply(searchResponse);

		return PartialList.<T>builder()
				.list(items)
				.totalSize(0L)
				.pageSize(-1L)
				.offset((long) offset)
				.build();

	}


	private <T extends Item> String getIndexName(Class<T> clazz) {
		return this.typeMapping.getIndexName(clazz);
	}

	private <T extends Item> String getType(Class<T> clazz) {
		return this.typeMapping.getType(clazz);
	}


	public boolean createIndex(String indexName) {
		return false;
	}

}

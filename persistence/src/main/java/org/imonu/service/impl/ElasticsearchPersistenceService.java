
package org.imonu.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vavr.Lazy;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.imonu.component.ResourceFileLoader;
import org.imonu.config.AppProperties;
import org.imonu.domain.Item;
import org.imonu.query.SearchQuery;
import org.imonu.service.EntityMapper;
import org.imonu.service.PersistenceService;
import org.imonu.service.TypeMapping;

@Slf4j
public class ElasticsearchPersistenceService implements PersistenceService {


	private final Client client;

	private final EntityMapper entityMapper;

	private final TypeMapping typeMapping;

	private final ResourceFileLoader resourceFileLoader;

	private final AppProperties appProperties;

	Lazy<Map<String, String>> mappingsLazy;


	public ElasticsearchPersistenceService(Client client, AppProperties appProperties, ResourceFileLoader resourceFileLoader,
			EntityMapper entityMapper, TypeMapping typeMapping) {
		Objects.requireNonNull(client, "client must not be null");
		this.client = client;
		this.entityMapper = entityMapper;
		this.typeMapping = typeMapping;
		this.resourceFileLoader = resourceFileLoader;
		this.appProperties = appProperties;
		this.mappingsLazy = Lazy.of(() -> resourceFileLoader.loadTypeMappings("classpath*:mappings/*.json"));
	}

	public <T extends Item> List<T> getAllItems(final Class<T> clazz) {
		return SearchQuery.of(client, entityMapper).index(getIndexName(clazz)).type(getType(clazz))
				.queryBuilder(QueryBuilders.matchAllQuery())
				.scroll(clazz)
				.fetchAll()
				.getList();
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
				.onFailure(ex -> log.error("Error getting item {}", itemId, ex))
				.toJavaOptional();
	}

	public <T> boolean remove(final String itemId, final Class<T> clazz) {
		client.prepareDelete(getIndexName(clazz), getType(clazz), itemId)
				.execute().actionGet();
		return true;
	}


	private <T> String getIndexName(Class<T> clazz) {
		return this.typeMapping.getIndexName(clazz);
	}

	private <T> String getType(Class<T> clazz) {
		return this.typeMapping.getType(clazz);
	}


	public boolean createIndex(String indexName) {
		if (!indexExist(indexName)) {
			final Map<String, String> mappings = mappingsLazy.get();
			final Map<String, String> filteredMappings = mappings.entrySet().stream()
					.filter(entry -> appProperties.getIndexNames().containsKey(entry.getKey()) &&
							appProperties.getIndexNames().get(entry.getKey()).equals(entry.getKey()))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			// Add default mapping
			filteredMappings.put("_default_", mappings.get("_default_"));
			final Settings indexSettings = resourceFileLoader.loadFile("classpath*:settings/index_settings.json")
					.map(json -> Settings.builder().loadFromSource(json, XContentType.JSON).build())
					.orElse(Settings.EMPTY);
			CreateIndexRequestBuilder builder = client.admin().indices().prepareCreate(indexName)
					.setSettings(indexSettings);
			filteredMappings.forEach((k, v) -> builder.addMapping(k, v, XContentType.JSON));
			return builder.execute().actionGet().isAcknowledged();
		}
		return false;
	}

	public boolean removeIndex(final String indexName) {
		if (indexExist(indexName)) {
			return client.admin().indices().prepareDelete(indexName).execute().actionGet().isAcknowledged();
		}
		return false;
	}

	private boolean indexExist(final String indexName) {
		return client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
	}

}

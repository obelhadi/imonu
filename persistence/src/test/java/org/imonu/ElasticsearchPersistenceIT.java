package org.imonu;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vavr.collection.HashMap;
import io.vavr.control.Try;
import org.imonu.common.TestItem;
import org.imonu.component.ResourceFileLoader;
import org.imonu.config.AppProperties;
import org.imonu.domain.Item;
import org.imonu.service.TypeMapping;
import org.imonu.service.impl.DefaultEntityMapper;
import org.imonu.service.impl.ElasticsearchPersistenceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchPersistenceIT extends AbstractESIntegrationTest {


	private static final String INDEX = "test_index";

	private static final String TEST_ITEM_TYPE = "testItem";

	@Mock
	private AppProperties appProperties;

	@Mock
	private ResourceFileLoader resourceFileLoader;

	@Mock
	private TypeMapping typeMapping;


	private ElasticsearchPersistenceService elasticsearchPersistenceService;

	@Before
	public void setUp() {
		elasticsearchPersistenceService = new ElasticsearchPersistenceService(client, appProperties, resourceFileLoader,
				new DefaultEntityMapper(), typeMapping);
		when(typeMapping.getIndexName(any())).thenReturn(INDEX);
		when(typeMapping.getType(ArgumentMatchers.<Class<TestItem>>any())).thenReturn(TEST_ITEM_TYPE);
		embeddedElastic.deleteIndex(INDEX);
	}


	@Test
	public void should_save_item() throws UnknownHostException {
		// Given
		final TestItem testItem = new TestItem("Id", "My Test Item");

		// When
		final Try<Item> saveTry = elasticsearchPersistenceService.save(testItem);
		refresh(INDEX);

		// Then
		assertTrue(saveTry.isSuccess());
		final Item savedItem = saveTry.get();
		assertThat(savedItem, is(equalTo(testItem)));
		final Map<String, String> allItems = getAll(INDEX, TEST_ITEM_TYPE);
		assertThat(allItems.size(), is(1));
		final String item = allItems.get(testItem.getItemId());
		assertThat(item, is(notNullValue()));
		final String itemId = readJsonPath(item, "$.itemId");
		final String name = readJsonPath(item, "$.name");
		assertThat(itemId, is(testItem.getItemId()));
		assertThat(name, is(testItem.getName()));
	}


	@Test
	public void should_load_item_by_id() throws JsonProcessingException, UnknownHostException {
		// Given
		final String itemId = "ID1";
		final TestItem testItem = new TestItem(itemId, "My Test Item");
		index(INDEX, TEST_ITEM_TYPE, itemId, objectMapper.writeValueAsString(testItem));

		// When
		final Optional<TestItem> optionalItem = elasticsearchPersistenceService.load(itemId, TestItem.class);

		// Then
		assertTrue(optionalItem.isPresent());
		final TestItem loadedItem = optionalItem.get();
		assertThat(loadedItem, is(equalTo(testItem)));
	}

	@Test
	public void should_get_all_items() throws JsonProcessingException, UnknownHostException {
		// Given
		final TestItem testItem1 = new TestItem("Id1", "My 1st Test Item");
		final TestItem testItem2 = new TestItem("Id2", "My 2nd Test Item");
		index(INDEX, TEST_ITEM_TYPE, testItem1.getItemId(), objectMapper.writeValueAsString(testItem1));
		index(INDEX, TEST_ITEM_TYPE, testItem2.getItemId(), objectMapper.writeValueAsString(testItem2));

		// When
		final List<TestItem> allItems = elasticsearchPersistenceService.getAllItems(TestItem.class);

		// Then
		assertThat(allItems, hasSize(2));
		assertThat(allItems, hasItems(testItem1, testItem2));
	}

	@Test
	public void should_remove_item_by_id() throws JsonProcessingException {
		// Given
		final String itemId = "ID1";
		final TestItem testItem = new TestItem(itemId, "My Test Item");
		index(INDEX, TEST_ITEM_TYPE, itemId, objectMapper.writeValueAsString(testItem));

		// When
		final boolean removed = elasticsearchPersistenceService.remove(itemId, TestItem.class);
		refresh(INDEX);

		// Then
		assertTrue(removed);
		final Map<String, String> allItems = getAll(INDEX, TEST_ITEM_TYPE);
		assertTrue(allItems.isEmpty());

	}

	@Test
	public void should_get_mappings_of_a_type() throws JsonProcessingException {
		// Given
		final String TYPE = "new_type";
		createIndex(INDEX);
		putMapping(INDEX, TYPE, "{\n" +
				"  \"properties\": {\n" +
				"    \"name\": {\n" +
				"      \"type\": \"text\"\n" +
				"    }\n" +
				"  }\n" +
				"}");

		putMapping(INDEX, TYPE, "{\n" +
				"  \"properties\": {\n" +
				"    \"date\": {\n" +
				"      \"type\": \"date\"\n" +
				"    }\n" +
				"  }\n" +
				"}");


		// When
		final Map<String, Map<String, Object>> propertiesMapping = elasticsearchPersistenceService.getPropertiesMapping(TYPE);
		// Then
		assertThat(propertiesMapping.size(), is(2));
		assertThat(propertiesMapping, hasEntry("name", HashMap.of("type", "text").toJavaMap()));
		assertThat(propertiesMapping, hasEntry("date", HashMap.of("type", "date").toJavaMap()));

	}


}


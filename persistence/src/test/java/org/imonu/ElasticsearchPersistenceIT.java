package org.imonu;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
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
		assertThat(optionalItem.isPresent(), is(true));
		final TestItem loadedItem = optionalItem.get();
		assertThat(loadedItem, is(equalTo(testItem)));
	}

}

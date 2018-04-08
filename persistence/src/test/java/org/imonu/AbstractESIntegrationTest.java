package org.imonu;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

@Ignore
@Slf4j
public abstract class AbstractESIntegrationTest {


	private static final String CLUSTER_NAME = "elasticsearch";

	private static final int PORT = 9300;

	static EmbeddedElastic embeddedElastic;

	ObjectMapper objectMapper = new ObjectMapper();
	Client client = getClient();


	void index(String index, String type, String id, String source) {
		this.client.prepareIndex()
				.setId(id)
				.setIndex(index)
				.setType(type)
				.setSource(source, XContentType.JSON)
				.execute()
				.actionGet();
		refresh(index);
	}

	void refresh(String index) {
		client.admin().indices().prepareFlush(index).execute().actionGet();
		client.admin().indices().prepareRefresh(index).execute().actionGet();
	}

	String readJsonPath(String item, String path) {
		return JsonPath.parse(item).read(path, String.class);
	}

	Map<String, String> getAll(String index, String type) {
		final SearchResponse searchResponse = client.prepareSearch(index)
				.setTypes(type)
				.setSize(100)
				.execute().actionGet();
		return Arrays.stream(searchResponse.getHits().getHits()).collect(Collectors.toMap(SearchHit::getId, SearchHit::getSourceAsString));
	}

	private Client getClient() {
		TransportClient client = null;
		try {
			client = new PreBuiltTransportClient(Settings.builder()
					.put("cluster.name", CLUSTER_NAME).build())
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), PORT));
		}
		catch (UnknownHostException e) {
			log.error("UnknownHostException ", e);
		}
		return client;
	}

	@BeforeClass
	public static void beforeAll() throws IOException, InterruptedException {
		embeddedElastic = EmbeddedElastic.builder()
				.withElasticVersion("5.6.8")
				.withSetting(PopularProperties.TRANSPORT_TCP_PORT, PORT)
				.withSetting(PopularProperties.CLUSTER_NAME, CLUSTER_NAME)
				.withStartTimeout(1, TimeUnit.MINUTES)
				.build()
				.start();
	}


	@AfterClass
	public static void afterAll() {
		if (embeddedElastic != null)
			embeddedElastic.stop();
	}

}

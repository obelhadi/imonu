package org.imonu.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.imonu.component.ResourceFileLoader;
import org.imonu.service.PersistenceService;
import org.imonu.service.impl.DefaultEntityMapper;
import org.imonu.service.impl.DefaultTypeMapping;
import org.imonu.service.impl.ElasticsearchPersistenceService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configuration
public class ElasticsearchConfig {


	@Bean
	public PersistenceService persistenceService(Client client, AppProperties appProperties, ResourceFileLoader resourceFileLoader) {
		return new ElasticsearchPersistenceService(client, appProperties, resourceFileLoader, new DefaultEntityMapper(), new DefaultTypeMapping(appProperties.getIndexName(), appProperties.getItemsMonthlyIndexed()
				, appProperties.getIndexNames()));
	}

	@Bean
	public ResourceFileLoader fileLoader(ResourcePatternResolver resourcePatternResolve) {
		return new ResourceFileLoader(resourcePatternResolve);
	}

	@Bean
	@DependsOn("embeddedElastic")
	public Client client(AppProperties appProperties) throws UnknownHostException {
		return new PreBuiltTransportClient(Settings.builder()
				.put("cluster.name", appProperties.getClusterName()).build())
				.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(appProperties.getHost()), appProperties.getPort()));
	}


}

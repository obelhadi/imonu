package org.imonu.config;

import org.elasticsearch.client.Client;
import org.imonu.service.PersistenceService;
import org.imonu.service.impl.ElasticsearchPersistenceService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

@Configuration
public class ServiceConfig {

	@Bean
	public PersistenceService persistenceService(Client client, ElasticsearchTemplate elasticsearchTemplate) {
		return new ElasticsearchPersistenceService(client, elasticsearchTemplate);
	}
}

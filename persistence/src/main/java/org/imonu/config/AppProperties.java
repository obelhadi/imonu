package org.imonu.config;

import java.util.List;
import java.util.Map;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "elasticsearch")
@Data
public class AppProperties {

	private String host;
	private Integer port;
	private String clusterName;
	private Integer numberOfShards;
	private Integer numberOfReplicas;
	private String indexName;
	List<String> itemsMonthlyIndexed;
	Map<String, String> indexNames;


}

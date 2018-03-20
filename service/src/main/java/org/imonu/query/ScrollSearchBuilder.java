package org.imonu.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Wither;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;


@Wither
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ScrollSearchBuilder {

	public static final int defaultQueryLimit = 10;

	@NonNull
	private Client client;

	@NonNull
	private QueryBuilder query;

	@NonNull
	private String indexName;

	private Integer offset;

	@NonNull
	private String type;


	private List<String> routing = new ArrayList<>();

	private String scrollTimeValidity;

	private Integer size;


	public SearchRequestBuilder build() {
		final Integer offset = Optional.ofNullable(this.offset).orElse(0);
		final Integer size = Optional.ofNullable(this.size).orElse(defaultQueryLimit);
		final TimeValue keepAlive = Optional.ofNullable(scrollTimeValidity)
				.map(s -> TimeValue.parseTimeValue(s, TimeValue.timeValueHours(1), "scrollTimeValidity"))
				.orElse(TimeValue.timeValueHours(1));

		final SearchRequestBuilder searchRequestBuilder = client.prepareSearch(this.indexName)
				.setTypes(this.type)
				.setFetchSource(true)
				.setScroll(keepAlive)
				.setFrom(offset)
				.setSize(size)
				.setQuery(this.query)
				.setVersion(true);

		if(this.routing != null && !this.routing.isEmpty()) {
			searchRequestBuilder.setRouting(this.routing.toArray(new String[this.routing.size()]));
		}
		return searchRequestBuilder;
	}


}

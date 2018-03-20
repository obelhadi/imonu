package org.imonu.query;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.imonu.domain.Item;
import org.imonu.service.EntityMapper;

public interface ItemsExtractor {

	default <T extends Item> List<T> extractItems(SearchHits hits, Class<T> clazz, EntityMapper entityMapper) {
		return Stream.of(hits.getHits())
				.map(SearchHit::getSourceAsString)
				.map(source -> entityMapper.mapToObject(source, clazz).toJavaOptional())
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());
	}
}

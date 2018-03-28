package org.imonu.query;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class SearchResult<T> {

	List<T> items;
	String scrollId;
	Long totalHits;

	public SearchResult(List<T> items) {
		this(items, null, null);
	}

}

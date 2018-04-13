package org.imonu.query;

import java.util.regex.Pattern;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import lombok.Getter;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;


public class SortByExtractor {

	private final String sortBy;

	@Getter
	private List<GeoDistanceSortBuilder> geoDistancesSort = List.empty();

	@Getter
	private List<Tuple2<String, SortOrder>> fieldsSort = List.empty();

	private SortByExtractor(String sortBy) {
		this.sortBy = sortBy;
	}

	public static SortByExtractor from(String sortByString) {
		return new SortByExtractor(sortByString);
	}

	private List<String> splitString(String str, String delimiter) {
		return Stream.ofAll(Pattern.compile(delimiter)
				.splitAsStream(str)
				.sequential()).toList();
	}

	public SortByExtractor build() {
		final List<List<String>> sortLines = splitString(sortBy, ",")
				.map(line -> splitString(line, ":"))
				.toList();
		sortLines
				.filter(line -> line.size() > 0)
				.forEach(line -> {
					SortOrder sortOrder = "desc".equals(line.last()) ? SortOrder.DESC : SortOrder.ASC;
					if ("geo".equals(line.head()) && line.size() > 3) {
						geoDistancesSort = geoDistancesSort.append(SortBuilders.geoDistanceSort(line.get(1), Double.parseDouble(line.get(2)),
								Double.parseDouble(line.get(3))).unit(DistanceUnit.KILOMETERS).order(sortOrder));
					}
					else {
						// TODO: get ES mapping for the field
						String name = line.head();
						if (name != null) {
							this.fieldsSort = this.fieldsSort.append(Tuple.of(name, sortOrder));
						}
					}
				});
		return this;
	}

}


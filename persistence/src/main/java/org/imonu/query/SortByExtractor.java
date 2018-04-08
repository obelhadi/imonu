package org.imonu.query;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Getter;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;


public class SortByExtractor {

	private final String sortBy;

	@Getter
	private List<GeoDistanceSortBuilder> geoDistanceBuilders = new ArrayList<>();

	@Getter
	private List<Tuple2<String, SortOrder>> otherSortOrder = new ArrayList<>();

	public SortByExtractor(String sortBy) {
		this.sortBy = sortBy;
		extractSortBuilders();
	}

	public static SortByExtractor of(String sortBy) {
		return  new SortByExtractor(sortBy);
	}

	private List<String> splitString(String str, String delimiter) {
		return Pattern.compile(delimiter)
				.splitAsStream(str)
				.sequential()
				.collect(Collectors.toList());
	}

	private void extractSortBuilders() {
		final List<List<String>> sortLines = splitString(sortBy, ",")
				.stream()
				.map(line -> splitString(line, ":"))
				.collect(Collectors.toList());

		sortLines.stream()
				.filter(line -> line.size() > 0)
				.forEach(line -> {
					if (line.get(0).equals("geo")) {
						SortOrder sortOrder = SortOrder.ASC;
						if (line.size() > 4 && line.get(4).equals("desc")) {
							sortOrder = SortOrder.DESC;
						}
						geoDistanceBuilders.add(SortBuilders.geoDistanceSort(line.get(1), Double.parseDouble(line.get(2)),
								Double.parseDouble(line.get(3))).unit(DistanceUnit.KILOMETERS).order(sortOrder));
					}
					else {
						// TODO: get ES mapping for the field
						final int size = line.size();
						String name = size > 1 ? line.get(size - 2) : line.get(size - 1);
						if (name != null) {
							if (line.get(size - 1).equals("desc")) {
								this.otherSortOrder.add(Tuple.of(name, SortOrder.DESC));
							}
							else {
								this.otherSortOrder.add(Tuple.of(name, SortOrder.ASC));
							}
						}

					}

				});

	}

}


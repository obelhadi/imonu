package org.imonu;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;

import java.util.Arrays;
import java.util.List;

import io.vavr.Tuple2;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.imonu.query.SortByExtractor;
import org.junit.Test;


public class SortByExtractorTest {


	@Test
	public void should_create_geo_distances_with_desc_sort_order() {
		// Given
		final String fieldName = "userLocation";
		final String lat = "2";
		final String lan = "3";
		String sortBy = "geo:" + fieldName + ":" + lat + ":" + lan + ":desc";

		// When
		final SortByExtractor sortByExtractor = new SortByExtractor(sortBy);

		// Then
		final List<GeoDistanceSortBuilder> geoDistanceBuilders = sortByExtractor.getGeoDistanceBuilders();
		assertThat(geoDistanceBuilders, hasSize(1));
		final GeoDistanceSortBuilder geoDistanceSortBuilder = geoDistanceBuilders.get(0);
		final String actualFieldName = geoDistanceSortBuilder.fieldName();
		assertThat(actualFieldName, is(fieldName));
		assertThat(geoDistanceSortBuilder.order(), is(SortOrder.DESC));
		final List<GeoPoint> points = Arrays.asList(geoDistanceSortBuilder.points());
		assertThat(points, hasSize(1));
		final GeoPoint geoPoint = points.get(0);
		assertThat(geoPoint.getLat(), is(2.0));
		assertThat(geoPoint.getLon(), is(3.0));

	}
	@Test
	public void should_create_geo_distances_with_asc_sort_order_when_not_specified() {
		// Given
		final String fieldName = "userLocation";
		final String lat = "40";
		final String lan = "50";
		String sortBy = "geo:" + fieldName + ":" + lat + ":" + lan;

		// When
		final SortByExtractor sortByExtractor = new SortByExtractor(sortBy);

		// Then
		final List<GeoDistanceSortBuilder> geoDistanceBuilders = sortByExtractor.getGeoDistanceBuilders();
		assertThat(geoDistanceBuilders, hasSize(1));
		final GeoDistanceSortBuilder geoDistanceSortBuilder = geoDistanceBuilders.get(0);
		final String actualFieldName = geoDistanceSortBuilder.fieldName();
		assertThat(actualFieldName, is(fieldName));
		assertThat(geoDistanceSortBuilder.order(), is(SortOrder.ASC));
		final List<GeoPoint> points = Arrays.asList(geoDistanceSortBuilder.points());
		assertThat(points, hasSize(1));
		final GeoPoint geoPoint = points.get(0);
		assertThat(geoPoint.getLat(), is(40.0));
		assertThat(geoPoint.getLon(), is(50.0));

	}

	@Test
	public void should_create_field_sort_with_desc_sort_order() {
		// Given
		final String fieldName = "userName";
		String sortBy = fieldName + ":" + "desc";

		// When
		final SortByExtractor sortByExtractor = new SortByExtractor(sortBy);

		// Then
		final List<Tuple2<String, SortOrder>> otherSortOrderList = sortByExtractor.getOtherSortOrder();
		assertThat(otherSortOrderList, hasSize(1));
		final Tuple2<String, SortOrder> otherSortOrder = otherSortOrderList.get(0);
		assertThat(otherSortOrder._1(), is(fieldName));
		assertThat(otherSortOrder._2(), is(SortOrder.DESC));

	}

	@Test
	public void should_create_field_ort_with_asc_sort_order() {
		// Given
		final String fieldName = "userName";
		String sortBy = fieldName + ":" + "asc";

		// When
		final SortByExtractor sortByExtractor = new SortByExtractor(sortBy);

		// Then
		final List<Tuple2<String, SortOrder>> otherSortOrderList = sortByExtractor.getOtherSortOrder();
		assertThat(otherSortOrderList, hasSize(1));
		final Tuple2<String, SortOrder> otherSortOrder = otherSortOrderList.get(0);
		assertThat(otherSortOrder._1(), is(fieldName));
		assertThat(otherSortOrder._2(), is(SortOrder.ASC));

	}



}

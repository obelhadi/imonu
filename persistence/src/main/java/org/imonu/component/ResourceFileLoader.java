package org.imonu.component;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Stream;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ResourceFileLoader {

	private final ResourcePatternResolver resourcePatternResolver;

	public ResourceFileLoader(ResourcePatternResolver resourcePatternResolver) {
		this.resourcePatternResolver = resourcePatternResolver;
	}

	public Map<String, String> loadTypeMappings(String dirPath) {
		final List<Resource> resources = Try.of(() -> resourcePatternResolver.getResources(dirPath))
				.filter(Objects::nonNull)
				.map(Arrays::asList)
				.onFailure(ex -> log.error("Error loading json files from classpath", ex))
				.getOrElse(new ArrayList<>());

		final Map<String, String> mappings = Stream.ofAll(resources)
				.flatMap(r -> Try.of(r::getFile))
				.map(f -> Tuple.of(f.getName(), Try.of(() -> FileUtils.readFileToString(f, UTF_8))
						.onFailure(ex -> log.error("Error converting file to String ", ex))
						.toJavaOptional())
				)
				.filter(tuple -> tuple._2.isPresent())
				.map(tuple -> Tuple.of(tuple._1.substring(0, tuple._1.lastIndexOf(".")), tuple._2.get()))
				.map(Tuple2::toEntry)
				.toMap(Map.Entry::getKey, Map.Entry::getValue)
				.toJavaMap();
		return mappings;
	}

	public Optional<String> loadFile(String path) {
		final Resource resource = resourcePatternResolver.getResource(path);
		return Try.of(() -> FileUtils.readFileToString(resource.getFile(), UTF_8))
				.onFailure(ex -> log.error("Error reading file {}", path, ex))
				.toJavaOptional();

	}
}

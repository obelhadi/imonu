package org.imonu.domain;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


@JsonDeserialize(builder = Metadata.MetadataBuilder.class)
@ToString
@EqualsAndHashCode(of = {"id", "scope"})
@Builder
@Getter
public class Metadata implements Comparable<Metadata> {

	private String id;

	private String name;

	private String description;

	private String scope;

	@Default
	private Set<String> tags = new LinkedHashSet<>();

	@Default
	private Set<String> systemTags = new LinkedHashSet<>();

	@Default
	private Boolean enabled = TRUE;

	@Default
	private Boolean missingPlugins = FALSE;

	@Default
	private Boolean hidden = FALSE;

	@Default
	private Boolean readOnly = FALSE;


	public int compareTo(Metadata o) {
		return getId().compareTo(o.getId());
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static class MetadataBuilder {
	}

}

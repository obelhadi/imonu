package org.imonu.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@JsonDeserialize(builder = Parameter.ParameterBuilder.class)
@ToString
@EqualsAndHashCode
@Builder
@Getter
public class Parameter {

	private String id;

	private String type;

	@Default
	private Boolean multivalued = Boolean.FALSE;

	private String defaultValue;

	@JsonPOJOBuilder(withPrefix = "")
	public static class ParameterBuilder {
	}

}
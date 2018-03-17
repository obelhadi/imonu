package org.imonu.condition;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.ToString;


@JsonDeserialize(builder = Condition.ConditionBuilder.class)
@ToString
@Builder
@Getter
public class Condition {

	@JsonIgnore
	ConditionType conditionType;

	@Default
	Map<String, Object> parameterValues = new HashMap<>();

	@JsonProperty("type")
	public String getConditionTypeId() {
		return Optional.ofNullable(getConditionType())
				.map(ConditionType::getItemId)
				.orElse(null);
	}


	public boolean containsParameter(String name) {
		return parameterValues.containsKey(name);
	}

	public Object getParameter(String name) {
		return parameterValues.get(name);
	}

	public void setParameter(String name, Object value) {
		parameterValues.put(name, value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Condition condition = (Condition) o;
		return Objects.equals(getConditionTypeId(), condition.getConditionTypeId()) &&
				Objects.equals(getParameterValues(), condition.getParameterValues());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getConditionTypeId(), getParameterValues());
	}


	@JsonPOJOBuilder(withPrefix = "")
	public static class ConditionBuilder {
	}

}

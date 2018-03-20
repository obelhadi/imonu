package org.imonu.domain.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.imonu.domain.Metadata;
import org.imonu.domain.MetadataItem;
import org.imonu.domain.Parameter;


@JsonDeserialize(builder = ConditionType.ConditionTypeBuilder.class)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
public class ConditionType extends MetadataItem {

	private String conditionEvaluator;

	private String queryBuilder;

	private Condition parentCondition;

	private List<Parameter> parameters;

	@Builder(toBuilder = true)
	public ConditionType(Metadata metadata, String conditionEvaluator, String queryBuilder, Condition parentCondition, List<Parameter> parameters) {
		super(metadata);
		this.conditionEvaluator = conditionEvaluator;
		this.queryBuilder = queryBuilder;
		this.parentCondition = parentCondition;
		this.parameters = Optional.ofNullable(parameters).orElse(new ArrayList<>());
	}

	@Override
	public String getItemType() {
		return "conditionType";
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static class ConditionTypeBuilder {
	}
}

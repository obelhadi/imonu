package org.imonu.domain;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.imonu.condition.Condition;

@JsonDeserialize(builder = Segment.SegmentBuilder.class)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
public class Segment extends MetadataItem {

	private Condition condition;

	@Builder(toBuilder = true)
	public Segment(Metadata metadata, Condition condition) {
		super(metadata);
		this.condition = condition;
	}

	@Override
	public String getItemType() {
		return "segment";
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static class SegmentBuilder {
	}
}

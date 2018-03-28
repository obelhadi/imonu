package org.imonu.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import org.imonu.domain.Item;
import org.imonu.domain.Profile;


@JsonDeserialize(builder = TestItem.TestItemBuilder.class)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
public class TestItem extends Item {
	String name;

	@Builder
	public TestItem(String itemId, String name) {
		super(itemId, null, null);
		this.name = name;
	}

	@Override
	public String getItemType() {
		return "testItem";
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static class TestItemBuilder {

	}
}
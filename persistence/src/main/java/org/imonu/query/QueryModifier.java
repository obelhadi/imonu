package org.imonu.query;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Getter
@Wither
@AllArgsConstructor
@NoArgsConstructor
public class QueryModifier {

	private String index;

	private String type;

	private Integer offset;

	private Integer size;

	private List<String> routing;


	public boolean isRoutingNotEmpty() {
		return this.getRouting() != null && !this.getRouting().isEmpty();
	}

	public String[] getRoutingAsArray() {
		return isRoutingNotEmpty() ?  getRouting().toArray(new String[getRouting().size()]) : new String[] {};
	}

}

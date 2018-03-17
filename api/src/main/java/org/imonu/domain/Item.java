package org.imonu.domain;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


@ToString
@EqualsAndHashCode(of = "itemId")
@AllArgsConstructor
@Getter
public abstract class Item {

	protected String itemId;

	protected String scope;

	protected Long version;


	public abstract String getItemType();

}

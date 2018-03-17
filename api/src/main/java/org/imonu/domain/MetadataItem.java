package org.imonu.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
public abstract class MetadataItem extends Item {

	protected Metadata metadata;


	public MetadataItem(Metadata metadata) {
		super(metadata.getId(), null, null);
		this.metadata = metadata;
	}

	public String getScope() {
		return metadata.getScope();
	}

}
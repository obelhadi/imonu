package org.imonu.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@Getter
public class PartialList<T> {

	@Default
	private List<T> list = new ArrayList<>();

	@Default
	private Long offset = 0L;

	@Default
	private Long pageSize = 0L;

	@Default
	private Long totalSize = 0L;

	private String scrollIdentifier;

	private String scrollTimeValidity;

}

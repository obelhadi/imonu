package org.imonu.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


@JsonDeserialize(builder = Profile.ProfileBuilder.class)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
public class Profile extends Item {


	private Map<String, Object> properties;

	private Map<String, Object> systemProperties;

	private Set<String> segments;

	private Map<String, Integer> scores;

	private String mergedWith;


	@Builder(toBuilder = true)
	public Profile(String itemId, String scope, Long version, Map<String, Object> properties, Map<String, Object> systemProperties, Set<String> segments, Map<String, Integer> scores, String mergedWith) {
		super(itemId, scope, version);
		this.properties = Optional.ofNullable(properties).orElse(new HashMap<>());
		this.systemProperties = Optional.ofNullable(systemProperties).orElse(new HashMap<>());
		this.segments = Optional.ofNullable(segments).orElse(new HashSet<>());
		this.scores = scores;
		this.mergedWith = mergedWith;
	}

	@Override
	public String getItemType() {
		return "profile";
	}


	@JsonIgnore
	public boolean isAnonymousProfile() {
		Boolean isAnonymous = (Boolean) getSystemProperties().get("isAnonymousProfile");
		return isAnonymous != null && isAnonymous;
	}


	@JsonPOJOBuilder(withPrefix = "")
	public static class ProfileBuilder {

	}
}

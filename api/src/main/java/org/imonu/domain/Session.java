package org.imonu.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


@JsonDeserialize(builder = Session.SessionBuilder.class)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
public class Session extends Item implements TimestampedItem {

	private String profileId;

	private Profile profile;

	private Map<String, Object> properties;

	private Map<String, Object> systemProperties;

	private Date timeStamp;

	private Date lastEventDate;

	private Integer size;

	private Integer duration;

	@Builder(toBuilder = true)
	public Session(String itemId, String scope, Long version, String profileId, Profile profile, Map<String, Object> properties, Map<String, Object> systemProperties, Date timeStamp, Date lastEventDate, Integer size, Integer duration) {
		super(itemId, scope, version);
		this.profileId = profileId;
		this.profile = profile;
		this.properties = Optional.ofNullable(properties).orElse(new HashMap<>());
		this.systemProperties = Optional.ofNullable(systemProperties).orElse(new HashMap<>());
		this.timeStamp = timeStamp;
		this.lastEventDate = lastEventDate;
		this.size = Optional.ofNullable(size).orElse(0);
		this.duration = Optional.ofNullable(duration).orElse(0);
	}

	@Override
	public String getItemType() {
		return "session";
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static class SessionBuilder {

	}
}

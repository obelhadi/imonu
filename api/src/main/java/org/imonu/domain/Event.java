package org.imonu.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@JsonDeserialize(builder = Event.EventBuilder.class)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
public class Event extends Item implements TimestampedItem {

	private String eventType;

	private Date timeStamp;

	private Map<String, Object> properties;

	@JsonIgnore
	private Profile profile;

	@JsonIgnore
	private Session session;

	private Item source;

	private Item target;

	@JsonIgnore
	private Boolean persistent;

	@JsonIgnore
	private Map<String, Object> attributes;

	@Builder(toBuilder = true)
	public Event(String itemId, String scope, Long version, String eventType, Date timeStamp, Map<String, Object> properties, Profile profile, Session session, Item source, Item target, Boolean persistent, Map<String, Object> attributes) {
		super(itemId, scope, version);
		this.eventType = eventType;
		this.timeStamp = timeStamp;
		this.properties = Optional.ofNullable(properties).orElse(new HashMap<>());
		this.profile = profile;
		this.session = session;
		this.source = source;
		this.target = target;
		this.persistent = Optional.ofNullable(persistent).orElse(Boolean.TRUE);
		this.attributes = Optional.ofNullable(attributes).orElse(new LinkedHashMap<>());
	}

	public String getProfileId() {
		return Optional.ofNullable(getProfile())
				.filter(p -> !p.isAnonymousProfile())
				.map(Profile::getItemId)
				.orElse(null);

	}

	public String getSessionId() {
		return Optional.ofNullable(getSession())
				.map(Session::getItemId)
				.orElse(null);
	}

	@Override
	public String getItemType() {
		return "event";
	}


	@JsonPOJOBuilder(withPrefix = "")
	public static class EventBuilder {
	}
}

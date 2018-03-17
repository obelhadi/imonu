package org.imonu.util;

import java.util.UUID;

public interface IdGenerator {
	default String generateId() {
		return UUID.randomUUID().toString();
	}
}

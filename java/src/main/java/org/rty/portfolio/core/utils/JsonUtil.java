package org.rty.portfolio.core.utils;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private JsonUtil() {
	}

	public static <T> List<T> toList(String jsonArray) throws Exception {
		final TypeReference<List<T>> typeReference = new TypeReference<List<T>>() {
		};

		return OBJECT_MAPPER.readValue(jsonArray, typeReference);
	}
}

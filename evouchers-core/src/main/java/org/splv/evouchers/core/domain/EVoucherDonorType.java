package org.splv.evouchers.core.domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EVoucherDonorType  {
	INDIVIDUAL(1), PROFESSIONAL(2), UNKNOWN(0);

	private static final Map<Integer, EVoucherDonorType> ENUM_MAP = new HashMap<>();
	static {
		Arrays.asList(EVoucherDonorType.values()).forEach(type -> ENUM_MAP.put(type.getValue(), type));
	}

	public static EVoucherDonorType fromValue(Integer value) {
		return ENUM_MAP.getOrDefault(value, UNKNOWN);
	}

	private final Integer value;
}

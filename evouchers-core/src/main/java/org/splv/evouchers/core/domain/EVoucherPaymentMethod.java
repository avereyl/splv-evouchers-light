package org.splv.evouchers.core.domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EVoucherPaymentMethod {

	CASH(0), CHECK(1), BANK_TRANSFER(2), CREDIT_CARD(3);

	private static final Map<Integer, EVoucherPaymentMethod> ENUM_MAP = new HashMap<>();
	static {
		Arrays.asList(EVoucherPaymentMethod.values()).forEach(method -> ENUM_MAP.put(method.getValue(), method));
	}

	public static EVoucherPaymentMethod fromValue(Integer value) {
		return ENUM_MAP.getOrDefault(value, CASH);
	}

	private final Integer value;
}

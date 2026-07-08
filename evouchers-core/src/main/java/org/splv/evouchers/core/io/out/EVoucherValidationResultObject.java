package org.splv.evouchers.core.io.out;

import java.io.Serializable;

import org.splv.evouchers.core.Constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
public class EVoucherValidationResultObject implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = Constants.SERIAL_VERSION;
	
	public enum EVoucherValidationResultValue {
		VALID,
		INVALID,
		UNSUPPORTED,
		ERROR
	}

	
	public static EVoucherValidationResultObject error() {
		return new EVoucherValidationResultObject(EVoucherValidationResultValue.ERROR);
	}
	
	public static EVoucherValidationResultObject invalid() {
		return new EVoucherValidationResultObject(EVoucherValidationResultValue.INVALID);
	}
	
	public static EVoucherValidationResultObject valid() {
		return new EVoucherValidationResultObject(EVoucherValidationResultValue.VALID);
	}
	public static EVoucherValidationResultObject unsupported() {
		return new EVoucherValidationResultObject(EVoucherValidationResultValue.UNSUPPORTED);
	}

	/**
	 * Result of the validation.
	 */
	private final EVoucherValidationResultValue value;


}

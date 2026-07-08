package org.splv.evouchers.core.service.tech.signing.exception;

import org.splv.evouchers.core.Constants;

import lombok.Getter;

@SuppressWarnings("java:S2166") // extending RuntimeException !!!
public class SigningException extends RuntimeException {

	public enum Reason {
		UNSUPPORTED,
		ERROR
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = Constants.SERIAL_VERSION;
	
	@Getter
	private final Reason reason;
	

	public SigningException(Reason reason) {
		this.reason = reason;
	}

	public SigningException(Reason reason, String message) {
		super(message);
		this.reason = reason;
	}

	public SigningException(Reason reason, String message, Exception e) {
		super(message, e);
		this.reason = reason;
	}


}

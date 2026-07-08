package org.splv.evouchers.core.service.tech.printing.exception;

import org.splv.evouchers.core.Constants;

public class QRCodeGenerationException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = Constants.SERIAL_VERSION;

	public QRCodeGenerationException() {
	}

	public QRCodeGenerationException(String message) {
		super(message);
	}

	public QRCodeGenerationException(Throwable cause) {
		super(cause);
	}

	public QRCodeGenerationException(String message, Throwable cause) {
		super(message, cause);
	}

}

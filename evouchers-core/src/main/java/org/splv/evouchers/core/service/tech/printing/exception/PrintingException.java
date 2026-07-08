package org.splv.evouchers.core.service.tech.printing.exception;

import org.splv.evouchers.core.Constants;

/**
 * @author AVEREYL
 *
 */
@SuppressWarnings("java:S2166") // extending RuntimeException !!!
public class PrintingException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = Constants.SERIAL_VERSION;

	/**
	 * 
	 */
	public PrintingException() {
	}

	/**
	 * @param message
	 */
	public PrintingException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public PrintingException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PrintingException(String message, Throwable cause) {
		super(message, cause);
	}

}

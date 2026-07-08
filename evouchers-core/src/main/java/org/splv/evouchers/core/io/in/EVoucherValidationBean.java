package org.splv.evouchers.core.io.in;

import java.io.Serializable;

import org.splv.evouchers.core.Constants;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EVoucherValidationBean implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = Constants.SERIAL_VERSION;
	
	private String signature;
}

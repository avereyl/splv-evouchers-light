package org.splv.evouchers.core.service.tech;

import java.security.SecureRandom;

import org.splv.evouchers.core.domain.EVoucher;

public interface EVoucherSigningService {

	static SecureRandom RANDOM = new SecureRandom();

	/**
	 * Digitally sign the eVoucher. (using default key pair)
	 * @param eVoucher eVoucher to sign
	 * @return Base64 encoded signature
	 */
	String signEVoucher(final EVoucher eVoucher);

	/**
	 * Verify the eVoucher signature.
	 * @param signature The signature to verify
	 * @param kid identifier of the key to use. May be null.
	 * @return True is signature is valid, false otherwise
	 */
	boolean verifyEVoucherSignature(final String signature);
	
	
	static String randomAlphanumeric(int length) {
	    int leftLimit = 48; // numeral '0'
	    int rightLimit = 122; // letter 'z'

	    return RANDOM.ints(leftLimit, rightLimit + 1)
	      .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
	      .limit(length)
	      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
	      .toString();
	}
}

package org.splv.evouchers.core.service.tech;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import org.splv.evouchers.core.domain.EVoucher;

public interface EVoucherPrintingService {

	/**
	 * Print into a PDF the EVoucher in parameter.
	 * @param eVoucher The eVoucher to print
	 * @param locale The Locale
	 * @return An output stream of the eVoucher printed as a PDF document
	 */
	ByteArrayOutputStream printEVoucher(final EVoucher eVoucher, final Locale locale);
	
}

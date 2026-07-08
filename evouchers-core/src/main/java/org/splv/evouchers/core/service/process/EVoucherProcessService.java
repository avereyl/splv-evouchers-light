package org.splv.evouchers.core.service.process;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.splv.evouchers.core.Constants;
import org.splv.evouchers.core.domain.EVoucher;
import org.splv.evouchers.core.io.converter.EVoucherConverter;
import org.splv.evouchers.core.io.in.EVoucherBean;
import org.splv.evouchers.core.io.in.EVoucherValidationBean;
import org.splv.evouchers.core.io.out.EVoucherPrintObject;
import org.splv.evouchers.core.io.out.EVoucherValidationResultObject;
import org.splv.evouchers.core.service.tech.EVoucherPrintingService;
import org.splv.evouchers.core.service.tech.EVoucherSigningService;
import org.splv.evouchers.core.service.tech.printing.exception.PrintingException;
import org.splv.evouchers.core.service.tech.printing.support.PrintingHelper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EVoucherProcessService {

	private final EVoucherPrintingService printingService;
	private final EVoucherSigningService signingService;

	private final EVoucherConverter converter = new EVoucherConverter();
	/**
	 * 
	 * @param eVoucherBean
	 * @return
	 */
	public EVoucherPrintObject printEVoucher(final EVoucherBean eVoucherBean) {
		EVoucher workingEVoucher = converter.convert(eVoucherBean);
		try (ByteArrayOutputStream baos = printingService.printEVoucher(workingEVoucher, Constants.DEFAULT_LOCALE);) {
			EVoucherPrintObject printObject = new EVoucherPrintObject();
			printObject.setId(workingEVoucher.getId());
			printObject.setReference(workingEVoucher.getReference());
			printObject.setData(baos.toByteArray());
			printObject.setFilename(PrintingHelper.computeVoucherFilename(workingEVoucher));
			return printObject;
		} catch (IOException e) {
			throw new PrintingException("Unable to print the eVoucher.", e);
		}
	}
	
	/**
	 * 
	 * @param validationBean
	 * @return
	 */
	public EVoucherValidationResultObject verifyEVoucherSignature(final EVoucherValidationBean validationBean) {
		try {
			boolean success = this.signingService.verifyEVoucherSignature(validationBean.getSignature());
			return success ? EVoucherValidationResultObject.valid() : EVoucherValidationResultObject.invalid();
		} catch (RuntimeException _) {
			return EVoucherValidationResultObject.error();
		}
	}
	
	
}

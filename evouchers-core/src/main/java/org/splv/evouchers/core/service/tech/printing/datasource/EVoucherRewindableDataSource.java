package org.splv.evouchers.core.service.tech.printing.datasource;

import java.util.Locale;

import org.splv.evouchers.core.Constants;
import org.splv.evouchers.core.domain.EVoucher;
import org.splv.evouchers.core.service.tech.printing.support.PrintingHelper;
import org.springframework.context.MessageSource;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EVoucherRewindableDataSource extends EmptyRewindableDataSource {

	protected final String voucherAmount;
	protected final String voucherDate;
	protected final String voucherDateLitteral;
	protected final String voucherReference;
	protected final String voucherCreationDate;
	
	public EVoucherRewindableDataSource(EVoucher eVoucher, Locale locale, MessageSource messageSource) {
		super();
		// formatted amount
		float amountValue = eVoucher.getAmount().floatValue();
		String amountNumeric = PrintingHelper.formatAmount(amountValue, locale);
		String amountLitteral = PrintingHelper.formatAmountLitteral(amountValue, locale, messageSource);
		this.voucherAmount = "%s (%s)".formatted(amountLitteral, amountNumeric);
		this.voucherDate = PrintingHelper.formatDate(eVoucher.getDonationDate(), Constants.DEFAULT_ZONEID, locale);
		this.voucherDateLitteral = PrintingHelper.formatDateLitteral(eVoucher.getDonationDate(), Constants.DEFAULT_ZONEID, locale);
		this.voucherReference = eVoucher.getReference();
		this.voucherCreationDate = PrintingHelper.formatDate(eVoucher.getCreatedDateOrNow(), Constants.DEFAULT_ZONEID, locale);
	}
	
	public EVoucherRewindableDataSource copy() {
		return new EVoucherRewindableDataSource(voucherAmount, voucherDate, voucherDateLitteral, voucherReference, voucherCreationDate);
	}

	@Override
	public Object getFieldValue(JRField jrField) throws JRException {
		Object result = null;
		switch (jrField.getName()) {
		case "voucherAmount":
			return voucherAmount;
		case "voucherDate":
			return voucherDate;
		case "voucherDateLitteral":
			return voucherDateLitteral;
		case "voucherReference":
			return voucherReference;
		case "voucherCreationDate":
			return voucherCreationDate;
		default:
			return result;
		}
	}
}

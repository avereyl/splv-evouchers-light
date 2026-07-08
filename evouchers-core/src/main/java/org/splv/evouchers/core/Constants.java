package org.splv.evouchers.core;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.Locale;

import org.springframework.util.MimeType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

	public static final long SERIAL_VERSION = 1L;

	public static final int DONOR_ADDRESS_LINES_MAX_NB = 3;
	public static final int DONOR_ADDRESS_LINES_MAX_LENGTH = 45;
	public static final int DONOR_NAME_LINES_MAX_NB = 2;
	public static final int DONOR_NAME_LINES_MAX_LENGTH = 45;

	public static final int DONOR_ADDRESS_ZIPCODE_MAX_LENGTH = 12;
	public static final int DONOR_ADDRESS_CITY_MAX_LENGTH = 64;

	public static final int PRINTED_NAME_LINE_MAXLENGTH = 45;

	public static final String BARCODE_DATA_FORMAT = "splv://chk/%s";
	public static final String DIGEST_ALGORITHM = "SHA-256";
	

	public static final String DATETIME_FORMAT_LOCAL = "yyyy-MM-dd'T'HH:mm:ss";
	
	public static final DateTimeFormatter SHORT_LOCAL_DATE;// yyyyMMdd
	static {
		SHORT_LOCAL_DATE = new DateTimeFormatterBuilder().appendValue(YEAR, 4, 4, SignStyle.EXCEEDS_PAD)
				.appendValue(MONTH_OF_YEAR, 2).appendValue(DAY_OF_MONTH, 2).toFormatter();
	}

	public static final MimeType DEFAULT_EVOUCHER_PRINT_MIME_TYPE = new MimeType("application", "pdf");

	public static final String DEFAULT_AUDITOR = "guillaume";
	public static final Locale DEFAULT_LOCALE = Locale.FRANCE;
	public static final String DEFAULT_ENCODING = "UTF-8";
	public static final ZoneId DEFAULT_ZONEID = ZoneId.of("Europe/Paris");

}
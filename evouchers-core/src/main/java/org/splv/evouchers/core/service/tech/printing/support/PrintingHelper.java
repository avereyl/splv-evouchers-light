package org.splv.evouchers.core.service.tech.printing.support;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.splv.evouchers.core.Constants;
import org.splv.evouchers.core.domain.EVoucher;
import org.springframework.context.MessageSource;

import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;
import com.ibm.icu.util.Currency;
import com.ibm.icu.util.ULocale;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PrintingHelper {

	/**
	 * 
	 * @param pt
	 * @return
	 */
	public static float pt2mm(float pt) {
		return pt * 25.4f / 72;
	}

	/**
	 * 
	 * @param mm
	 * @return
	 */
	public static float mm2pt(float mm) {
		return mm / 25.4f * 72;
	}

	public static String formatAmountLitteral(float amount, final Locale locale, final MessageSource messageSource) {
		ULocale ulocal = ULocale.forLocale(locale);
		Currency currency = Currency.getInstance(ulocal);
		NumberFormat formatter = new RuleBasedNumberFormat(ulocal, RuleBasedNumberFormat.SPELLOUT);
		double units = Math.floor(amount);
		double decimals = Math.round((amount - Math.floor(amount)) * Math.pow(10.0, currency.getDefaultFractionDigits()));
		int currencyFormat = units >= 2.0d ? Currency.PLURAL_LONG_NAME : Currency.LONG_NAME;

		StringJoiner sj = new StringJoiner(" ");
		sj.add(formatter.format(units));
		sj.add(currency.getName(locale, currencyFormat, "", null));
		if (decimals > 0) {
			String decimalJoiner = messageSource.getMessage("currency.and", null, "", locale);
			if(StringUtils.isNotBlank(decimalJoiner)){
				sj.add(decimalJoiner);
			}
			sj.add(formatter.format(decimals));
			String decimalMessageKey = buildMessageSourceDecimalKey(currency, decimals >= 2.0);
			String decimalText = messageSource.getMessage(decimalMessageKey, null, "", locale);
			if(StringUtils.isNotBlank(decimalText)){
				sj.add(decimalText);
			}
		}
		return sj.toString();
	}
	
	private static String buildMessageSourceDecimalKey(Currency currency, boolean plural) {
		StringBuilder sb = new StringBuilder();
		sb.append("currency.");
		sb.append(currency.getCurrencyCode().toLowerCase());
		sb.append(plural ? ".decimals" : ".decimal");
		return sb.toString();
	}

	public static String formatAmount(float amount, final Locale locale) {
		return formatAmount(amount, locale, 2);
	}
	public static String formatAmountNoCurrency(float amount, final Locale locale) {
		return formatAmountNoCurrency(amount, locale, 2);
	}
	
	public static String formatAmount(float amount, final Locale locale, final int nbOfFractionDigits) {
		ULocale ulocal = ULocale.forLocale(locale);
		StringBuilder sb = new StringBuilder();
		NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(ulocal);
		currencyFormatter.setMaximumFractionDigits(nbOfFractionDigits);
		currencyFormatter.setMinimumFractionDigits(nbOfFractionDigits);
		sb.append(currencyFormatter.format(amount));
		return sb.toString();
	}
	public static String formatAmountNoCurrency(float amount, final Locale locale, final int nbOfFractionDigits) {
		ULocale ulocal = ULocale.forLocale(locale);
		StringBuilder sb = new StringBuilder();
		NumberFormat formatter = NumberFormat.getInstance(ulocal);
		formatter.setMaximumFractionDigits(nbOfFractionDigits);
		formatter.setMinimumFractionDigits(nbOfFractionDigits);
		sb.append(formatter.format(amount));
		return sb.toString();
	}

	public static String formatDateLitteral(@Nullable ZonedDateTime datetime, final ZoneId zoneId, final Locale locale) {
		return datetime == null ? ""
				: datetime.withZoneSameInstant(zoneId)
						.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale));
	}

	public static String formatDate(final ZonedDateTime datetime, final ZoneId zoneId, final Locale locale) {
		return datetime == null ? ""
				: datetime.withZoneSameInstant(zoneId)
						.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale));
	}

	/**
	 * Limit the given name so that it fits the report layout.
	 * <ul>
	 * <li>2 lines maximum
	 * <li>Constants.NAME_PRINTED_LINE_MAXLENGTH characters per line
	 * </ul>
	 * 
	 * @param name
	 * @return
	 */
	public static String formatName4print(final String name) {
		return format4print(name, 2);
	}
	/**
	 * Limit the given address so that it fits the report layout.
	 * <ul>
	 * <li>3 lines maximum
	 * <li>Constants.NAME_PRINTED_LINE_MAXLENGTH characters per line
	 * </ul>
	 * 
	 * @param name
	 * @return
	 */
	public static String formatAddress4print(final String address) {
		return format4print(address, 3);
	}
	
	private static String format4print(final String lines, int maxLines) {
//		@formatter:off
		return Arrays.asList(lines.split("\n"))
				.stream()
				.limit(maxLines)
				.map(s -> StringUtils.abbreviate(s, Constants.PRINTED_NAME_LINE_MAXLENGTH-1))
				.collect(Collectors.joining("\n"));
//		@formatter:on
	}
	
	public static List<String> split4print(final String lines, int maxLines) {
//		@formatter:off
		return Optional.ofNullable(lines)
				.stream()
				.flatMap(l -> Arrays.stream(l.split("\n")))
				.limit(maxLines)
				.map(s -> StringUtils.abbreviate(s, Constants.PRINTED_NAME_LINE_MAXLENGTH-1))
				.toList();
//		@formatter:on
	}
	
	public static String flatten(final String lines, String separator) {
		return Stream.of(lines.split("\n")).collect(Collectors.joining(separator));
	}
	
	public static String flatten(final String lines) {
		return flatten(lines, " - ");
	}
	
	public static StringBuilder computeVoucherFilenameBase(final EVoucher eVoucher) {
		StringBuilder sb = new StringBuilder();
		sb.append(eVoucher.getDistributionYear());
		sb.append("-");
		sb.append(eVoucher.getHash());
		return sb;
	}
	
	public static String computeVoucherFilename(final EVoucher eVoucher) {
		StringBuilder sb = computeVoucherFilenameBase(eVoucher);
		sb.append(".pdf");
		return sb.toString();
	}
	
}

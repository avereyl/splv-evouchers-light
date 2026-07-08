/**
 * 
 */
package org.splv.evouchers.core.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.codec.digest.MurmurHash3;
import org.splv.evouchers.core.Constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 */
@Setter
@Getter
public final class EVoucher implements Serializable {

	private static final long serialVersionUID = Constants.SERIAL_VERSION;
	
	private UUID id;

	private EVoucherDonorType donorType;
	private String donorName;
	private String donorLastname;
	private String donorFirstname;

	private String donorAddress;
	private String donorZipcode;
	private String donorCity;
	private String donorCountryCode = Constants.DEFAULT_LOCALE.getCountry();

	private String donorEmail;

	private Integer distributionYear;

	private BigDecimal amount;
	private ZonedDateTime donationDate;
	private EVoucherPaymentMethod paymentMethod;
	
	private ZonedDateTime createdDate;
	
	@Setter(value = AccessLevel.PRIVATE)
	private transient String hash;

	public String getDisplayName() {
		return switch (donorType) {
		case INDIVIDUAL -> String.join(" ", this.getDonorFirstname(), this.getDonorLastname());
		case PROFESSIONAL -> this.getDonorName();
		case UNKNOWN -> throw new IllegalArgumentException("Cannot display name on unknown donor type");
		};
	}

	public String getHash() {
		if (this.hash == null) {
			this.hash = getHash(this);
		}
		return this.hash;
	} 
	
	public String getReference() {
		return String.join("-", this.distributionYear.toString(), this.getHash());
	}
	
	public Optional<ZonedDateTime> getCreatedDate() {
		return Optional.ofNullable(this.createdDate);
	}
	public ZonedDateTime getCreatedDateOrNow() {
		if (this.createdDate == null) {
			this.createdDate = ZonedDateTime.now(Constants.DEFAULT_ZONEID);
		}
		return this.createdDate;
	}
	
	public static String getHash(EVoucher eVoucher) {
		String textToHash = String.join("", 
				eVoucher.getDonorType().name(), 
				eVoucher.getDisplayName(), 
				eVoucher.getDonorAddress(),
				eVoucher.getDonorZipcode(),
				eVoucher.getDonorCity(),
				eVoucher.getDonorCountryCode(), 
				eVoucher.getDonorEmail(),
				eVoucher.getDistributionYear().toString(),//
				NumberFormat.getInstance(Constants.DEFAULT_LOCALE).format(eVoucher.getAmount()),
				DateTimeFormatter.ISO_ZONED_DATE_TIME.format(eVoucher.getDonationDate()),
				eVoucher.getPaymentMethod().name());
		return Integer.toHexString(MurmurHash3.hash32x86(textToHash.getBytes(StandardCharsets.UTF_8)));
	}



}

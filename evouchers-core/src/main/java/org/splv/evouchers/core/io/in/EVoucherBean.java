package org.splv.evouchers.core.io.in;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.splv.evouchers.core.Constants;
import org.splv.evouchers.core.domain.EVoucherDonorType;
import org.splv.evouchers.core.domain.EVoucherPaymentMethod;
import org.splv.evouchers.core.io.validation.DonorAddressConstraint;
import org.splv.evouchers.core.io.validation.DonorNameConstraint;
import org.splv.evouchers.core.io.validation.MultilineConstraint;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DonorNameConstraint
@DonorAddressConstraint
public class EVoucherBean implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = Constants.SERIAL_VERSION;
	

	@NotNull
	private EVoucherDonorType donorType;

	@MultilineConstraint(maxLines = Constants.DONOR_NAME_LINES_MAX_NB, maxLengthPerLine = Constants.DONOR_NAME_LINES_MAX_LENGTH)
	private String donorName;
	
	@Size(max = Constants.DONOR_NAME_LINES_MAX_LENGTH)
	private String donorLastname;
	
	@Size(max = Constants.DONOR_NAME_LINES_MAX_LENGTH)
	private String donorFirstname;
	
	private Long version = 0L;

	/**
	 * Address is checked at bean level @see {@link DonorAddressConstraint}
	 * Address may consist of several lines.
	 */
	@MultilineConstraint(maxLines = Constants.DONOR_ADDRESS_LINES_MAX_NB, maxLengthPerLine = Constants.DONOR_ADDRESS_LINES_MAX_LENGTH)
	private String donorAddress;
	
	@Size(max = Constants.DONOR_ADDRESS_ZIPCODE_MAX_LENGTH)
	private String donorZipcode;
	
	@Size(max = Constants.DONOR_ADDRESS_CITY_MAX_LENGTH)
	private String donorCity;
	
	@NotNull
	@Email
	private String donorEmail;
	@NotNull
	@Positive
	private BigDecimal amount;
	@NotNull
	private ZonedDateTime donationDate;
	@NotNull
	private EVoucherPaymentMethod paymentMethod;
	@NotNull
	@Positive
	private Integer distributionYear;


}

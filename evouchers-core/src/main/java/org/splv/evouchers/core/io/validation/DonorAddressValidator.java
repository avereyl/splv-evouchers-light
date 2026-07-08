package org.splv.evouchers.core.io.validation;

import org.apache.commons.lang3.StringUtils;
import org.splv.evouchers.core.domain.EVoucherDonorType;
import org.splv.evouchers.core.io.in.EVoucherBean;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DonorAddressValidator implements ConstraintValidator<DonorAddressConstraint, EVoucherBean> {

	@Override
	public boolean isValid(EVoucherBean value, ConstraintValidatorContext context) {
		return (value.getDonorType() == EVoucherDonorType.INDIVIDUAL)
				|| StringUtils.isNoneBlank(value.getDonorAddress(), value.getDonorZipcode(), value.getDonorCity());
	}

}

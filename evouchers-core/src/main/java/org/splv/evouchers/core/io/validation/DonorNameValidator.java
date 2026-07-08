package org.splv.evouchers.core.io.validation;

import org.splv.evouchers.core.domain.EVoucherDonorType;
import org.splv.evouchers.core.io.in.EVoucherBean;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DonorNameValidator implements ConstraintValidator<DonorNameConstraint, EVoucherBean> {

	@Override
	public boolean isValid(EVoucherBean value, ConstraintValidatorContext context) {
		return (value.getDonorType() == EVoucherDonorType.PROFESSIONAL && !isNullOrBlank(value.getDonorName()))
				|| (value.getDonorType() == EVoucherDonorType.INDIVIDUAL && !isNullOrBlank(value.getDonorFirstname())
						&& !isNullOrBlank(value.getDonorLastname()));
	}

	private boolean isNullOrBlank(final String s) {
		return s == null || s.isBlank();
	}

}

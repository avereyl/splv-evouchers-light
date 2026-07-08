package org.splv.evouchers.core.io.converter;

import java.time.ZonedDateTime;

import org.splv.evouchers.core.Constants;
import org.splv.evouchers.core.domain.EVoucher;
import org.splv.evouchers.core.io.in.EVoucherBean;
import org.springframework.core.convert.converter.Converter;

public class EVoucherConverter implements Converter<EVoucherBean, EVoucher> {

	@Override
	public EVoucher convert(EVoucherBean source) {
		return merge(source, new EVoucher());
	}

	public EVoucher merge(EVoucherBean source, EVoucher destination) {
		destination.setAmount(source.getAmount());
		destination.setDonationDate(source.getDonationDate());
		destination.setDonorAddress(source.getDonorAddress());
		destination.setDonorCity(source.getDonorCity());
		destination.setDonorEmail(source.getDonorEmail());
		destination.setDonorFirstname(source.getDonorFirstname());
		destination.setDonorLastname(source.getDonorLastname());
		destination.setDonorName(source.getDonorName());
		destination.setDonorType(source.getDonorType());
		destination.setDonorZipcode(source.getDonorZipcode());
		destination.setPaymentMethod(source.getPaymentMethod());
		destination.setDistributionYear(
				source.getDistributionYear() == null ? distributionYear(ZonedDateTime.now(Constants.DEFAULT_ZONEID))
						: source.getDistributionYear());
		return destination;
	}

	public Integer distributionYear(ZonedDateTime zdt) {
		if (zdt.isBefore(ZonedDateTime.of(zdt.getYear(), 6, 30, 0, 0, 0, 0, zdt.getZone()))) {
			return zdt.getYear();
		}
		return zdt.getYear() + 1;
	}

}

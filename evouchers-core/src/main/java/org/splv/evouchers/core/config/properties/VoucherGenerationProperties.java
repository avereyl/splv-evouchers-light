package org.splv.evouchers.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.voucher")
public class VoucherGenerationProperties {

	
	VoucherResponsibleProperties responsible;
	VoucherFacturXProperties facturX;
		
	@Getter
	@Setter
	public static class VoucherResponsibleProperties {
		String title = "";
		String name = "";
		String signaturePath = "";
	}

	@Getter
	@Setter
	public static class VoucherFacturXProperties {
		String name = "";
		String address = "";
		String zipCode = "";
		String city = "";
		String countryCode = "";
	}

}

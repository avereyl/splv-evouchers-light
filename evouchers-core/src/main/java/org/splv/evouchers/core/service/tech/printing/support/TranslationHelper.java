package org.splv.evouchers.core.service.tech.printing.support;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TranslationHelper {
	
	private final MessageSource messageSource;
	@Getter
	private final Locale locale;
	private final String prefix;

	public TranslationHelper(MessageSource messageSource, Locale locale) {
		super();
		this.messageSource = messageSource;
		this.locale = locale;
		this.prefix = "";
	}
	
	public String getMessage(String key) {
		return this.messageSource.getMessage(getCode(key), new Object[]{}, locale);
	}

	
	public String getMessage(String key, Object... args) {
		Object[] objects = args != null ? args : new Object[]{};
		return this.messageSource.getMessage(getCode(key), objects, locale);
	}

	private String getCode(String key) {
		return StringUtils.isEmpty(prefix) ? key : String.join(".", this.prefix, key);
	}

}

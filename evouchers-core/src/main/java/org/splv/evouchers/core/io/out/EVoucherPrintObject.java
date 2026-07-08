package org.splv.evouchers.core.io.out;

import java.util.UUID;

import org.splv.evouchers.core.Constants;
import org.springframework.util.MimeType;
import org.w3c.dom.DocumentType;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EVoucherPrintObject {

	public static final EVoucherPrintObject EMPTY = new EVoucherPrintObject();

	private UUID id;
	private String reference;
	private byte[] data;
	
	private DocumentType documentType;
	private String filename = "";
	private MimeType mimeType = Constants.DEFAULT_EVOUCHER_PRINT_MIME_TYPE;
}

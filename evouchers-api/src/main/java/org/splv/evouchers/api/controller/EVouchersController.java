package org.splv.evouchers.api.controller;


import org.splv.evouchers.core.io.in.EVoucherBean;
import org.splv.evouchers.core.io.in.EVoucherValidationBean;
import org.splv.evouchers.core.io.out.EVoucherPrintObject;
import org.splv.evouchers.core.io.out.EVoucherValidationResultObject;
import org.splv.evouchers.core.service.process.EVoucherProcessService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class EVouchersController {

	private final EVoucherProcessService eVoucherProcessService;
	
	/**
     * Print an eVoucher with data in the payload.
     * @param bean The eVoucher data to print 
     * @return The print as a PDF
     */
    @PutMapping(value = "/v1.0/evouchers/print")
    public ResponseEntity<ByteArrayResource> printEVoucher(@Valid @RequestBody EVoucherBean bean
    		) {
    	EVoucherPrintObject print = eVoucherProcessService.printEVoucher(bean);
    	
    	ByteArrayResource resource = new ByteArrayResource(print.getData());
    	HttpHeaders respHeaders = new HttpHeaders();
    	respHeaders.setContentType(MediaType.asMediaType(print.getMimeType()));
    	respHeaders.setContentLength(resource.contentLength());
    	respHeaders.set("Content-disposition", "inline; filename=" + print.getFilename());
    	return ResponseEntity.ok().headers(respHeaders).body(resource);
    }
    
    /**
     * Assess the validity of the signature
     * @param validationBean The signature to validate.
     * @return Result of validation.
     */
    @GetMapping(value = "/v1.0/evouchers/validation")
    public ResponseEntity<EVoucherValidationResultObject> getValidity(@Valid @RequestBody EVoucherValidationBean validationBean) {
    	EVoucherValidationResultObject validationResult = eVoucherProcessService.verifyEVoucherSignature(validationBean);
    	 HttpStatus status = switch (validationResult.getValue()) {
    		case VALID, INVALID -> HttpStatus.OK;
    		case UNSUPPORTED -> HttpStatus.BAD_REQUEST;
             default -> HttpStatus.INTERNAL_SERVER_ERROR;
    	};
    	return new ResponseEntity<>(validationResult, status);
    }
    
    
    
}

package org.splv.evouchers.core.service.tech.printing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.splv.evouchers.core.config.properties.VoucherGenerationProperties;
import org.splv.evouchers.core.domain.EVoucher;
import org.splv.evouchers.core.service.tech.EVoucherPrintingService;
import org.splv.evouchers.core.service.tech.EVoucherSigningService;
import org.splv.evouchers.core.service.tech.printing.datasource.EVoucherRewindableDataSource;
import org.splv.evouchers.core.service.tech.printing.exception.PrintingException;
import org.splv.evouchers.core.service.tech.printing.support.PrintingHelper;
import org.splv.evouchers.core.service.tech.printing.support.TranslationHelper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;
import net.sf.jasperreports.pdf.SimplePdfExporterConfiguration;
import net.sf.jasperreports.pdf.SimplePdfReportConfiguration;
import net.sf.jasperreports.pdf.type.PdfaConformanceEnum;

@Slf4j
@RequiredArgsConstructor
@Service("printingServiceJasper")
public class EVoucherPrintingServiceJasper implements EVoucherPrintingService, InitializingBean {


	private static final String ICM_PATH = "assets/icm/sRGB_ColorSpaceProfile.icm";

	private static final String VOUCHER_REPORT_PATH = "classpath:templates/jasper/eVoucherA4new.jasper";
	private static final String SUBREPORTS_BASE_PATH = "classpath:templates/jasper/subreports/";

	private static final String VOUCHER_ASSOCIATION_LOGO_SVG_PATH = "classpath:assets/img/logo-splv-text.svg";

	// Report datasources
	private static final String EVOUCHER_REFERENCE_DATASOURCE = "EVOUCHER_REFERENCE_DATASOURCE";
	private static final String EVOUCHER_AMOUNT_DATASOURCE = "EVOUCHER_AMOUNT_DATASOURCE";
	private static final String EVOUCHER_RECIPIENT_DS = "EVOUCHER_RECIPIENT_DATASOURCE";
	private static final String EVOUCHER_DONOR_DS = "EVOUCHER_DONOR_DATASOURCE";

	private static final String EVOUCHER_RESPONSIBLE_NAME = "EVOUCHER_RESPONSIBLE_NAME";
	private static final String EVOUCHER_RESPONSIBLE_TITLE = "EVOUCHER_RESPONSIBLE_TITLE";

	private static final String EVOUCHER_QRCODE_SVG_DATA = "EVOUCHER_QRCODE_SVG_DATA";
	private static final String EVOUCHER_ASSOCIATION_LOGO_SVG_DATA = "EVOUCHER_ASSOCIATION_LOGO_SVG_DATA";
	private static final String EVOUCHER_RESPONSIBLE_SIGNATURE_SVG_DATA = "EVOUCHER_RESPONSIBLE_SIGNATURE_SVG_DATA";
	

	@RequiredArgsConstructor
	private enum SubReport {

		EVOUCHER_AMOUNT_SUBREPORT("eVoucherA4Amount.jasper"), 
		EVOUCHER_FOOTER_SUBREPORT("eVoucherA4Footer.jasper"),
		EVOUCHER_HEADER_SUBREPORT("eVoucherA4Header.jasper"),
		EVOUCHER_REFERENCE_SUBREPORT("eVoucherA4Reference.jasper"),
		EVOUCHER_SIGNATURE_SUBREPORT("eVoucherA4Signature.jasper"),
		EVOUCHER_TRADING_PARTIES_SUBREPORT("eVoucherA4TradingParties.jasper"),
		EVOUCHER_TRADING_PARTY_SUBREPORT("eVoucherA4TradingParty.jasper"),
		EVOUCHER_TRADING_PARTY_LINE_SUBREPORT("eVoucherA4TradingPartyLine.jasper");

		final String filename;
	}

	private static final Map<String, JasperReport> SUBREPORTS_MAP = new HashMap<>();

	private final VoucherGenerationProperties voucherProperties;
	private final MessageSource messageSource;
	private final ResourceLoader resourceLoader;
	private final QRCodeGenerator qrcodeGenerator;
	private final EVoucherSigningService signingService;

	private JasperReport eVoucherReport;
	private String associationLogoSvgData;
	private String responsibleSignatureSvgData;

	@Override
	public void afterPropertiesSet() throws Exception {
		// loading report
		try (InputStream eVoucherReportStream = resourceLoader.getResource(VOUCHER_REPORT_PATH).getInputStream();) {
			this.eVoucherReport = (JasperReport) JRLoader.loadObject(eVoucherReportStream);
			// loading vector images files once and for all
			this.associationLogoSvgData = extractSVG(VOUCHER_ASSOCIATION_LOGO_SVG_PATH);
			this.responsibleSignatureSvgData = extractSVG(voucherProperties.getResponsible().getSignaturePath());
			// loading subreports
			for (SubReport sr : SubReport.values()) {
				SUBREPORTS_MAP.put(sr.name(), loadReport(sr.filename));
			}
		} catch (IOException | JRException e) {
			throw new IllegalArgumentException("Unable to load the report or assets used by the report", e);
		}
	}
	

	/**
	 * Print an eVoucher to a PDF document
	 * 
	 * @param eVoucher The eVoucher to print
	 * @param locale The locale of the printed document
	 * @return the PDF doc as a {@link ByteArrayOutputStream}
	 */
	@Override
	public ByteArrayOutputStream printEVoucher(EVoucher eVoucher, final Locale locale) {
		// print eVoucher
		JasperPrint eVoucherPrint = printEVoucherInternal(eVoucher, locale);
		// export as PDF
		return exportEVoucherToPDF(eVoucherPrint, eVoucher, locale, messageSource);
	}

	private JasperPrint printEVoucherInternal(EVoucher eVoucher, final Locale locale) {
		TranslationHelper th = new TranslationHelper(messageSource, locale, "voucher");
		// formatted amount
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(JRParameter.REPORT_LOCALE, locale);
		parameters.put(JRParameter.REPORT_RESOURCE_BUNDLE, new MessageSourceResourceBundle(messageSource, locale));

		// adding all sub reports
		SUBREPORTS_MAP.forEach(parameters::put);

		// adding datasource
		EVoucherRewindableDataSource eVoucherDS = new EVoucherRewindableDataSource(eVoucher, locale, messageSource);

		List<String> donorLines = new ArrayList<>();
		donorLines.addAll(PrintingHelper.split4print(eVoucher.getDisplayName(), 2));
		donorLines.addAll(PrintingHelper.split4print(eVoucher.getDonorAddress(), 2));
		donorLines.addAll(PrintingHelper.split4print(eVoucher.getDonorZipcode() + " " + eVoucher.getDonorCity(), 1));

		List<String> recipientLines = new ArrayList<>();
		recipientLines.add(th.getMessage("splv"));
		recipientLines.addAll(PrintingHelper.split4print(th.getMessage("splv.address"), 2));

		parameters.put(EVOUCHER_REFERENCE_DATASOURCE, eVoucherDS);
		parameters.put(EVOUCHER_AMOUNT_DATASOURCE, eVoucherDS.copy());

		// see fieldDescription with '_THIS' keyword
		parameters.put(EVOUCHER_RECIPIENT_DS, new JRBeanCollectionDataSource(recipientLines));
		parameters.put(EVOUCHER_DONOR_DS, new JRBeanCollectionDataSource(donorLines));

		// adding extra parameters
		parameters.put(EVOUCHER_RESPONSIBLE_TITLE, voucherProperties.getResponsible().getTitle());
		parameters.put(EVOUCHER_RESPONSIBLE_NAME, voucherProperties.getResponsible().getName());
		parameters.put(EVOUCHER_QRCODE_SVG_DATA, qrcodeGenerator.generateQRCodeAsSVG(signingService.signEVoucher(eVoucher)));
		parameters.put(EVOUCHER_ASSOCIATION_LOGO_SVG_DATA, this.associationLogoSvgData);
		parameters.put(EVOUCHER_RESPONSIBLE_SIGNATURE_SVG_DATA, this.responsibleSignatureSvgData);

		try {
			JasperPrint jasperPrint = JasperFillManager.fillReport(eVoucherReport, parameters, new JREmptyDataSource());
			if (jasperPrint.getPages().size() > 1) {
				log.error("Too many pages generated!");
				throw new PrintingException("Only 1 page is expected.");
			}
			return jasperPrint;
		} catch (JRException e) {
			throw new PrintingException("Unable to print eVoucher with jasper.", e);
		}
	}

	private ByteArrayOutputStream exportEVoucherToPDF(JasperPrint eVoucherPrint, EVoucher eVoucher, Locale locale,
			MessageSource messageSource) {
		TranslationHelper th = new TranslationHelper(messageSource, locale, "voucher.metadata");

		// export config - PDF metadata and PDF/A conformance
		SimplePdfExporterConfiguration exportConfig = new SimplePdfExporterConfiguration();
		exportConfig.setMetadataTitle(th.getMessage("title", String.valueOf(eVoucher.getDistributionYear())));// Do not set it here
		exportConfig.setMetadataSubject(th.getMessage("subject", eVoucher.getReference()));
		exportConfig.setMetadataKeywords(signingService.signEVoucher(eVoucher));
		exportConfig.setDisplayMetadataTitle(Boolean.TRUE);
		exportConfig.setMetadataAuthor(th.getMessage("author"));
		exportConfig.setMetadataCreator(th.getMessage("creator"));
		exportConfig.setTagLanguage(locale.getLanguage());

		exportConfig.setPdfaConformance(PdfaConformanceEnum.PDFA_1A);
		exportConfig.setIccProfilePath(ICM_PATH);

		// report config
		SimplePdfReportConfiguration reportConfig = new SimplePdfReportConfiguration();
		reportConfig.setSizePageToContent(true);
		reportConfig.setForceLineBreakPolicy(false);

		JRPdfExporter exporter = new JRPdfExporter();
		
		exporter.setConfiguration(reportConfig);
		exporter.setConfiguration(exportConfig);

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			exporter.setExporterInput(new SimpleExporterInput(eVoucherPrint));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
			exporter.exportReport();
			
			return baos;
		} catch (IOException | JRException e) {
			throw new PrintingException("Unable to export the eVoucher print to a PDF", e);
		}
	}

	private JasperReport loadReport(String reportName) throws JRException, IOException {
		String path = SUBREPORTS_BASE_PATH.concat(reportName);
		try (InputStream stream = resourceLoader.getResource(path).getInputStream();) {
			return (JasperReport) JRLoader.loadObject(stream);
		}
	}

	private String extractSVG(String path) throws IOException {
		return IOUtils.toString(resourceLoader.getResource(path).getInputStream(), StandardCharsets.UTF_8.name());
	}

}

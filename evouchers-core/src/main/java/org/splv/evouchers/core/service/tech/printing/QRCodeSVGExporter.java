package org.splv.evouchers.core.service.tech.printing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Optional;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.splv.evouchers.core.service.tech.printing.exception.QRCodeGenerationException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.zxing.common.BitMatrix;

@Component
public class QRCodeSVGExporter {

	/**
	 * Size are relative for a SVG path. Nevertheless we want to handle a fix Quiet
	 * Zone in millimeters. To be more accurate with this quiet zone, we increase
	 * relative value so that 1 point of relative value is a small fraction of a
	 * millimeter.
	 * 
	 * <pre>
	 * | Version | modules |        mm/module     |
	 * |         |         |  scale 1  | scale 9  |
	 * |   V10   |    057  |    0.807  |   0.089  |
	 * |   V17   |    085  |    0.541  |   0.060  |
	 * |   V25   |    117  |    0.393  |   0.043  |
	 * </pre>
	 */
	private static final int DEFAULT_SCALE = 9;

	private static final String STYLE_WHITE = "fill:white;";
	private static final String STYLE_BLACK = "fill:black;";

	private static final String SVG_ATTR_STYLE = "style";
	private static final String SVG_ATTR_WIDTH = "width";
	private static final String SVG_ATTR_VIEWBOX = "viewBox";
	private static final String SVG_ATTR_HEIGHT = "height";
	private static final String SVG_ELEMENT_SVG = "svg";
	private static final String SVG_ELEMENT_PATH = "path";
	private static final String SVG_ELEMENT_TITLE = "title";

	/**
	 * Final expected size in mm for a QR code. (without "quiet zone")
	 */
	private static final double QRCODE_SIZE_MM = 30.0;
	/**
	 * Minimal size in mm for the quiet zone. (one side)
	 */
	private static final double QRCODE_QZ_MIN_SIZE_MM = 2.5;


	public Document export(BitMatrix matrix, Optional<Double> quietZone) {
		int matrixSize = matrix.getWidth(); // square one
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		Document svg = impl.createDocument(svgNS, SVG_ELEMENT_SVG, null);
		Element svgRoot = svg.getDocumentElement();

		// in mm
		double qzWidthmm = quietZone.orElse(QRCODE_QZ_MIN_SIZE_MM);
		// offset in matrix point not scaled
		int matrixQZoffset = (int) Math.floor(matrixSize / QRCODE_SIZE_MM * qzWidthmm);

		double finalSizemm = QRCODE_SIZE_MM + 2 * qzWidthmm;
		String finalSizeAsString = String.format(Locale.US, "%.2fmm", finalSizemm);

		String size = Integer.toString((matrixSize + 2 * matrixQZoffset) * DEFAULT_SCALE);
		svgRoot.setAttributeNS(null, SVG_ATTR_WIDTH, finalSizeAsString);
		svgRoot.setAttributeNS(null, SVG_ATTR_HEIGHT, finalSizeAsString);
		svgRoot.setAttributeNS(null, SVG_ATTR_VIEWBOX, "0 0 %s %s".formatted(size, size));

		// title
		final Element title = svg.createElementNS(svgNS, SVG_ELEMENT_TITLE);
		title.setNodeValue("SPLV eVoucher QR code");
		svg.getDocumentElement().appendChild(title);

		// add white background rectangle of the matrix size
		final Element backgroundPath = svg.createElementNS(svgNS, SVG_ELEMENT_PATH);
		backgroundPath.setAttribute("d", buildBackgroundPathD((matrixSize + 2 * matrixQZoffset) * DEFAULT_SCALE));
		backgroundPath.setAttribute(SVG_ATTR_STYLE, STYLE_WHITE);
		svg.getDocumentElement().appendChild(backgroundPath);

		final Element qrCodePath = svg.createElementNS(svgNS, SVG_ELEMENT_PATH);
		// copy the matrix as it will be destroyed building the path
		// do not reuse this working matrix
		BitMatrix workingMatrix = new BitMatrix(matrixSize);
		for (int y = 0; y < matrixSize; y++) {
			workingMatrix.setRow(y, matrix.getRow(y, null));
		}
		qrCodePath.setAttribute("d", buildQRCodePathD(workingMatrix, matrixQZoffset));
		qrCodePath.setAttribute(SVG_ATTR_STYLE, STYLE_BLACK);
		svg.getDocumentElement().appendChild(qrCodePath);

		return svg;
	}

	public void writeToStream(BitMatrix matrix, OutputStream os, Optional<Double> quietZone) throws IOException {
		Document document = this.export(matrix, quietZone);
		StreamResult result = new StreamResult(os);
		Transformer transformer = createTransformer();
		try {
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			transformer.transform(new DOMSource(document), result);
		} catch (TransformerException e) {
			throw new IOException("Transformer Exception", e);
		}

	}

	@SuppressWarnings("java:S2755") // Documents handled by this transformer are internally generated therefore we
									// keep control on external DTD, schemas and style sheets to prevent XXE attacks
	private Transformer createTransformer() throws IOException {
		// An implementation of the TransformerFactory class is NOT guaranteed to be thread safe hence a new one is created for each call.
		final TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			return transformer;
		} catch (TransformerConfigurationException e) {
			throw new IOException("Transformer Config Exception", e);
		}
	}
	
	public String svgDocumentToString(Document document)  {
		try {
			Transformer transformer = createTransformer();
			DOMSource domSource = new DOMSource(document);
			StringWriter sw = new StringWriter();
			StreamResult sr = new StreamResult(sw);
			transformer.transform(domSource, sr);
			return sw.toString();
		} catch (TransformerException | IOException e) {
			throw new QRCodeGenerationException("Unable to transform the org.w3c.dom.Document in java.lang.String.", e);
		}
	}

	public ByteArrayResource exportAsResource(BitMatrix matrix, Optional<Double> quietZone) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
			writeToStream(matrix, baos, quietZone);
			return new ByteArrayResource(baos.toByteArray());
		}
	}


	private String buildBackgroundPathD(int size) {
		return "M0 0 h%d v%d h%d z".formatted(size, size, -size);
	}

	private String buildQRCodePathD(BitMatrix matrix, int qzOffset) {
		int matrixSize = matrix.getWidth(); // square one
		StringBuilder pathD = new StringBuilder();

		// loop on the matrix !
		// to reduce svg number of elements, we add rectangle of the biggest area
		// possible by coalescing contiguous values.
		for (int y = 0; y < matrixSize; y++) {
			for (int x = 0; x < matrixSize; x++) {
				boolean value = matrix.get(x, y);
				if (value) {// start of a rectangle
					// find biggest area
					int[] areaEnd = findBiggestArea(matrix, x, y);
					int dX = areaEnd[0];
					int dY = areaEnd[1];
					// add rectangle to svg path and clear matrix corresponding area
					pathD.append(buildRectangle(x + qzOffset, y + qzOffset, dX, dY, DEFAULT_SCALE));
					clearArea(matrix, x, y, dX, dY);
				}
			}
		}

		return pathD.toString();
	}

	private String buildRectangle(int x, int y, int dX, int dY, int scale) {
		return " M%d %d h%d v%d h%d z".formatted(x * scale, y * scale, dX * scale, dY * scale, -dX * scale);
	}

	private int[] findBiggestArea(BitMatrix matrix, int x, int y) {
		int matrixSize = matrix.getWidth(); // square one
		int[] dXY = new int[2];
		int bestDx = 1;
		int bestDy = 1;
		int xLimit = matrixSize;
		int yLimit = matrixSize;
		int maxArea = 1;

		int iy = y;
		while (iy < yLimit && matrix.get(x, iy)) {
			int dx = Math.min(matrix.getRow(iy, null).getNextUnset(x) - x, xLimit);
			xLimit = dx;
			int area = dx * (iy - y + 1);
			if (area > maxArea) {
				maxArea = area;
				bestDx = dx;
				bestDy = iy - y + 1;
			}
			iy++;
		}

		dXY[0] = bestDx;
		dXY[1] = bestDy;
		return dXY;
	}

	private void clearArea(BitMatrix matrix, int x, int y, int dX, int dY) {
		for (int iY = y; iY < y + dY; iY++) {
			for (int iX = x; iX < x + dX; iX++) {
				matrix.unset(iX, iY);
			}
		}
	}
}

package org.splv.evouchers.core.service.tech.signing;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.Date;

import org.splv.evouchers.core.Constants;
import org.splv.evouchers.core.domain.EVoucher;
import org.splv.evouchers.core.service.tech.EVoucherSigningService;
import org.splv.evouchers.core.service.tech.printing.support.PrintingHelper;
import org.splv.evouchers.core.service.tech.signing.exception.SigningException;
import org.splv.evouchers.core.service.tech.signing.exception.SigningException.Reason;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class EVoucherSigningServiceImpl implements EVoucherSigningService, InitializingBean {

	@Value("${app.signing.keystore.default-kid}")
	private String defaultKid;

	@Value("${app.signing.keystore.path}")
	private String keystorePath;

	@Value("${app.signing.keystore.password}")
	private String keystorePassword;
	
	private final ResourceLoader resourceLoader;
	
	private KeyStore keystore = null;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		try (InputStream readStream = resourceLoader.getResource(keystorePath).getInputStream()) {
			keystore = KeyStore.getInstance("PKCS12");
			keystore.load(readStream, keystorePassword.toCharArray());
		} catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
			throw new SigningException(Reason.ERROR, "Unable to load keyStore.", e);
		}
	}

	@Override
	public final String signEVoucher(EVoucher eVoucher) {
		
		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(defaultKid).build();
		
// @formatter:off
 		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.issuer("SPLV")
				.issueTime(Date.from(ZonedDateTime.now(Constants.DEFAULT_ZONEID).toInstant()))
				.subject(eVoucher.getDisplayName())
				.claim("ref", eVoucher.getReference())
				.claim("add", eVoucher.getDonorAddress())
				.claim("zip", eVoucher.getDonorZipcode())
				.claim("dct", eVoucher.getDonorCity())
				.claim("cco", eVoucher.getDonorCountryCode())
				.claim("amt", PrintingHelper.formatAmount(eVoucher.getAmount().floatValue(), Constants.DEFAULT_LOCALE))
				.claim("pay", eVoucher.getPaymentMethod().name())
				.claim("dat", eVoucher.getDonationDate().format(Constants.SHORT_LOCAL_DATE))
				.build();
// @formatter:on
		
		JWSObject jwsObject = new JWSObject(header,	claimsSet.toPayload());
		try {
			JWK signingKey =  RSAKey.load(keystore, defaultKid, keystorePassword.toCharArray());
			JWSSigner signer = new DefaultJWSSignerFactory().createJWSSigner(signingKey, JWSAlgorithm.RS256);
			jwsObject.sign(signer);
			return jwsObject.serialize();
		} catch (JOSEException | KeyStoreException _) {
			throw new SigningException(Reason.ERROR, "Unable to sign the eVoucher");
		}
		
	}
	
	@Override
	public boolean verifyEVoucherSignature(String message) {
		
		JWSObject jwsObject;
		try {
			jwsObject = JWSObject.parse(message);
			String keyId = jwsObject.getHeader().getKeyID();
			JWSVerifier verifier = new RSASSAVerifier(RSAKey.load(keystore, keyId, keystorePassword.toCharArray()));
			
			return jwsObject.verify(verifier);
		} catch (ParseException _) {	
			throw new SigningException(Reason.UNSUPPORTED,"eVoucher does not support signature checking");
		} catch (KeyStoreException | JOSEException _) {
			throw new SigningException(Reason.ERROR, "Unable to verify the eVoucher signature");
		}
		
	}

}

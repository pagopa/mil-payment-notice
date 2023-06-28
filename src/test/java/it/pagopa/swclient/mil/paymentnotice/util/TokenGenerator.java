/**
 * 
 */
package it.pagopa.swclient.mil.paymentnotice.util;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtSignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

public class TokenGenerator {

	static final Logger logger = LoggerFactory.getLogger(TokenGenerator.class);

	private PrivateKey privateKey;

	private String keyId;

	public TokenGenerator(String keyId, PrivateKey privateKey) {
		this.keyId = keyId;
		this.privateKey = privateKey;
	}

	public String getToken(Role role) {
		return getToken(role, UUID.randomUUID().toString());
	}

	public String getToken(Role role, String clientId) {
		
		String token = null;
		try {
			token = Jwt
					.groups(new HashSet<>(Collections.singletonList(role.label)))
					.subject(clientId)
					.expiresIn(3600)
					.jws()
					.keyId(keyId)
					.sign(privateKey);
		} catch (JwtSignatureException e) {
			logger.error("Error while generating jwt token", e);
		}

		return token;
	}

}

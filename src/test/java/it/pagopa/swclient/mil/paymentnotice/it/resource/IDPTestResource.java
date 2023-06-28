package it.pagopa.swclient.mil.paymentnotice.it.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import it.pagopa.swclient.mil.paymentnotice.util.Role;
import it.pagopa.swclient.mil.paymentnotice.util.AccessToken;
import it.pagopa.swclient.mil.paymentnotice.util.TokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class IDPTestResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    private static final Logger logger = LoggerFactory.getLogger(IDPTestResource.class);

    TokenGenerator tokenGenerator;

    @Override
    public void setIntegrationTestContext(DevServicesContext devServicesContext) {
    }

    @Override
    public Map<String, String> start() {

        String keyId = UUID.randomUUID().toString();
        try {
            // Generate the RSA key pair
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair keyPair = gen.generateKeyPair();

            tokenGenerator = new TokenGenerator(keyId, keyPair.getPrivate());

            generateJwksFile(keyId, keyPair);

            generateTokenResponseFile(Role.NOTICE_PAYER, Role.SLAVE_POS, Role.NODO);

        } catch (IOException | NoSuchAlgorithmException e) {
           logger.error("Error while generating IDP resources", e);
        }

        return null;
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(tokenGenerator, new TestInjector.AnnotatedAndMatchesType(InjectTokenGenerator.class, TokenGenerator.class));
    }

    private static void generateJwksFile(String kid, KeyPair keyPair) throws IOException {

        // Convert to JWK format
        JWK jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyUse(KeyUse.SIGNATURE)
                .keyID(kid)
                .issueTime(new Date())
                .build();

        JWKSet jwkSet = new JWKSet(jwk);

        Files.createDirectories(Path.of("./target/generated-idp-files/"));
        Files.writeString(Path.of("./target/generated-idp-files/jwks.json"), jwkSet.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

    }

    private void generateTokenResponseFile(Role... roles) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String clientId = UUID.randomUUID().toString();
        for (Role role : roles) {
            String token = tokenGenerator.getToken(role, clientId);
            AccessToken accessToken = new AccessToken(token, token, 3600);
            Files.writeString(Path.of("./target/generated-idp-files/" + role.label + ".json"),
                    objectMapper.writeValueAsString(accessToken),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    @Override
    public void stop() {
    }
}

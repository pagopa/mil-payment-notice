package it.pagopa.swclient.mil.paymentnotice.it.resource;

import com.google.common.collect.ImmutableMap;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509ExtensionUtils;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;


public class RedisTestResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    private static final Logger logger = LoggerFactory.getLogger(RedisTestResource.class);
    private static final String REDIS_NETWORK_ALIAS = "redis-it";

    private GenericContainer<?> redisContainer;

    private DevServicesContext devServicesContext;

    public void setIntegrationTestContext(DevServicesContext devServicesContext) {
        this.devServicesContext = devServicesContext;
    }

    @Override
    public Map<String, String> start() {

        logger.info("Starting Redis container...");

        // Start the needed container(s)
        redisContainer = new GenericContainer(DockerImageName.parse("redis:6.0"))
                .withExposedPorts(6379)
                .withNetwork(getNetwork())
                .withNetworkAliases(REDIS_NETWORK_ALIAS)
                //.withNetworkMode(devServicesContext.containerNetworkId().get())
                .waitingFor(Wait.forListeningPort());

        //redisContainer.withLogConsumer(new Slf4jLogConsumer(logger, true));

        final String password = "wCou42NVkv7H8";

        // configure tls
        final boolean tlsEnabled = generateRedisCerts();
        if (tlsEnabled) {
            redisContainer.withFileSystemBind("./target/redis-certs", "/tls");
            redisContainer.setCommand(
                    "--tls-port 6379",
                    "--port 0",
                    "--tls-cert-file /tls/redis.crt",
                    "--tls-key-file /tls/redis.key",
                    // "--tls-ca-cert-file /tls/ca.crt",
                    "--tls-auth-clients no",
                    "--requirepass " + password);
        }
        else {
            redisContainer.setCommand("--requirepass " + password);
        }

        final String redisEndpoint = "redis" + (tlsEnabled ? "s" : "") + "://:" + password + "@" + REDIS_NETWORK_ALIAS + ":" + 6379;

        redisContainer.start();

        final Integer exposedPort = redisContainer.getMappedPort(6379);
        devServicesContext.devServicesProperties().put("test.redis.exposed-port", exposedPort.toString());
        devServicesContext.devServicesProperties().put("test.redis.tls", Boolean.toString(tlsEnabled));
        devServicesContext.devServicesProperties().put("test.redis.password", password);

        // Pass the configuration to the application under test
        return ImmutableMap.of(
                "redis.connection.string", redisEndpoint,
                "quarkus.redis.tls.trust-all", "true"
        );
    }


    // create a "fake" network using the same id as the one that will be used by Quarkus
    // using the network is the only way to make the withNetworkAliases work
    private Network getNetwork() {
        logger.info("devServicesContext.containerNetworkId() -> " + devServicesContext.containerNetworkId());
        return new Network() {
            @Override
            public String getId() {
                return devServicesContext.containerNetworkId().orElse(StringUtils.EMPTY);
            }

            @Override
            public void close() {

            }

            @Override
            public Statement apply(Statement statement, Description description) {
                return null;
            }
        };
    }

    @Override
    public void stop() {
        // Stop the needed container(s)
        if (redisContainer != null) {
            logger.info("Stopping Redis container...");
            redisContainer.stop();
            logger.info("Redis container stopped");
        }
    }

    /**
     * Method to generate the certificate to enable ssl on Redis.
     * MODIFIED from the redis server <a href="https://github.com/redis/redis/blob/cc0091f0f9fe321948c544911b3ea71837cf86e3/utils/gen-test-certs.sh">gen-certs</a> utils
     */
    private static boolean generateRedisCerts() {

        boolean tlsEnabled = false;

        try {
            final Provider bouncyCastleProvider = new BouncyCastleProvider();
            Security.addProvider(bouncyCastleProvider);

            // generate CA keypair
            final KeyPairGenerator caKeyPairGenerator = KeyPairGenerator.getInstance("RSA", bouncyCastleProvider);
            caKeyPairGenerator.initialize(4096);
            final KeyPair caKeyPair = caKeyPairGenerator.generateKeyPair();

            // configure ca certificate
            final X500Name caSubject = new X500Name("CN=Certificate Authority,O=Redis Test");
            final Instant now = Instant.now();
            final X509v3CertificateBuilder caCertificateBuilder = new JcaX509v3CertificateBuilder(
                    caSubject,
                    BigInteger.valueOf(now.getEpochSecond()),
                    Date.from(now),
                    Date.from(now.plus(365*10, ChronoUnit.DAYS)),
                    caSubject,
                    caKeyPair.getPublic()
            );

            // flag certificate as CA
            caCertificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));

            // generate ca certificate
            final JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA256WithRSA").setProvider(bouncyCastleProvider);
            final ContentSigner signer = signerBuilder.build(caKeyPair.getPrivate());

            final X509CertificateHolder caCertificateHolder = caCertificateBuilder.build(signer);
            final JcaX509CertificateConverter converter = new JcaX509CertificateConverter().setProvider(bouncyCastleProvider);

            // save ca certificate to filesystem
            Files.createDirectories(Paths.get("./target/redis-certs"));
            final File caCertificateFile = Paths.get("./target/redis-certs/ca.crt").toFile();
            x509CertificateToPem(converter.getCertificate(caCertificateHolder), caCertificateFile);

            // generate server keypair
            final KeyPairGenerator serverKeyPairGenerator = KeyPairGenerator.getInstance("RSA", bouncyCastleProvider);
            serverKeyPairGenerator.initialize(2048);
            final KeyPair serverKeyPair = caKeyPairGenerator.generateKeyPair();

            // save server key to filesystem
            final File serverKeyFile = Paths.get("./target/redis-certs/redis.key").toFile();
            keyToPem(serverKeyPair.getPrivate(), serverKeyFile);

            // generate server csr
            final JcaContentSignerBuilder csrBuilder = new JcaContentSignerBuilder("SHA256WithRSA").setProvider(bouncyCastleProvider);
            final ContentSigner csrContentSigner = csrBuilder.build(caKeyPair.getPrivate());

            final X500Name subject = new X500Name("CN=Generic-cert,O=Redis Test");
            final PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(subject, serverKeyPair.getPublic());
            final PKCS10CertificationRequest csr = p10Builder.build(csrContentSigner);

            // configure server certificate
            final X509v3CertificateBuilder serverCertificateBuilder = new X509v3CertificateBuilder(
                    caSubject,
                    BigInteger.valueOf(now.getEpochSecond()),
                    Date.from(now),
                    Date.from(now.plus(365, ChronoUnit.DAYS)),
                    csr.getSubject(),
                    csr.getSubjectPublicKeyInfo());

            final X509ExtensionUtils issuedCertExtUtils = new JcaX509ExtensionUtils();

            // flag certificate as not ca
            serverCertificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
            serverCertificateBuilder.addExtension(Extension.authorityKeyIdentifier, false, issuedCertExtUtils.createAuthorityKeyIdentifier(caCertificateHolder));
            serverCertificateBuilder.addExtension(Extension.subjectKeyIdentifier, false, issuedCertExtUtils.createSubjectKeyIdentifier(csr.getSubjectPublicKeyInfo()));

            // generate server certificate
            final X509CertificateHolder serverCertificateHolder = serverCertificateBuilder.build(csrContentSigner);

            // save server certificate to filesystem
            final File serverCertificateFile = Paths.get("./target/redis-certs/redis.crt").toFile();
            x509CertificateToPem(converter.getCertificate(serverCertificateHolder), serverCertificateFile);

            tlsEnabled = true;
        }
        catch (NoSuchAlgorithmException | OperatorCreationException | IOException | CertificateException e) {
            logger.error("Error while generating redis certificates", e);
        }
        catch (Throwable e) {
            logger.error("Generic error while generating redis certificates ", e);
        }

        return tlsEnabled;
    }

    private static void keyToPem(final Key key, final File certificateFile) throws IOException {
        final FileWriter writer = new FileWriter(certificateFile);
        final JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
        pemWriter.writeObject(key);
        pemWriter.flush();
        pemWriter.close();
    }
    private static void x509CertificateToPem(final X509Certificate cert, final File certificateFile) throws IOException {
        final FileWriter writer = new FileWriter(certificateFile);
        final JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
        pemWriter.writeObject(cert);
        pemWriter.flush();
        pemWriter.close();
    }

}

package it.pagopa.swclient.mil.paymentnotice.it.resource;

import java.util.Map;

import it.pagopa.swclient.mil.paymentnotice.it.CustomRedpandaContainer;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.Network;
import org.testcontainers.redpanda.RedpandaContainer;
import org.testcontainers.utility.DockerImageName;
import org.junit.runner.Description;
import com.google.common.collect.ImmutableMap;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;


public class RedpandaTestResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {
    
	private static final Logger logger = LoggerFactory.getLogger(RedpandaTestResource.class);

    private static final String REDPANDA_NETWORK_ALIAS = "redpanda-it";

    private RedpandaContainer redpandaContainer;

    private DevServicesContext devServicesContext;
    
	@Override
	public void setIntegrationTestContext(DevServicesContext devServicesContext){
		  this.devServicesContext = devServicesContext;
	}

	@Override
	public Map<String, String> start() {

		try {
			logger.info("Starting Redpanda container...");

			// this version of testcontainers is not compatible with redpandadata/redpanda
			DockerImageName myImage = DockerImageName.parse("redpandadata/redpanda:v23.1.9")
					.asCompatibleSubstituteFor("docker.redpanda.com/vectorized/redpanda");

			redpandaContainer = new CustomRedpandaContainer(myImage);
			redpandaContainer
					.withNetwork(getNetwork())
					.withNetworkAliases(REDPANDA_NETWORK_ALIAS);

//			redpandaContainer.withLogConsumer(new Slf4jLogConsumer(logger));
			 
			
			
			//redpandaContainer.withFileSystemBind("./src/test/resources/it/mongo", "/home/mongo");
			//redpandaContainer.setCommand("--verbose");
			redpandaContainer.start();
			
			try {
				ExecResult result = redpandaContainer.execInContainer("rpk", "cluster", "config",  "set", "enable_sasl", "true");
				logger.info("1>>>>>>>>>>>>>>>>>>>>>!!!!!!!!!!!!!!!><: {}", result.toString());
				result = redpandaContainer.execInContainer( "rpk", "acl", "user", "create", "admin", "-p", "12345678");
				logger.info("2>>>>>>>>>>>>>>>>>>>>>!!!!!!!!!!!!!!!><: {}", result.toString());
				result = redpandaContainer.execInContainer( "rpk", "cluster", "config", "set", "superusers", "['admin']");
				
				logger.info("3>>>>>>>>>>>>>>>>>>>>>!!!!!!!!!!!!!!!><: {}", result.toString());
				result = redpandaContainer.execInContainer("rpk", "acl", "user", "create", "testuser", "-p", "testuser");
				logger.info("4>>>>>>>>>>>>>>>>>>>>>!!!!!!!!!!!!!!!><: {}", result.toString());
				result = redpandaContainer.execInContainer("rpk", "acl", "create",  "--allow-principal", "*", "--operation", "all",
						"--topic", "presets", "--user", "admin", "--password", "12345678", "--sasl-mechanism", "SCRAM-SHA-256" );
				            
				logger.info("5>>>>>>>>>>>>>>>>>>>>>!!!!!!!!!!!!!!!><: {}", result.toString());

				
//				String stdout = result.getStdout();
			
			}catch (Exception e) {
				logger.error(">>>>>>>>>>>>>>>>>>>!!!!!!!!!!!!11>Error ",e);
			}
//			try {
////				ExecResult result = redpandaContainer.execInContainer("bash", "rpk", "cluster", "config", "set", "superusers", "['admin']");
////				logger.info(">>>>>>>>>>>>>>>>>>>>>><: {}", result.toString());
//				
//				
//				
//				
//				
////				result = redpandaContainer.execInContainer("rpk", "acl", "create",  "--allow-principal", "User:testuser", "--operation", "create,describe",
////						"--cluster", "--user", "suname", "--password", "12345678", "--sasl-mechanism", "SCRAM-SHA-256");
////				logger.info(">>>>>>>>>>>>>>>>>>>>>!!!!!!!!!!!!!!!><: {}", result.toString());
////				String stdout = result.getStdout();
//				
//			}catch (Exception e) {
//				logger.error(">>>>>>>>>>>>>>>>>>>>Error ",e);
//			}
			final Integer exposedPort = redpandaContainer.getMappedPort(9092);
			final String bootstrapServers = redpandaContainer.getBootstrapServers();
			logger.info("Redpanda bootstrap servers: {}", bootstrapServers);

			final String calculatedBootstrapServers = REDPANDA_NETWORK_ALIAS + ":" + exposedPort;
			logger.info("Redpanda calculated bootstrap servers: {}", calculatedBootstrapServers);

			devServicesContext.devServicesProperties().put("test.kafka.bootstrap-server", "localhost:" + exposedPort);

			// Pass the configuration to the application under test
			return ImmutableMap.of(
					"kafka-bootstrap-server", REDPANDA_NETWORK_ALIAS + ":" + 29092,
					"kafka-security-protocol", "SASL_PLAINTEXT",
					"kafka-sasl-mechanism","SCRAM-SHA-256",
					"kafka-sasl-jaas-config","org.apache.kafka.common.security.scram.ScramLoginModule required username=\"testuser\" password=\"testuser\";"
			);
		} catch (Exception e) {
			logger.error("Error while starting redpanda", e);
			throw e;
		}
	}

	// create a "fake" network using the same id as the one that will be used by Quarkus
	// using the network is the only way to make the withNetworkAliases work
	private Network getNetwork() {
		logger.info("devServicesContext.containerNetworkId() -> " + devServicesContext.containerNetworkId());
		return new Network() {
			@Override
			public String getId() {
				return devServicesContext.containerNetworkId().get();
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
		if (null != redpandaContainer) {
			logger.info("Stopping Redpanda container...");
			
			redpandaContainer.stop();
			logger.info("Redpanda container stopped");
		}
		
	}
	
}

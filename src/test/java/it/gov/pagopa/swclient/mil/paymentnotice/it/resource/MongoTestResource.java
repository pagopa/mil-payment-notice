package it.gov.pagopa.swclient.mil.paymentnotice.it.resource;

import com.google.common.collect.ImmutableMap;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class MongoTestResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {
    
	private static final Logger logger = LoggerFactory.getLogger(MongoTestResource.class);

    private static final String MONGO_NETWORK_ALIAS = "mongo-it";

    private GenericContainer<?> mongoContainer;

    private DevServicesContext devServicesContext;
    
	@Override
	public void setIntegrationTestContext(DevServicesContext devServicesContext){
		  this.devServicesContext = devServicesContext;
	}

	@Override
	public Map<String, String> start() {

		logger.info("Starting Mongo container...");

        mongoContainer = new GenericContainer<>(DockerImageName.parse("mongo:latest"))
				.withExposedPorts(27017)
                .withNetwork(getNetwork())
                .withNetworkAliases(MONGO_NETWORK_ALIAS)
                //.withNetworkMode(devServicesContext.containerNetworkId().get())
                .waitingFor(Wait.forListeningPort());

        mongoContainer.withLogConsumer(new Slf4jLogConsumer(logger));

        mongoContainer.withFileSystemBind("./src/test/resources/it/mongo", "/home/mongo");
		//mongoContainer.setCommand("--verbose");
        mongoContainer.start();

		final Integer exposedPort = mongoContainer.getMappedPort(27017);
		devServicesContext.devServicesProperties().put("test.mongo.exposed-port", exposedPort.toString());

        try {
			ExecResult result = mongoContainer.execInContainer("mongosh", "<", "/home/mongo/mongoInit.js");
			logger.info("Init script result {}", result);
		}
		catch (Exception e) {
			logger.error("Error while importing data into DB", e);
		}

        // Pass the configuration to the application under test
		return ImmutableMap.of(
				"mongo-connection-string-1","mongodb://" + MONGO_NETWORK_ALIAS + ":" + 27017,
				"mongo-connection-string-2",MONGO_NETWORK_ALIAS + ":" + 27017,
				"mongo-connect-timeout", "30",
				"mongo-read-timeout", "30",
				"mongo-server-selection-timeout", "30"
		);
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
		if (null != mongoContainer) {
			logger.info("Stopping Mongo container...");
			mongoContainer.stop();
			logger.info("Mongo container stopped");
		}
		
	}
	
}

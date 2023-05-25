package it.pagopa.swclient.mil.paymentnotice.it;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.redpanda.RedpandaContainer;
import org.testcontainers.utility.DockerImageName;

public class CustomRedpandaContainer extends RedpandaContainer {

    private static final Logger logger = LoggerFactory.getLogger(CustomRedpandaContainer.class);

    public CustomRedpandaContainer(String image) {
        super(image);
    }

    public CustomRedpandaContainer(DockerImageName imageName) {
        super(imageName); 
    }

    protected void containerIsStarting(InspectContainerResponse containerInfo) {
        super.containerIsStarting(containerInfo);

        logger.info("getNetworkAliases() -> {}", this.getNetworkAliases());

        String command = "#!/bin/bash\n";
        command = command + "/usr/bin/rpk redpanda start --mode dev-container ";
        command = command + "--kafka-addr PLAINTEXT://0.0.0.0:29092,OUTSIDE://0.0.0.0:9092 ";
        command = command + "--advertise-kafka-addr PLAINTEXT://" + this.getNetworkAliases().get(this.getNetworkAliases().size()-1) + ":29092,OUTSIDE://" + this.getHost() + ":" + this.getMappedPort(9092);
        this.copyFileToContainer(Transferable.of(command, 511), "/testcontainers_start.sh");



    }

}

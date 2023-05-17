package it.pagopa.swclient.mil.paymentnotice.utils;

import io.smallrye.config.ConfigMapping;

import java.util.List;
import java.util.Map;

/**
 * Configuration class used when remapping faults from the node to mil outcomes
 */
@ConfigMapping(prefix = "node.error")
public interface NodeErrorMapping {

    /**
     * @return the list of the possible outcomes returned by the mil layer after an integration with the node
     */
    List<String> outcomes();

    /**
     * @return the map between the node fault codes and the outcomes
     */
    Map<String, Integer> map();

}

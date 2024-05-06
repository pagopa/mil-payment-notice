package it.pagopa.swclient.mil.paymentnotice.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkiverse.cxf.annotation.CXFClient;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.SendPaymentOutcomeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeReq;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeRes;
import it.gov.pagopa.pagopa_api.nodeforpsp.NodeForPsp;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Bean wrapping the async interfaces of the generated CXF SOAP client, and returning their Uni version
 */
@ApplicationScoped
public class NodeForPspWrapper {

    /**
     * The generated CXF SOAP client interface
     */
    @Inject
    @CXFClient("node")
    NodeForPsp nodeForPsp;

    /**
     * Connect timeout vs the node SOAP services
     */
    @ConfigProperty(name="node.soap-client.connect-timeout", defaultValue = "30000")
    long soapClientConnectTimeout;

    /**
     * Read timeout vs the node SOAP services
     */
    @ConfigProperty(name="node.soap-client.read-timeout", defaultValue = "30000")
    long soapClientReadTimeout;

    /**
     * APIM subscription key to be passed as an header in the request to the node endpoint
     */
    @ConfigProperty(name="node.soap-client.apim-subscription-key")
    Optional<String> apimSubscriptionKey;

    /**
     * Wrapper method of the async verifyPaymentNotice interface, returning its response as a Uni
     * @param verifyPaymentNoticeReq the request to be serialized and passed to the node
     * @return an @{@link Uni} emitting the response of the SOAP service
     */
    public Uni<VerifyPaymentNoticeRes> verifyPaymentNotice(VerifyPaymentNoticeReq verifyPaymentNoticeReq) {
    	return Uni.createFrom().future(() ->
                (Future<VerifyPaymentNoticeRes>)nodeForPsp.verifyPaymentNoticeAsync(verifyPaymentNoticeReq, res -> {}));
    }

    /**
     * Wrapper method of the async activatePaymentNotice interface, returning its response as a Uni
     * @param activatePaymentNoticeV2Request the request to be serialized and passed to the node
     * @return an @{@link Uni} emitting the response of the SOAP service
     */
    public Uni<ActivatePaymentNoticeV2Response> activatePaymentNoticeV2Async(ActivatePaymentNoticeV2Request activatePaymentNoticeV2Request) {
    	return Uni.createFrom().future(() ->
                (Future<ActivatePaymentNoticeV2Response>)nodeForPsp.activatePaymentNoticeV2Async(activatePaymentNoticeV2Request, res -> {}));
    }
    
    /**
     * 
     * @param req
     * @return
     */
    public Uni<SendPaymentOutcomeV2Response> sendPaymentOutcomeV2Async(SendPaymentOutcomeV2Request req) {
    	return Uni.createFrom().future(() ->
        	(Future<SendPaymentOutcomeV2Response>)nodeForPsp.sendPaymentOutcomeV2Async(req, res -> {}));
    }

    /**
     * Configures the socket and connection timeout for the Quarkus-CXF SOAP client and
     * the API Management subscription key to be passed in the request
     */
    @PostConstruct
    void configureSoapClient() {
        Client client = ClientProxy.getClient(nodeForPsp);

        final var httpConduit = (HTTPConduit)client.getConduit();
        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout(soapClientConnectTimeout);
        httpClientPolicy.setReceiveTimeout(soapClientReadTimeout);
        httpConduit.setClient(httpClientPolicy);

        if (apimSubscriptionKey.isPresent()) {
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("Ocp-Apim-Subscription-Key", Arrays.asList(apimSubscriptionKey.get()));
            client.getRequestContext().put(Message.PROTOCOL_HEADERS, headers);
        }
    }
    
}

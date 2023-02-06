package it.gov.pagopa.swclient.mil.paymentnotice.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkiverse.cxf.annotation.CXFClient;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeReq;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeRes;
import it.gov.pagopa.pagopa_api.nodeforpsp.NodeForPsp;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class NodeForPspWrapper {

    @Inject
    @CXFClient("node")
    NodeForPsp nodeForPsp;

    @ConfigProperty(name="node.soap-client.connect-timeout", defaultValue = "30000")
    long soapClientConnectTimeout;

    @ConfigProperty(name="node.soap-client.read-timeout", defaultValue = "30000")
    long soapClientReadTimeout;

    @ConfigProperty(name="node.soap-client.apim-subscription-key")
    Optional<String> apimSubscriptionKey;


    public Uni<VerifyPaymentNoticeRes> verifyPaymentNotice(VerifyPaymentNoticeReq verifyPaymentNoticeReq) {
    	return Uni.createFrom().future(() ->
                (Future<VerifyPaymentNoticeRes>)nodeForPsp.verifyPaymentNoticeAsync(verifyPaymentNoticeReq, res -> {}));
    }
    
    public Uni<ActivatePaymentNoticeV2Response> activatePaymentNoticeV2Async(ActivatePaymentNoticeV2Request activatePaymentNoticeV2Request) {
    	return Uni.createFrom().future(() ->
                (Future<ActivatePaymentNoticeV2Response>)nodeForPsp.activatePaymentNoticeV2Async(activatePaymentNoticeV2Request, res -> {}));
    }

    /**
     * Configures the socket and connection timeout for the Quarkus-CXF SOAP client
     * Configures the API Management subscription key to be passed in the request
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

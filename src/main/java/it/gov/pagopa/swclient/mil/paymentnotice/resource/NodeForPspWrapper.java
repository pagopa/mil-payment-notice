package it.gov.pagopa.swclient.mil.paymentnotice.resource;

import java.util.concurrent.Future;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkiverse.cxf.annotation.CXFClient;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Request;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.ActivatePaymentNoticeV2Response;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeReq;
import it.gov.pagopa.pagopa_api.node.nodeforpsp.VerifyPaymentNoticeRes;
import it.gov.pagopa.pagopa_api.nodeforpsp.NodeForPsp;

@ApplicationScoped
public class NodeForPspWrapper {
    @Inject
    @CXFClient("nodo")
    private NodeForPsp nodeForPsp;
    
    public Uni<VerifyPaymentNoticeRes> verifyPaymentNotice(VerifyPaymentNoticeReq verifyPaymentNoticeReq) {
    	return Uni.createFrom().future(() -> (Future<VerifyPaymentNoticeRes>)nodeForPsp.verifyPaymentNoticeAsync(verifyPaymentNoticeReq, res -> {}));
		
    }
    
    public Uni<ActivatePaymentNoticeV2Response> activatePaymentNoticeV2Async(ActivatePaymentNoticeV2Request activatePaymentNoticeV2Request) {
    	return Uni.createFrom().future(() -> (Future<ActivatePaymentNoticeV2Response>)nodeForPsp.activatePaymentNoticeV2Async(activatePaymentNoticeV2Request, res -> {}));
		
    }
    
}

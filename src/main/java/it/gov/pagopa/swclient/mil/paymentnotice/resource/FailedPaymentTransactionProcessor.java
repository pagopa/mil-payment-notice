package it.gov.pagopa.swclient.mil.paymentnotice.resource;

import io.quarkus.logging.Log;
import io.quarkus.vertx.ConsumeEvent;
import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodeRestService;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentRequest;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;

@ApplicationScoped
public class FailedPaymentTransactionProcessor {

    @RestClient
    NodeRestService nodeRestService;

    /**
     * Consumes the event of failed payment transaction
     * @param nodeClosePaymentRequest the object received in request of the closePayment
     */
    @ConsumeEvent("failedPaymentTransaction")
    void consume(NodeClosePaymentRequest nodeClosePaymentRequest) {
        Log.debugf("Asynchronously process %s", nodeClosePaymentRequest);


        nodeRestService.closePayment(nodeClosePaymentRequest)
                .onFailure().retry().withBackOff(Duration.ofSeconds(5), Duration.ofSeconds(5)).atMost(2)
                .subscribe().with(
                        r -> Log.debugf("The node closePayment service responded %s", r),
                        t -> Log.errorf(t, "[%s] Error calling the node closePayment service", ErrorCode.REDIS_ERROR_WHILE_SAVING_PAYMENT_RESULT)
                        );

    }
}

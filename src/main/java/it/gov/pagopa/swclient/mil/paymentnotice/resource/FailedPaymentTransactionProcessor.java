package it.gov.pagopa.swclient.mil.paymentnotice.resource;

import io.quarkus.logging.Log;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodeRestService;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;

@ApplicationScoped
public class FailedPaymentTransactionProcessor {

    /**
     * The reactive REST client for the node interfaces
     */
    @RestClient
    NodeRestService nodeRestService;

    /**
     * Consumes the event of failed payment transaction
     *
     * @param nodeClosePaymentRequest the object received in request of the closePayment
     */
    @ConsumeEvent("failedPaymentTransaction")
    void consume(NodeClosePaymentRequest nodeClosePaymentRequest) {
        Log.debugf("Asynchronously process %s", nodeClosePaymentRequest);

        callNodeClosePayment(nodeClosePaymentRequest)
                .onFailure().retry().withBackOff(Duration.ofSeconds(5), Duration.ofSeconds(5)).atMost(2)
                .subscribe().with(
                        r -> Log.debugf("The node closePayment service responded %s", r),
                        t -> Log.errorf(t, "[%s] Error calling the node closePayment service", ErrorCode.ERROR_AMOUNT_MUST_NOT_BE_NULL)
                        );

    }

    /**
     * Calls the closePayment API on the node and return its response as an {@link Uni}.
     * If the outcome is "KO" it is considered an error and response with a failure.
     *
     * @param nodeClosePaymentRequest the request to be sent to the node
     * @return an {@link Uni} emitting the response from the node, or a failure otherwise
     */
    private Uni<NodeClosePaymentResponse> callNodeClosePayment(NodeClosePaymentRequest nodeClosePaymentRequest) {
        return nodeRestService.closePayment(nodeClosePaymentRequest)
                .onItem().transform(Unchecked.function(r -> {
                    if (r.getOutcome().equals("KO")) throw new InvalidResponseException();
                    else return r;
                }));
    }

    private static class InvalidResponseException extends Exception {
    }
}

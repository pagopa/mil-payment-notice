package it.gov.pagopa.swclient.mil.paymentnotice.resource;

import io.quarkus.logging.Log;
import io.quarkus.vertx.ConsumeEvent;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionEntity;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;

@ApplicationScoped
public class AsyncUpdateTransactionProcessor {

    /**
     * The reactive MongoDB client to store/update the payment transactions
     */
    @Inject
    PaymentTransactionRepository paymentTransactionRepository;


    /**
     * Asynchronously retries updates for a payment transaction
     *
     * @param entity the payment transaction entity to update
     */
    @ConsumeEvent("processUpdateTransaction")
    public void updatePaymentTransactionProcess(PaymentTransactionEntity entity) {

        paymentTransactionRepository.update(entity)
                .onFailure().retry().withBackOff(Duration.ofSeconds(30), Duration.ofSeconds(30)).atMost(2)
                .subscribe().with(
                        r -> Log.debugf("Updated payment transaction %s to status %s",
                                r.transactionId, r.paymentTransaction.getStatus()),
                        t -> Log.errorf(t, "Error while updating data for transaction %s on DB", entity)
                );

    }

}

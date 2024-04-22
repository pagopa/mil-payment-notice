package it.pagopa.swclient.mil.paymentnotice.dao;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepositoryBase;
import it.pagopa.swclient.mil.paymentnotice.utils.Trace;

/**
 *  MongoDB repository for payment transactions, reactive flavor
 */
@Trace
@ApplicationScoped
public class PaymentTransactionRepository implements ReactivePanacheMongoRepositoryBase<PaymentTransactionEntity, String> { //NOSONAR
	
}

package it.pagopa.swclient.mil.paymentnotice.dao;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepositoryBase;

/**
 *  MongoDB repository for payment transactions, reactive flavor
 */
@ApplicationScoped
public class PaymentTransactionRepository implements ReactivePanacheMongoRepositoryBase<PaymentTransactionEntity, String> { //NOSONAR
	
}

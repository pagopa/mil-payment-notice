package it.pagopa.swclient.mil.paymentnotice.dao;

import io.quarkus.mongodb.panache.common.MongoEntity;
import it.pagopa.swclient.mil.bean.CommonHeader;
import it.pagopa.swclient.mil.paymentnotice.bean.PreCloseRequest;
import it.pagopa.swclient.mil.paymentnotice.resource.PaymentResource;
import org.bson.codecs.pojo.annotations.BsonId;


/**
 * Entity bean mapping the payment transaction saved in the DB
 */
@MongoEntity(database = "mil", collection = "paymentTransactions")
public class PaymentTransactionEntity {

	/**
	 * Transaction ID of the payment, as passed by the client in the
	 * {@link PaymentResource#preClose(CommonHeader, PreCloseRequest) preClose} API
	 */
	@BsonId
	public String transactionId;

	/**
	 * The payment transaction data
	 */
	public PaymentTransaction paymentTransaction;

}

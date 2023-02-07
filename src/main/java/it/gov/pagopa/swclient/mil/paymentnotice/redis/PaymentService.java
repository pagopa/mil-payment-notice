package it.gov.pagopa.swclient.mil.paymentnotice.redis;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ReceivePaymentStatusRequest;

/**
 * Redis client, reactive flavor
 * Used to store/retrieve the payment result exhanged with the node
 */
@ApplicationScoped
public class PaymentService {

	private ReactiveValueCommands<String, ReceivePaymentStatusRequest> commands;


	public PaymentService(ReactiveRedisDataSource ds) {
		commands = ds.value(ReceivePaymentStatusRequest.class);
    }


	/**
	 * Returns a cached payment result.
	 * The result could hava outcome = null if the node still has not call back with the actual result on the
	 * {@link it.gov.pagopa.swclient.mil.paymentnotice.resource.PaymentResource#receivePaymentStatus(String, ReceivePaymentStatusRequest)
	 * receivePaymentStatus } API
	 *
	 * @param paymentId the ID of the payment, as passed in the closePayment API
	 * @return a {@link ReceivePaymentStatusRequest} instance containing the payment result for the node,
	 * or null if not present in the cache
	 */
	public Uni<ReceivePaymentStatusRequest> get(String paymentId) {
		return commands.get(paymentId);
	}

	/**
	 * Store a payment result.
	 *
	 * @param paymentId the ID of the payment, as passed in the closePayment API
	 * @return a {@link Uni} emitting Void
	 */
	public Uni<Void> set(String paymentId, ReceivePaymentStatusRequest body) {
		return commands.set(paymentId, body);
	}


	/**
	 * Store a payment result if not already existing
	 *
	 * @param paymentId the ID of the payment, as passed in the closePayment API
	 * @return a {@link Uni} emitting Boolean
	 */
	public Uni<Boolean> setIfNotExist(String paymentId, ReceivePaymentStatusRequest body) {
		return commands.setnx(paymentId, body);
	}

}

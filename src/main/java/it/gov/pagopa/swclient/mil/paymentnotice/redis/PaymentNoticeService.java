package it.gov.pagopa.swclient.mil.paymentnotice.redis;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.Notice;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

/**
 * Redis client, reactive flavor
 * Used to store/retrieve the payment notices data returned by the node in the activatePayment API
 */
@ApplicationScoped
public class PaymentNoticeService {

	private ReactiveValueCommands<String, Notice> commands;

	public PaymentNoticeService(ReactiveRedisDataSource ds) {
		commands = ds.value(Notice.class);
    }


	/**
	 * Returns a list of cached payment notices
	 *
	 * @param paymentTokens a list of payment tokens
	 * @return a {@link Map} containing the payment notices as values or null if not found
	 */
	public Uni<Map<String, Notice>> mget(List<String> paymentTokens) {
		return commands.mget(paymentTokens.toArray(new String[0]));
	}

	/**
	 * Stores a payment notice
	 *
	 * @param paymentToken the ID of the payment
	 * @return a {@link Uni} emitting Void
	 */
	public Uni<Void> set(String paymentToken, Notice notice) {
		return commands.set(paymentToken, notice);
	}


}

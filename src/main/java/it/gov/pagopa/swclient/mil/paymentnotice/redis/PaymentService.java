package it.gov.pagopa.swclient.mil.paymentnotice.redis;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ReceivePaymentStatusRequest;

@ApplicationScoped
public class PaymentService {

	private ReactiveValueCommands<String, ReceivePaymentStatusRequest> commands;


	public PaymentService(ReactiveRedisDataSource ds) {
		commands = ds.value(ReceivePaymentStatusRequest.class);
    }


	public Uni<ReceivePaymentStatusRequest> get(String paymentId) {
		return commands.get(paymentId);
	}

	public Uni<Void> set(String paymentId, ReceivePaymentStatusRequest body) {
		return commands.set(paymentId, body);
	}

}

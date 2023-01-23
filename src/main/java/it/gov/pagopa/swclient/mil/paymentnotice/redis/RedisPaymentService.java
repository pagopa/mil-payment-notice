package it.gov.pagopa.swclient.mil.paymentnotice.redis;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.keys.ReactiveKeyCommands;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PaymentBody;

@ApplicationScoped
public class RedisPaymentService {
	private ReactiveKeyCommands<String> keyCommands; 
	private ReactiveValueCommands<String, PaymentBody> paymentCommands;
	
	public RedisPaymentService(ReactiveRedisDataSource ds, ReactiveRedisDataSource reactive) { 
		paymentCommands	= ds.value(PaymentBody.class); 
        keyCommands 	= reactive.key();  
    }
	public Uni<PaymentBody> get(String key) {
		return paymentCommands.get(key);
	}
	public Uni<Void> set(String key, PaymentBody body) {
		return paymentCommands.set(key, body);
	}
	public Uni<Void> delete(String key) {
		return keyCommands.del(key).replaceWithVoid();
	}
}

package it.gov.pagopa.swclient.mil.paymentnotice.dao;

import org.bson.codecs.pojo.annotations.BsonId;

import io.quarkus.mongodb.panache.common.MongoEntity;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PspInfo;

@MongoEntity(database = "mil", collection = "paymentnotice")
public class PNEntity {
	@BsonId
	public String acquirerId;

	public PspInfo pspInfo;
}

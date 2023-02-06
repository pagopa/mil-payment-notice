package it.gov.pagopa.swclient.mil.paymentnotice.dao;

import org.bson.codecs.pojo.annotations.BsonId;

import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(database = "mil", collection = "pspconf")
public class PspConfEntity {

	@BsonId
	public String acquirerId;

	public PspConfiguration pspConfiguration;

}

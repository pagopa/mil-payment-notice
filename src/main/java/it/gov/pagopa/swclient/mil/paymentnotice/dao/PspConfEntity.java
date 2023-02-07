package it.gov.pagopa.swclient.mil.paymentnotice.dao;

import org.bson.codecs.pojo.annotations.BsonId;

import io.quarkus.mongodb.panache.common.MongoEntity;

/**
 * Entity bean mapping the configuration of a PSP when connecting to the node
 */
@MongoEntity(database = "mil", collection = "pspconf")
public class PspConfEntity {

	/**
	 * The ID of the acquirer
	 */
	@BsonId
	public String acquirerId;

	/**
	 * The configuration of the PSP
	 */
	public PspConfiguration pspConfiguration;

}

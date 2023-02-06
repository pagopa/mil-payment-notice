package it.gov.pagopa.swclient.mil.paymentnotice.dao;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;

@ApplicationScoped
public class PspConfRepository implements ReactivePanacheMongoRepository<PspConfEntity> {

}

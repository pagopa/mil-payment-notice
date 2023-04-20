package it.gov.pagopa.swclient.mil.paymentnotice.resource;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonParseException;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.eventbus.EventBus;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PaymentMethod;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PaymentTransactionOutcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PreCloseRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PreCloseResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.NodeClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.client.bean.PspConfiguration;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.Notice;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PaymentTransaction;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionEntity;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionRepository;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PaymentTransactionStatus;
import it.gov.pagopa.swclient.mil.paymentnotice.redis.PaymentNoticeService;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.NodeApi;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.PaymentNoticeConstants;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.bean.CommonHeader;
import it.gov.pagopa.swclient.mil.bean.Errors;
import it.gov.pagopa.swclient.mil.paymentnotice.ErrorCode;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ReceivePaymentStatusResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ReceivePaymentStatusRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodeRestService;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

@Path("/payments")
public class PaymentResource extends BasePaymentResource {

    /**
     * The reactive REDIS client for retrieving the activated payment notices
     */
    @Inject
    PaymentNoticeService paymentNoticeService;

    /**
     * The reactive MongoDB client to store/update the payment transactions
     */
    @Inject
    PaymentTransactionRepository paymentTransactionRepository;

    /**
     * The reactive REST client for the node interfaces
     */
    @RestClient
    NodeRestService nodeRestService;

    /**
     * The Vert.X bus used to asynchronously process the closePayment request in case of an KO outcome
     * of the e-payment transaction
     */
    @Inject
    EventBus bus;

    /**
     * The value of the Max-Retries header to be sent in response to the closePayment API
     */
    @ConfigProperty(name="paymentnotice.closepayment.max-retry", defaultValue = "3")
    int closePaymentMaxRetry;

    /**
     * The value of the Retry-After header to be sent in response to the closePayment API
     */
    @ConfigProperty(name="paymentnotice.closepayment.retry-after", defaultValue = "30")
    int closePaymentRetryAfter;

    /**
     * The base URL for the location header returned by the closePayment (i.e. the API management base URL)
     */
    @ConfigProperty(name="paymentnotice.closepayment.location.base-url")
    String closePaymentLocationBaseURL;

    /**
     * The maximum number of transactions returned by the getPayments API
     */
    @ConfigProperty(name="paymentnotice.getpayments.max-transactions", defaultValue = "30")
    int getPaymentsMaxTransactions;

    /**
     * How many days in the past the getPayment API will filter when retrieving the transactions
     */
    @ConfigProperty(name="paymentnotice.getpayments.days-before", defaultValue = "30")
    int getPaymentsDaysBefore;

    /**
     * Initializes the close payment flow.
     * If the outcome in request is PRE_CLOSE, retrieves the payment notice data from the cache and store the transaction in the DB.
     * If the outcome is ABORT, calls the node to free the activated payment notices and stores a new transaction in the DB.
     * Returns a 409 if the transaction already exists in the DB.
     *
     * @param headers the object containing all the common headers used by the mil services
     * @param preCloseRequest a {@link PreCloseRequest} instance containing the payment tokens
     * @return a {@link PreCloseResponse} instance containing the OK outcome of the pre-close operation
     */
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> preClose(
            @Valid @BeanParam
            CommonHeader headers,
            @Valid @NotNull(message = "[" + ErrorCode.PRE_CLOSE_REQUEST_MUST_NOT_BE_EMPTY + "] request must not be empty")
            PreCloseRequest preCloseRequest) {

        Log.debugf("preClose - Input parameters: %s, %s", headers, preCloseRequest);

        if (PaymentTransactionOutcome.PRE_CLOSE.name().equals(preCloseRequest.getOutcome())) {
            return retrieveNoticesFromCache(preCloseRequest.getPaymentTokens())
                    .chain(notices -> storePaymentTransaction(headers, preCloseRequest, notices));
        } else {
            // if outcome is ABORTED, call the node to free the activated payment notices
            String transactionId = RandomStringUtils.random(32, 0, 0, true, true, null, new SecureRandom());

            return retrievePSPConfiguration(headers.getRequestId(), headers.getAcquirerId(), NodeApi.CLOSE)
                    .chain(pspConf -> retrieveNoticesFromCache(preCloseRequest.getPaymentTokens())
                            .onFailure().recoverWithItem(List.of()) // ignore failure in accessing cache
                            .call(notices -> {
                                if (!notices.isEmpty()) {
                                    var transactionEntity = createPaymentTransactionEntity(headers, transactionId,
                                            null, notices, preCloseRequest.getOutcome());

                                    var nodeClosePaymentRequest = createNodeClosePaymentRequest(PaymentMethod.PAYMENT_CARD.name(),
                                            LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).toString(),
                                            Outcome.KO, transactionEntity.paymentTransaction, pspConf);

                                    // we are using the non-mutiny variant of the event bus because this is a fire-and-forget scenario
                                    bus.<String>request("processClosePayment", nodeClosePaymentRequest);
                                } else {
                                    Log.errorf("No notice found in cache, skipping call node");
                                }
                                return Uni.createFrom().nullItem();
                            })
                            .map(transactionEntity -> {
                                PreCloseResponse okResponse = new PreCloseResponse();
                                okResponse.setOutcome(Outcome.OK.name());
                                Log.debugf("preClose - Response transactionId=%s, %s", transactionId, okResponse);
                                return Response
                                        .status(Status.CREATED)
                                        .entity(okResponse)
                                        .build();
                            }));
        }
    }

    /**
     * Retrieves the payment notices stored in cache by their payment tokens.
     * If a payment token is not found in cache the Uni will not fail.
     *
     * @param paymentTokens the list of payment tokens for which to retrieve the cached notice data
     * @return an {@link Uni} emitting the list of {@link Notice} found in cache
     */
    private Uni<List<Notice>> retrieveNoticesFromCache(List<String> paymentTokens) {
        return paymentNoticeService.mget(paymentTokens)
                .onFailure().transform(t -> {
                    Log.errorf(t, "[%s] Error while retrieving notices from cache", ErrorCode.ERROR_RETRIEVING_DATA_FROM_REDIS);
                    return new InternalServerErrorException(Response
                            .status(Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_DATA_FROM_REDIS)))
                            .build());
                })
                .map(noticeMap -> noticeMap.entrySet().stream()
                        .filter(entry -> {
                            if (entry.getValue() == null) {
                                Log.errorf("Notice with payment token %s not found in cache", entry.getKey());
                                return false;
                            } else return true;
                        })
                        .map(Map.Entry::getValue)
                        .toList()
                );
    }

    /**
     * Final branch of the preClose Uni, for outcome PRE_CLOSE.
     * Creates the payment transaction entity and stores it in the DB.
     * Returns 500 if it cannot store the transaction in the DB.
     *
     * @param headers the object containing all the common headers used by the mil services
     * @param preCloseRequest a {@link PreCloseRequest} instance containing the payment tokens
     * @param notices the list of notices retrieved from the cache
     * @return an {@link Uni} emitting the {@link PreCloseResponse}
     */
    private Uni<Response> storePaymentTransaction(CommonHeader headers, PreCloseRequest preCloseRequest, List<Notice> notices) {

        if (preCloseRequest.getPaymentTokens().size() != notices.size()) {
            Log.errorf("[%s] Found %s notices in cache instead of %s passed in request",
                    ErrorCode.CACHED_NOTICE_NOT_FOUND, notices.size(), preCloseRequest.getPaymentTokens().size());
            return Uni.createFrom().failure(new BadRequestException(
                    Response
                            .status(Status.BAD_REQUEST)
                            .entity(new Errors(List.of(ErrorCode.CACHED_NOTICE_NOT_FOUND)))
                            .build())
            );
        }

        long cachedTotal = notices.stream().map(Notice::getAmount).reduce(Long::sum).orElse(0L);
        if (preCloseRequest.getTotalAmount() != cachedTotal) {
            Log.errorf("[%s] Total amount %s in request is different from the total amount %s of cached notices",
                    ErrorCode.ERROR_TOTAL_AMOUNT_MUST_MATCH_TOTAL_CACHED_VALUE, preCloseRequest.getTotalAmount(), cachedTotal);
            return Uni.createFrom().failure(new BadRequestException(
                    Response
                            .status(Status.BAD_REQUEST)
                            .entity(new Errors(List.of(ErrorCode.ERROR_TOTAL_AMOUNT_MUST_MATCH_TOTAL_CACHED_VALUE)))
                            .build())
            );
        }

        // create a payment transaction with status PRE_CLOSE
        PaymentTransactionEntity entity = createPaymentTransactionEntity(
                headers,
                preCloseRequest.getTransactionId(),
                preCloseRequest.getFee(),
                notices,
                preCloseRequest.getOutcome());

        // store the payment transaction in the db
        Log.debugf("Storing payment transaction %s on DB", entity.paymentTransaction);
        return paymentTransactionRepository.persist(entity)
                .onFailure().transform(t -> {
                    if (t instanceof MongoWriteException writeException
                            && writeException.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                        // if a transaction is found, return 409 CONFLICT
                        Log.errorf(t, "Payment transaction %s already exist on DB", preCloseRequest.getTransactionId());
                        return new ClientErrorException(Response
                                .status(Status.CONFLICT)
                                .build());
                    } else {
                        Log.errorf(t, "[%s] Error while storing payment transaction %s on db",
                                ErrorCode.ERROR_STORING_DATA_IN_DB, preCloseRequest.getTransactionId());
                        return new InternalServerErrorException(Response
                                .status(Status.INTERNAL_SERVER_ERROR)
                                .entity(new Errors(List.of(ErrorCode.ERROR_STORING_DATA_IN_DB)))
                                .build());
                    }
                })
                // return OK to the caller
                .map(e -> {
                    PreCloseResponse okResponse = new PreCloseResponse();
                    okResponse.setOutcome(Outcome.OK.name());
                    Log.debugf("preClose - Response: %s", okResponse);
                    return Response
                            .status(Status.CREATED)
                            .location(getPaymentStatusURI(preCloseRequest.getTransactionId()))
                            .entity(okResponse)
                            .build();
                });

    }


    /**
     * Closes a payment transaction previously created with the {@link #preClose(CommonHeader, PreCloseRequest) preClose} API.
     * Calls the node to pass the outcome of the e-money transaction and updates the payment transaction on the DB.
     * The HTTP response contains a Location header with the URL to invoke to retrieve the final status of the closing operation.
     *
     * @param headers the object containing all the common headers used by the mil services
     * @param transactionId the transaction ID of the e-money transaction passed in request of the {@link #preClose(CommonHeader, PreCloseRequest)}
     * @param closePaymentRequest a {@link ClosePaymentRequest} instance containing the outcome of the e-payment transaction
     * @return a {@link ClosePaymentResponse} instance containing the remapped outcome from the node
     */
    @PATCH
    @Path("/{transactionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> closePayment(
            @Valid @BeanParam
            CommonHeader headers,
            @Pattern(regexp = PaymentNoticeConstants.TRANSACTION_ID_REGEX,
                    message = "[" + ErrorCode.ERROR_TRANSACTION_ID_MUST_MATCH_REGEXP + "] transactionId must match \"{regexp}\"")
            String transactionId,
            @Valid
            @NotNull(message = "[" + ErrorCode.CLOSE_REQUEST_MUST_NOT_BE_EMPTY + "] request must not be empty")
            ClosePaymentRequest closePaymentRequest) {

        Log.debugf("closePayment - Input parameters: %s, transactionId : %s, %s",
                headers, transactionId, closePaymentRequest);

        return retrievePSPConfiguration(headers.getRequestId(), headers.getAcquirerId(), NodeApi.CLOSE)
                .chain(pspConf -> retrievePaymentTransaction(transactionId, headers)
                        .chain(txEntity -> {
                            Log.debugf("Retrieved payment transaction: %s", txEntity.paymentTransaction);
                            if (PaymentTransactionOutcome.CLOSE.name().equals(closePaymentRequest.getOutcome())) {
                                return callNodeClosePaymentOutcomeOk(closePaymentRequest, txEntity, pspConf);
                            } else {
                                NodeClosePaymentRequest nodeClosePaymentRequest =
                                        createNodeClosePaymentRequest(closePaymentRequest.getPaymentMethod(),
                                                closePaymentRequest.getPaymentTimestamp(), Outcome.KO, txEntity.paymentTransaction, pspConf);

                                // asynchronously process the close payment
                                // we are using the non-mutiny variant of the event bus because this is a fire-and-forget scenario
                                bus.<String>request("processClosePayment", nodeClosePaymentRequest);

                                // update transaction on DB
                                txEntity.paymentTransaction.setStatus(PaymentTransactionStatus.ERROR_ON_PAYMENT.name());
                                txEntity.paymentTransaction.setPaymentMethod(closePaymentRequest.getPaymentMethod());
                                txEntity.paymentTransaction.setPaymentTimestamp(closePaymentRequest.getPaymentTimestamp());
                                txEntity.paymentTransaction.setCloseTimestamp(getTimestamp());

                                return paymentTransactionRepository.update(txEntity)
                                        .onFailure().recoverWithItem(t -> {
                                            // asynchronously retry the update
                                            bus.<String>request("processUpdateTransaction", txEntity);
                                            return txEntity;
                                        })
                                        .map(e -> {
                                            Log.debugf("closePayment - Response status %s", Status.ACCEPTED);
                                            return Response.status(Status.ACCEPTED).build();
                                        });
                            }
                        })
                );
    }

    /**
     * Retrieves the list of the last transactions done by the terminal
     *
     * @param headers the object containing all the common headers used by the mil services
     * @return a list of {@link PaymentTransaction} instances containing the detail of the payment transaction and its status
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getPayments(@Valid @BeanParam CommonHeader headers) {

        Log.debugf("getPayments - Input parameters: %s", headers);

        return retrievePaymentTransactions(headers)
                .map(txEntityList -> {
                    var transactionList = txEntityList.stream().map(txEntity -> txEntity.paymentTransaction).toList();
                    Log.debugf("closePayment - Response: %s", transactionList);
                    return Response
                            .status(Status.OK)
                            .entity(transactionList)
                            .build();
                });
    }

    /**
     * Retrieves the status of a payment transaction by its id
     *
     * @param headers the object containing all the common headers used by the mil services
     * @param transactionId the id of the payment transaction as returned by the {@link #closePayment(CommonHeader, String, ClosePaymentRequest) closePayment} method
     * @return a {@link PaymentTransaction} instance containing the detail of the payment transaction and its status
     */
    @GET
    @Path("/{transactionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getPaymentStatus(@Valid @BeanParam CommonHeader headers,
                                          @Pattern(regexp = PaymentNoticeConstants.TRANSACTION_ID_REGEX,
                                                  message = "[" + ErrorCode.ERROR_TRANSACTION_ID_MUST_MATCH_REGEXP + "] transactionId must match \"{regexp}\"")
                                          String transactionId) {

		Log.debugf("getPaymentStatus - Input parameters: %s, transactionId: %s", headers, transactionId);

        return retrievePaymentTransaction(transactionId, headers)
                .map(txEntity -> {
                    Log.debugf("getPaymentStatus - Response: %s", txEntity.paymentTransaction);
                    return Response.status(Status.OK).entity(txEntity.paymentTransaction).build();
                });
    }


    /**
     * Stores the status of a close payment operation by its transactionId
     *
     * @param transactionId the id of the payment transaction as returned by the {@link #closePayment(CommonHeader, String, ClosePaymentRequest) closePayment} method
     * @param receivePaymentStatusRequest a {@link ReceivePaymentStatusRequest} instance containing the status of the close payment operation and the details of the payment notices
     * @return a {@link ReceivePaymentStatusResponse} instance containing the outcome of the store operation
     */
    @POST
    @Path("/{transactionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> receivePaymentStatus( @Pattern(regexp = PaymentNoticeConstants.TRANSACTION_ID_REGEX, message = "[" + ErrorCode.ERROR_TRANSACTION_ID_MUST_MATCH_REGEXP + "] transactionId must match \"{regexp}\"")
                                               String transactionId,
                                               @Valid ReceivePaymentStatusRequest receivePaymentStatusRequest) {

		Log.debugf("receivePaymentStatus - Input parameters: transactionId %s, %s", transactionId, receivePaymentStatusRequest);

        return paymentTransactionRepository.findById(transactionId)
                .onFailure().transform(t -> {
                    Log.errorf(t, "[%s] Error while retrieving payment transaction %s from DB",
                            ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB, transactionId);
                    return new InternalServerErrorException(Response
                            .status(Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB)))
                            .build());
                })
                .onItem().ifNull().failWith(() -> {
                    // if no transaction is found on redis return PAYMENT_NOT_FOUND as outcome
                    Log.errorf("Payment transaction %s not found on DB", transactionId);
                    ReceivePaymentStatusResponse receiveResponse = new ReceivePaymentStatusResponse();
                    receiveResponse.setOutcome("PAYMENT_NOT_FOUND");
                    Log.debugf("receivePaymentStatus - Response: %s", receiveResponse);
                    return new NotFoundException(Response.status(Status.NOT_FOUND).entity(receiveResponse).build());
                })
                .map(entity -> {
                    // update payment transaction record with data from the node response
                    receivePaymentStatusRequest.getPayments()
                            .forEach(pay -> {
                                Optional<Notice> optNotice = entity.paymentTransaction.getNotices().stream()
                                        .filter(not -> pay.getPaymentToken().equals(not.getPaymentToken()))
                                        .findFirst();
                                optNotice.ifPresentOrElse(
                                        notice -> {
                                            notice.setCreditorReferenceId(pay.getCreditorReferenceId());
                                            notice.setDebtor(pay.getDebtor());
                                        },
                                        () -> Log.errorf("Payment token %s not found on DB", pay.getPaymentToken())
                                );
                            });

                    if (Outcome.OK.name().equalsIgnoreCase(receivePaymentStatusRequest.getOutcome())) {
                        entity.paymentTransaction.setStatus(PaymentTransactionStatus.CLOSED.name());
                    } else {
                        entity.paymentTransaction.setStatus(PaymentTransactionStatus.ERROR_ON_RESULT.name());
                    }

                    entity.paymentTransaction.setCallbackTimestamp(getTimestamp());
                    entity.paymentTransaction.setPaymentDate(receivePaymentStatusRequest.getPaymentDate());

                    return entity;
                })
                .chain(entity -> paymentTransactionRepository.update(entity)
                        .onFailure().recoverWithItem(t -> {
                            // asynchronously retry the update
                            bus.<String>request("processUpdateTransaction", entity);
                            return entity;
                        })
                        .map(e -> {
                            ReceivePaymentStatusResponse receiveResponse = new ReceivePaymentStatusResponse();
                            receiveResponse.setOutcome(Outcome.OK.toString());
                            Log.debugf("receivePaymentStatus - Response: %s", receiveResponse);
                            return Response.status(Status.OK).entity(receiveResponse).build();
                        })
                );
    }

    /**
     * Final branch of the closePayment Uni, calls the node to notify the OK status of the e-money transaction
     * Remaps the response of the node to an OK/KO outcome to the client, and updates the payment transaction on the DB.
     * If the update on the DB fails, will not respond with an error.
     *
     * @param closePaymentRequest the object received in request of the {@link #closePayment(CommonHeader, String, ClosePaymentRequest)}
     * @param paymentTransactionEntity the object containing the data of the payment transaction, retrieved from the DB
     * @param pspConfiguration the configuration of the PSP, retrieved from the MIL configuration API
     * @return a {@link Uni} emitting a {@link ClosePaymentResponse} containing the OK / KO outcome of the call to the node
     */
    private Uni<Response> callNodeClosePaymentOutcomeOk(ClosePaymentRequest closePaymentRequest,
                                                        PaymentTransactionEntity paymentTransactionEntity,
                                                        PspConfiguration pspConfiguration) {

        var nodeClosePaymentRequest = createNodeClosePaymentRequest(closePaymentRequest.getPaymentMethod(),
                closePaymentRequest.getPaymentTimestamp(), Outcome.OK,
                paymentTransactionEntity.paymentTransaction, pspConfiguration);

        Log.debugf("Calling the node closePayment service for OK outcome, request: %s", nodeClosePaymentRequest);
        return nodeRestService.closePayment(nodeClosePaymentRequest)
                .onItemOrFailure()
                .transformToUni((closePayRes, error) -> {
                    boolean outcomeOk = false;
                    PaymentTransaction paymentTransaction = paymentTransactionEntity.paymentTransaction;
                    if (error != null) {
                        Log.errorf(error, "Error calling the node closePayment service");
                        if (error instanceof ClientWebApplicationException webEx) {
                            outcomeOk = validateClosePaymentError(webEx);
                        } else if (error instanceof TimeoutException) {
                            Log.debug("Node closePayment went in timeout, responding with outcome OK");
                            outcomeOk = true; // for a timeout we return outcome ok
                        } else {
                            // in any other case we return 500
                            return Uni.createFrom().item(
                                    Response.status(Status.INTERNAL_SERVER_ERROR)
                                            .entity(new Errors(List.of(ErrorCode.ERROR_CALLING_NODE_REST_SERVICES)))
                                            .build());
                        }
                    } else {
                        Log.debugf("Node closePayment service returned a 200 status, response: %s", closePayRes);
                        if (Outcome.OK.name().equals(closePayRes.getOutcome())) {
                            outcomeOk = true;
                            paymentTransaction.setStatus(PaymentTransactionStatus.PENDING.name());
                        }
                    }

                    // returning response
                    ClosePaymentResponse closePaymentResponse = new ClosePaymentResponse();
                    Response.ResponseBuilder responseBuilder = Response.status(Status.OK);
                    if (outcomeOk) {
                        closePaymentResponse.setOutcome(Outcome.OK);
                        responseBuilder
                                .location(getPaymentStatusURI(paymentTransaction.getTransactionId()))
                                .header("Retry-After", closePaymentRetryAfter)
                                .header("Max-Retries", closePaymentMaxRetry);
                        paymentTransaction.setStatus(PaymentTransactionStatus.PENDING.name());
                    } else {
                        closePaymentResponse.setOutcome(Outcome.KO);
                        paymentTransaction.setStatus(PaymentTransactionStatus.ERROR_ON_CLOSE.name());
                    }

                    paymentTransaction.setPaymentMethod(closePaymentRequest.getPaymentMethod());
                    paymentTransaction.setPaymentTimestamp(closePaymentRequest.getPaymentTimestamp());
                    paymentTransaction.setCloseTimestamp(getTimestamp());

                    Log.debugf("closePayment - Response %s", closePaymentResponse);

                    // update transaction on DB
                    return paymentTransactionRepository.update(paymentTransactionEntity)
                            .onFailure().recoverWithItem(t -> {
                                // if was not possible to update the transaction on the DB, still return 200
                                Log.errorf(t,"[%s] Error while updating transaction %s on DB",
                                        ErrorCode.ERROR_UPDATING_DATA_IN_DB, paymentTransaction.getTransactionId());
                                return paymentTransactionEntity;
                            })
                            .map(entity -> responseBuilder.entity(closePaymentResponse).build());
                });

    }


    /**
     * Retrieves a payment transaction from the DB
     *
     * @param transactionId the payment transaction ID
     * @return an {@link Uni} emitting the {@link PaymentTransactionEntity} or an error if not found
     */
    private Uni<PaymentTransactionEntity> retrievePaymentTransaction(String transactionId, CommonHeader headers) {
        return paymentTransactionRepository.findById(transactionId)
                .onFailure().transform(t -> {
                    Log.errorf(t, "[%s] Error while retrieving payment transaction %s from DB",
                            ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB, transactionId);
                    return new InternalServerErrorException(Response
                            .status(Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB)))
                            .build());
                })
                .onItem().ifNull().failWith(() -> {
                    Log.errorf("[%s] Payment transaction %s not found on DB", ErrorCode.UNKNOWN_PAYMENT_TRANSACTION, transactionId);
                    return new NotFoundException(Response
                            .status(Status.NOT_FOUND)
                            .build());
                })
                .map(Unchecked.function(txEntity -> {
                    if (!isTransactionLinkedToClient(headers, txEntity.paymentTransaction)) {
                        Log.errorf("[%s] Transaction id %s was not created by terminalId %s, merchantId %s, channel %s",
                                ErrorCode.UNKNOWN_PAYMENT_TRANSACTION, transactionId, headers.getTerminalId(),
                                headers.getMerchantId(), headers.getChannel());
                        throw new NotFoundException(Response
                                .status(Status.NOT_FOUND)
                                .build());
                    } else {
                        return txEntity;
                    }
                }));
    }

    /**
     * Retrieves a list of payment transaction from the DB
     *
     * @param headers the object containing all the common headers used by the mil services
     * @return an {@link Uni} emitting the {@link PaymentTransactionEntity} list
     */
    private Uni<List<PaymentTransactionEntity>> retrievePaymentTransactions(CommonHeader headers) {

        return paymentTransactionRepository.find(
                """
                        paymentTransaction.terminalId = ?1 and
                        paymentTransaction.merchantId = ?2 and
                        paymentTransaction.channel    = ?3 and
                        paymentTransaction.acquirerId = ?4 and
                        paymentTransaction.insertTimestamp >= ?5
                      """,
                        Sort.by("paymentTransaction.insertTimestamp").descending(),
                        headers.getTerminalId(),
                        headers.getMerchantId(),
                        headers.getChannel(),
                        headers.getAcquirerId(),
                        LocalDateTime.ofInstant(Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC)
                                .toLocalDate().atTime(LocalTime.MIN) // set midnight
                                .minus(getPaymentsDaysBefore, ChronoUnit.DAYS) // transaction of the last 30 days
                                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .withBatchSize(getPaymentsMaxTransactions)
                .page(Page.ofSize(getPaymentsMaxTransactions))
                .list()
                .onFailure().transform(t -> {
                    Log.errorf(t, "[%s] Error while retrieving payment transactions from DB",
                            ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB);
                    return new InternalServerErrorException(Response
                            .status(Status.INTERNAL_SERVER_ERROR)
                            .entity(new Errors(List.of(ErrorCode.ERROR_RETRIEVING_DATA_FROM_DB)))
                            .build());
                });
    }

    /**
     * Checks if a ClientWebApplicationException (wrapping a status != 2xx) or an unparsable response
     * returned from the REST client connecting to the node closePayment REST API should be mapped to an OK outcome
     *
     * @param webEx the exception returned by the rest client
     * @return true if the error maps to an OK outcome, false otherwise
     */
    private boolean validateClosePaymentError(ClientWebApplicationException webEx) {

        boolean outcomeOk = false;

        // un unparsable response is wrapped in a ClientWebApplicationException exception
        // with 404 status, so we need to distinguish it from the real 404 case
        if (ExceptionUtils.indexOfThrowable(webEx, JsonParseException.class) != -1) {
            Log.debug("Node closePayment returned an unparsable response");
            outcomeOk = true;
        } else {
            int nodeResponseStatus = webEx.getResponse().getStatus();
            String nodeResponse = webEx.getResponse().readEntity(String.class);
            // for these two statuses we return outcome ko
            if (nodeResponseStatus == 400 || nodeResponseStatus == 404) {
                Log.debugf("Node closePayment returned a %s status, response: %s", nodeResponseStatus, nodeResponse);
            } else { // for any other status we return outcome ok
                Log.debugf("Node closePayment returned a %s status, response: %s", nodeResponseStatus, nodeResponse);
                outcomeOk = true;
            }
        }
        return outcomeOk;
    }


    /**
     * Returns the URI of the getPaymentStatus API for a payment transaction, to be used in the Location header
     * of the {@link #preClose(CommonHeader, PreCloseRequest)} and {@link #closePayment(CommonHeader, String, ClosePaymentRequest)} APIs
     *
     * @param transactionId the payment transaction ID
     * @return the {@link URI URI} to be used in the Location header
     */
    private URI getPaymentStatusURI(String transactionId) {
        return URI.create(closePaymentLocationBaseURL + "/payments/" + transactionId);
    }

}

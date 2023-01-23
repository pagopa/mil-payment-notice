package it.gov.pagopa.swclient.mil.paymentnotice;

import static io.restassured.RestAssured.given;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.InternalServerErrorException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentRequest;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.ClosePaymentResponse;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.Outcome;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PaymentRequestBody;
import it.gov.pagopa.swclient.mil.paymentnotice.bean.PspInfo;
import it.gov.pagopa.swclient.mil.paymentnotice.client.NodoService;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PNEntity;
import it.gov.pagopa.swclient.mil.paymentnotice.dao.PNRepository;
import it.gov.pagopa.swclient.mil.paymentnotice.exception.NodeExceptionManageKo;
import it.gov.pagopa.swclient.mil.paymentnotice.exception.NodeExceptionManageOk;
import it.gov.pagopa.swclient.mil.paymentnotice.resource.ClosePaymentResource;

@QuarkusTest
@TestHTTPEndpoint(ClosePaymentResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClosePaymentResourceTest {
	
	private final static String SESSION_ID			= "a6a666e6-97da-4848-b568-99fedccb642c";
	private final static String API_VERSION			= "1.0.0-alpha-a.b-c-somethinglong+build.1-aef.1-its-okay";
	
	@InjectMock
	@RestClient
	private NodoService nodoService;
	
	@InjectMock
	private PNRepository pnRepository;
	
	@Test
	void testClosePayment_200() throws ParseException, DatatypeConfigurationException {
		
		PspInfo pspInfo = new PspInfo();
		pspInfo.setPspBroker("brocker");
		pspInfo.setPspId("09127491649");
		pspInfo.setPspPassword("xxjaldo");
		
		PNEntity pnEntity 	= new PNEntity();
		pnEntity.acquirerId = "4585625";
		pnEntity.pspInfo	= pspInfo;
		
		PaymentRequestBody paymentRequestBody = new PaymentRequestBody();
		paymentRequestBody.setOutcome(Outcome.OK.toString());
		List<String> tokens = new ArrayList<>();
		tokens.add("648fhg36s95jfg7DS");
		paymentRequestBody.setPaymentTokens(tokens);
		paymentRequestBody.setPaymentMethod("PAGOBANCOMAT");
		paymentRequestBody.setTransactionId("517a4216840E461fB011036A0fd134E1");
		paymentRequestBody.setTotalAmount(234234);
		paymentRequestBody.setFee(897);
		paymentRequestBody.setTimestampOp("2022-11-12T08:53:55");

		Mockito
		.when(pnRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(pnEntity)));
		
		ClosePaymentResponse closePayentResponse = new ClosePaymentResponse();
		closePayentResponse.setOutcome(Outcome.OK.toString());
		
		Mockito.when(nodoService.closePayment(Mockito.any(ClosePaymentRequest.class)))
		.thenReturn(Uni.createFrom().item(closePayentResponse));
		
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.body(paymentRequestBody)
				.when()
				.post("/payment")
				.then()
				.extract()
				.response();

	        Assertions.assertEquals(200, response.statusCode());
	        Assertions.assertNotNull(response.getHeader("Location"));
	        Assertions.assertNotNull(response.getHeader("Retry-after"));
	        Assertions.assertNotNull(response.getHeader("Max-Retry"));
	     
	}
	
	@Test
	void testClosePaymentNodeResponseIs_TimeoutException_return_200() throws ParseException, DatatypeConfigurationException {
		
		PspInfo pspInfo = new PspInfo();
		pspInfo.setPspBroker("brocker");
		pspInfo.setPspId("09127491649");
		pspInfo.setPspPassword("xxjaldo");
		
		PNEntity pnEntity 	= new PNEntity();
		pnEntity.acquirerId = "4585625";
		pnEntity.pspInfo	= pspInfo;
		
		PaymentRequestBody paymentRequestBody = new PaymentRequestBody();
		paymentRequestBody.setOutcome(Outcome.OK.toString());
		List<String> tokens = new ArrayList<>();
		tokens.add("648fhg36s95jfg7DS");
		paymentRequestBody.setPaymentTokens(tokens);
		paymentRequestBody.setPaymentMethod("PAGOBANCOMAT");
		paymentRequestBody.setTransactionId("517a4216840E461fB011036A0fd134E1");
		paymentRequestBody.setTotalAmount(234234);
		paymentRequestBody.setFee(897);
		paymentRequestBody.setTimestampOp("2022-11-12T08:53:55");

		Mockito
		.when(pnRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(pnEntity)));
		
		ClosePaymentResponse closePayentResponse = new ClosePaymentResponse();
		closePayentResponse.setOutcome(Outcome.OK.toString());
		
		Mockito.when(nodoService.closePayment(Mockito.any(ClosePaymentRequest.class)))
		.thenReturn(Uni.createFrom().failure(new TimeoutException()));
		
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.body(paymentRequestBody)
				.when()
				.post("/payment")
				.then()
				.extract()
				.response();

	        Assertions.assertEquals(200, response.statusCode());
	        Assertions.assertNotNull(response.getHeader("Location"));
	        Assertions.assertNotNull(response.getHeader("Retry-after"));
	        Assertions.assertNotNull(response.getHeader("Max-Retry"));
	     
	}
	
	@Test
	void testClosePaymentNodeResponseIs_400_return_200() throws ParseException, DatatypeConfigurationException {
		
		PspInfo pspInfo = new PspInfo();
		pspInfo.setPspBroker("brocker");
		pspInfo.setPspId("09127491649");
		pspInfo.setPspPassword("xxjaldo");
		
		PNEntity pnEntity 	= new PNEntity();
		pnEntity.acquirerId = "4585625";
		pnEntity.pspInfo	= pspInfo;
		
		PaymentRequestBody paymentRequestBody = new PaymentRequestBody();
		paymentRequestBody.setOutcome(Outcome.OK.toString());
		List<String> tokens = new ArrayList<>();
		tokens.add("648fhg36s95jfg7DS");
		paymentRequestBody.setPaymentTokens(tokens);
		paymentRequestBody.setPaymentMethod("PAGOBANCOMAT");
		paymentRequestBody.setTransactionId("517a4216840E461fB011036A0fd134E1");
		paymentRequestBody.setTotalAmount(234234);
		paymentRequestBody.setFee(897);
		paymentRequestBody.setTimestampOp("2022-11-12T08:53:55");

		Mockito
		.when(pnRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(pnEntity)));
		
		ClosePaymentResponse closePayentResponse = new ClosePaymentResponse();
		closePayentResponse.setOutcome(Outcome.OK.toString());
		
		Mockito.when(nodoService.closePayment(Mockito.any(ClosePaymentRequest.class)))
		.thenReturn(Uni.createFrom().failure(new NodeExceptionManageKo()));
		
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.body(paymentRequestBody)
				.when()
				.post("/payment")
				.then()
				.extract()
				.response();

	        Assertions.assertEquals(200, response.statusCode());
	        Assertions.assertEquals(Outcome.KO.toString(), response.jsonPath().getString("outcome"));
	     
	}
	
	@Test
	void testClosePaymentNodeResponseIs_408_return_200() throws ParseException, DatatypeConfigurationException {
		
		PspInfo pspInfo = new PspInfo();
		pspInfo.setPspBroker("brocker");
		pspInfo.setPspId("09127491649");
		pspInfo.setPspPassword("xxjaldo");
		
		PNEntity pnEntity 	= new PNEntity();
		pnEntity.acquirerId = "4585625";
		pnEntity.pspInfo	= pspInfo;
		
		PaymentRequestBody paymentRequestBody = new PaymentRequestBody();
		paymentRequestBody.setOutcome(Outcome.OK.toString());
		List<String> tokens = new ArrayList<>();
		tokens.add("648fhg36s95jfg7DS");
		paymentRequestBody.setPaymentTokens(tokens);
		paymentRequestBody.setPaymentMethod("PAGOBANCOMAT");
		paymentRequestBody.setTransactionId("517a4216840E461fB011036A0fd134E1");
		paymentRequestBody.setTotalAmount(234234);
		paymentRequestBody.setFee(897);
		paymentRequestBody.setTimestampOp("2022-11-12T08:53:55");

		Mockito
		.when(pnRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(pnEntity)));
		
		ClosePaymentResponse closePayentResponse = new ClosePaymentResponse();
		closePayentResponse.setOutcome(Outcome.OK.toString());
		
		Mockito.when(nodoService.closePayment(Mockito.any(ClosePaymentRequest.class)))
		.thenReturn(Uni.createFrom().failure(new NodeExceptionManageOk()));
		
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.body(paymentRequestBody)
				.when()
				.post("/payment")
				.then()
				.extract()
				.response();

	        Assertions.assertEquals(200, response.statusCode());
	        Assertions.assertNotNull(response.getHeader("Location"));
	        Assertions.assertNotNull(response.getHeader("Retry-after"));
	        Assertions.assertNotNull(response.getHeader("Max-Retry"));
	     
	}
	
	@Test
	void testClosePaymentResponds_500() throws ParseException, DatatypeConfigurationException {
		
		PspInfo pspInfo = new PspInfo();
		pspInfo.setPspBroker("brocker");
		pspInfo.setPspId("09127491649");
		pspInfo.setPspPassword("xxjaldo");
		
		PNEntity pnEntity 	= new PNEntity();
		pnEntity.acquirerId = "4585625";
		pnEntity.pspInfo	= pspInfo;
		
		PaymentRequestBody paymentRequestBody = new PaymentRequestBody();
		paymentRequestBody.setOutcome(Outcome.OK.toString());
		List<String> tokens = new ArrayList<>();
		tokens.add("648fhg36s95jfg7DS");
		paymentRequestBody.setPaymentTokens(tokens);
		paymentRequestBody.setPaymentMethod("PAGOBANCOMAT");
		paymentRequestBody.setTransactionId("517a4216840E461fB011036A0fd134E1");
		paymentRequestBody.setTotalAmount(234234);
		paymentRequestBody.setFee(897);
		paymentRequestBody.setTimestampOp("2022-11-12T08:53:55");

		Mockito
		.when(pnRepository.findByIdOptional(Mockito.any(String.class)))
		.thenReturn(Uni.createFrom().item(Optional.of(pnEntity)));
		
		ClosePaymentResponse closePayentResponse = new ClosePaymentResponse();
		closePayentResponse.setOutcome(Outcome.OK.toString());
		
		Mockito.when(nodoService.closePayment(Mockito.any(ClosePaymentRequest.class)))
		.thenReturn(Uni.createFrom().failure(new InternalServerErrorException()));
		
		
		Response response = given()
				.contentType(ContentType.JSON)
				.headers(
						"RequestId", "d0d654e6-97da-4848-b568-99fedccb642b",
						"Version", API_VERSION,
						"AcquirerId", "4585625",
						"Channel", "ATM",
						"TerminalId", "0aB9wXyZ",
						"SessionId", SESSION_ID)
				.and()
				.body(paymentRequestBody)
				.when()
				.post("/payment")
				.then()
				.extract()
				.response();

	        Assertions.assertEquals(500, response.statusCode());
	     
	}
	
}

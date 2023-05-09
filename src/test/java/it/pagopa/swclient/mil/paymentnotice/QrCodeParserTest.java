/**
 *
 */
package it.pagopa.swclient.mil.paymentnotice;

import it.pagopa.swclient.mil.paymentnotice.util.PaymentTestData;
import it.pagopa.swclient.mil.paymentnotice.utils.QrCodeParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.swclient.mil.paymentnotice.bean.QrCode;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QrCodeParserTest {

	@Inject
	QrCodeParser qrCodeParser;

	@Test
	void testParse() {

		QrCode parsedQrCode = qrCodeParser.parse(PaymentTestData.QR_CODE);

		Assertions.assertEquals("PAGOPA", parsedQrCode.getIdCode());
		Assertions.assertEquals("002", parsedQrCode.getVersion());
		Assertions.assertEquals(PaymentTestData.NOTICE_NUMBER, parsedQrCode.getNoticeNumber());
		Assertions.assertEquals(PaymentTestData.PA_TAX_CODE, parsedQrCode.getPaTaxCode());
		Assertions.assertEquals("9999", parsedQrCode.getAmount());

	}

	@Test
	void testB64UrlParse_OK_withPadding() {

		byte[] bytes = Base64.getUrlEncoder().encode(PaymentTestData.QR_CODE.getBytes(StandardCharsets.UTF_8));
		QrCode parsedQrCode = qrCodeParser.b64UrlParse(new String(bytes, StandardCharsets.UTF_8));

		Assertions.assertEquals("PAGOPA", parsedQrCode.getIdCode());
		Assertions.assertEquals("002", parsedQrCode.getVersion());
		Assertions.assertEquals(PaymentTestData.NOTICE_NUMBER, parsedQrCode.getNoticeNumber());
		Assertions.assertEquals(PaymentTestData.PA_TAX_CODE, parsedQrCode.getPaTaxCode());
		Assertions.assertEquals("9999", parsedQrCode.getAmount());

	}

	@Test
	void testB64UrlParse_OK_withoutPadding() {

		byte[] bytes = Base64.getUrlEncoder().withoutPadding().encode(PaymentTestData.QR_CODE.getBytes(StandardCharsets.UTF_8));
		QrCode parsedQrCode = qrCodeParser.b64UrlParse(new String(bytes, StandardCharsets.UTF_8));

		Assertions.assertEquals("PAGOPA", parsedQrCode.getIdCode());
		Assertions.assertEquals("002", parsedQrCode.getVersion());
		Assertions.assertEquals(PaymentTestData.NOTICE_NUMBER, parsedQrCode.getNoticeNumber());
		Assertions.assertEquals(PaymentTestData.PA_TAX_CODE, parsedQrCode.getPaTaxCode());
		Assertions.assertEquals("9999", parsedQrCode.getAmount());

	}

	@Test
	void testB64UrlParse_KO() {

		String encodedQrCode = Base64.getUrlEncoder().encodeToString("https://www.test.com".getBytes(StandardCharsets.UTF_8));
		Assertions.assertThrows(ConstraintViolationException.class, () -> qrCodeParser.b64UrlParse(encodedQrCode));

	}
}

/**
 * 
 */
package it.gov.pagopa.swclient.mil.paymentnotice.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.QrCode;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QrCodeTest {

	@Test
	void qrCode_200_OK() {
		final String qrCode = "PAGOPA|002|100000000000000001|20000000002|9999"; 
		QrCode parsedQrCode = QrCode.parse(qrCode);
		
		Assertions.assertNotNull(parsedQrCode.getIdCode());
		Assertions.assertNotNull(parsedQrCode.getVersion());
		Assertions.assertNotNull(parsedQrCode.getAmount());
		Assertions.assertNotNull(parsedQrCode.toString());
	}
}

/**
 * 
 */
package it.gov.pagopa.swclient.mil.paymentnotice.util;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.swclient.mil.paymentnotice.utils.StAmountFormatter;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StAmountFormatterTest {

	@Test
	void parseDecimal_200_OK() {
		Assertions.assertEquals(new BigDecimal(1000), StAmountFormatter.parseBigDecimal("1000"));
		
		Assertions.assertEquals("100.00",StAmountFormatter.printBigDecimal(new BigDecimal("100"))); 
	}
}

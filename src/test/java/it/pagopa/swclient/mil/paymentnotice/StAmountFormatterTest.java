/**
 * 
 */
package it.pagopa.swclient.mil.paymentnotice;

import java.math.BigDecimal;

import it.pagopa.swclient.mil.paymentnotice.utils.StAmountFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StAmountFormatterTest {

	@Test
	void parseDecimal() {
		Assertions.assertEquals(new BigDecimal(1000), StAmountFormatter.parseBigDecimal("1000"));
	}

	@Test
	void printDecimal() {
		Assertions.assertEquals("100.00",StAmountFormatter.printBigDecimal(new BigDecimal("100")));
	}
}

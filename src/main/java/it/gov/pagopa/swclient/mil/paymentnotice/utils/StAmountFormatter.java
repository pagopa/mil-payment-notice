package it.gov.pagopa.swclient.mil.paymentnotice.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class StAmountFormatter {

    private StAmountFormatter() {

    }

    public static String printBigDecimal(BigDecimal value) {

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');

        DecimalFormat df = new DecimalFormat("0.00");
        df.setDecimalFormatSymbols(symbols);
        df.setGroupingUsed(false);

        return df.format(value);
    }

    public static BigDecimal parseBigDecimal(String value) {
        return new BigDecimal(value);
    }

}

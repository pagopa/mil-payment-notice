
package it.gov.pagopa.pagopa_api.xsd.common_types.v1_0;

import java.math.BigDecimal;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class Adapter1
    extends XmlAdapter<String, BigDecimal>
{


    public BigDecimal unmarshal(String value) {
        return (it.gov.pagopa.swclient.mil.paymentnotice.utils.StAmountFormatter.parseBigDecimal(value));
    }

    public String marshal(BigDecimal value) {
        return (it.gov.pagopa.swclient.mil.paymentnotice.utils.StAmountFormatter.printBigDecimal(value));
    }

}

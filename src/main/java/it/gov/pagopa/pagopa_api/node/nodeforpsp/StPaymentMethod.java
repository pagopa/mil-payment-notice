
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for stPaymentMethod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="stPaymentMethod"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="cash"/&gt;
 *     &lt;enumeration value="creditCard"/&gt;
 *     &lt;enumeration value="bancomat"/&gt;
 *     &lt;enumeration value="other"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "stPaymentMethod")
@XmlEnum
public enum StPaymentMethod {

    @XmlEnumValue("cash")
    CASH("cash"),
    @XmlEnumValue("creditCard")
    CREDIT_CARD("creditCard"),
    @XmlEnumValue("bancomat")
    BANCOMAT("bancomat"),
    @XmlEnumValue("other")
    OTHER("other");
    private final String value;

    StPaymentMethod(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static StPaymentMethod fromValue(String v) {
        for (StPaymentMethod c: StPaymentMethod.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

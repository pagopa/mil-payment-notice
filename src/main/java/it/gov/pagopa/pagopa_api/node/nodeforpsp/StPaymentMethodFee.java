
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for stPaymentMethodFee.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="stPaymentMethodFee"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="BBT"/&gt;
 *     &lt;enumeration value="BP"/&gt;
 *     &lt;enumeration value="AD"/&gt;
 *     &lt;enumeration value="CP"/&gt;
 *     &lt;enumeration value="PO"/&gt;
 *     &lt;enumeration value="OBEP"/&gt;
 *     &lt;enumeration value="JIF"/&gt;
 *     &lt;enumeration value="MYBK"/&gt;
 *     &lt;enumeration value="PPAL"/&gt;
 *     &lt;maxLength value="4"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "stPaymentMethodFee")
@XmlEnum
public enum StPaymentMethodFee {

    BBT,
    BP,
    AD,
    CP,
    PO,
    OBEP,
    JIF,
    MYBK,
    PPAL;

    public String value() {
        return name();
    }

    public static StPaymentMethodFee fromValue(String v) {
        return valueOf(v);
    }

}

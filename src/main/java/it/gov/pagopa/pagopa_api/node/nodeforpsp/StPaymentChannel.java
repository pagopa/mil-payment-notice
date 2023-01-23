
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for stPaymentChannel.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="stPaymentChannel"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="frontOffice"/&gt;
 *     &lt;enumeration value="atm"/&gt;
 *     &lt;enumeration value="onLine"/&gt;
 *     &lt;enumeration value="app"/&gt;
 *     &lt;enumeration value="other"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "stPaymentChannel")
@XmlEnum
public enum StPaymentChannel {

    @XmlEnumValue("frontOffice")
    FRONT_OFFICE("frontOffice"),
    @XmlEnumValue("atm")
    ATM("atm"),
    @XmlEnumValue("onLine")
    ON_LINE("onLine"),
    @XmlEnumValue("app")
    APP("app"),
    @XmlEnumValue("other")
    OTHER("other");
    private final String value;

    StPaymentChannel(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static StPaymentChannel fromValue(String v) {
        for (StPaymentChannel c: StPaymentChannel.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

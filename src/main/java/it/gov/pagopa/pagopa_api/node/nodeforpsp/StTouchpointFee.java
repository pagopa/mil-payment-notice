
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for stTouchpointFee.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="stTouchpointFee"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="IO"/&gt;
 *     &lt;enumeration value="WISP"/&gt;
 *     &lt;enumeration value="CHECKOUT"/&gt;
 *     &lt;enumeration value="PSP"/&gt;
 *     &lt;enumeration value="ATM"/&gt;
 *     &lt;enumeration value="BETTING"/&gt;
 *     &lt;enumeration value="TS"/&gt;
 *     &lt;enumeration value="HB"/&gt;
 *     &lt;maxLength value="10"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "stTouchpointFee")
@XmlEnum
public enum StTouchpointFee {

    IO,
    WISP,
    CHECKOUT,
    PSP,
    ATM,
    BETTING,
    TS,
    HB;

    public String value() {
        return name();
    }

    public static StTouchpointFee fromValue(String v) {
        return valueOf(v);
    }

}

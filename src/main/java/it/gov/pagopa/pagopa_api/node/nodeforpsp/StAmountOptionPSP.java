
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for stAmountOptionPSP.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="stAmountOptionPSP"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="EQ"/&gt;
 *     &lt;enumeration value="LS"/&gt;
 *     &lt;enumeration value="GT"/&gt;
 *     &lt;enumeration value="ANY"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "stAmountOptionPSP")
@XmlEnum
public enum StAmountOptionPSP {

    EQ,
    LS,
    GT,
    ANY;

    public String value() {
        return name();
    }

    public static StAmountOptionPSP fromValue(String v) {
        return valueOf(v);
    }

}


package it.gov.pagopa.pagopa_api.xsd.common_types.v1_0;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for stOutcome.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="stOutcome"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="OK"/&gt;
 *     &lt;enumeration value="KO"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "stOutcome")
@XmlEnum
public enum StOutcome {

    OK,
    KO;

    public String value() {
        return name();
    }

    public static StOutcome fromValue(String v) {
        return valueOf(v);
    }

}


package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for stEntityUniqueIdentifierType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <pre>
 * &lt;simpleType name="stEntityUniqueIdentifierType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="F"/&gt;
 *     &lt;enumeration value="G"/&gt;
 *     &lt;length value="1"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "stEntityUniqueIdentifierType")
@XmlEnum
public enum StEntityUniqueIdentifierType {

    F,
    G;

    public String value() {
        return name();
    }

    public static StEntityUniqueIdentifierType fromValue(String v) {
        return valueOf(v);
    }

}

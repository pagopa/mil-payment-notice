
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ctEntityUniqueIdentifier complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctEntityUniqueIdentifier"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="entityUniqueIdentifierType" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stEntityUniqueIdentifierType"/&gt;
 *         &lt;element name="entityUniqueIdentifierValue" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stEntityUniqueIdentifierValue"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctEntityUniqueIdentifier", propOrder = {
    "entityUniqueIdentifierType",
    "entityUniqueIdentifierValue"
})
public class CtEntityUniqueIdentifier {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected StEntityUniqueIdentifierType entityUniqueIdentifierType;
    @XmlElement(required = true)
    protected String entityUniqueIdentifierValue;

    /**
     * Gets the value of the entityUniqueIdentifierType property.
     * 
     * @return
     *     possible object is
     *     {@link StEntityUniqueIdentifierType }
     *     
     */
    public StEntityUniqueIdentifierType getEntityUniqueIdentifierType() {
        return entityUniqueIdentifierType;
    }

    /**
     * Sets the value of the entityUniqueIdentifierType property.
     * 
     * @param value
     *     allowed object is
     *     {@link StEntityUniqueIdentifierType }
     *     
     */
    public void setEntityUniqueIdentifierType(StEntityUniqueIdentifierType value) {
        this.entityUniqueIdentifierType = value;
    }

    /**
     * Gets the value of the entityUniqueIdentifierValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEntityUniqueIdentifierValue() {
        return entityUniqueIdentifierValue;
    }

    /**
     * Sets the value of the entityUniqueIdentifierValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEntityUniqueIdentifierValue(String value) {
        this.entityUniqueIdentifierValue = value;
    }

}

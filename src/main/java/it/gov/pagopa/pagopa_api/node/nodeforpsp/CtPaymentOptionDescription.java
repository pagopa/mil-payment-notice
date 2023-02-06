
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.Adapter1;


/**
 * <p>Java class for ctPaymentOptionDescription complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctPaymentOptionDescription"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="amount" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stAmount"/&gt;
 *         &lt;element name="options" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stAmountOptionPSP"/&gt;
 *         &lt;element name="dueDate" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stISODate" minOccurs="0"/&gt;
 *         &lt;element name="paymentNote" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stText210" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctPaymentOptionDescription", propOrder = {
    "amount",
    "options",
    "dueDate",
    "paymentNote"
})
public class CtPaymentOptionDescription {

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "decimal")
    protected BigDecimal amount;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected StAmountOptionPSP options;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar dueDate;
    protected String paymentNote;

    /**
     * Gets the value of the amount property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Sets the value of the amount property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAmount(BigDecimal value) {
        this.amount = value;
    }

    /**
     * Gets the value of the options property.
     * 
     * @return
     *     possible object is
     *     {@link StAmountOptionPSP }
     *     
     */
    public StAmountOptionPSP getOptions() {
        return options;
    }

    /**
     * Sets the value of the options property.
     * 
     * @param value
     *     allowed object is
     *     {@link StAmountOptionPSP }
     *     
     */
    public void setOptions(StAmountOptionPSP value) {
        this.options = value;
    }

    /**
     * Gets the value of the dueDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDueDate() {
        return dueDate;
    }

    /**
     * Sets the value of the dueDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDueDate(XMLGregorianCalendar value) {
        this.dueDate = value;
    }

    /**
     * Gets the value of the paymentNote property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPaymentNote() {
        return paymentNote;
    }

    /**
     * Sets the value of the paymentNote property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPaymentNote(String value) {
        this.paymentNote = value;
    }

}

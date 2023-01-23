
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for ctOutcomeDetailsV2 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctOutcomeDetailsV2"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="paymentMethod" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stPaymentMethod"/&gt;
 *         &lt;element name="paymentChannel" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stPaymentChannel" minOccurs="0"/&gt;
 *         &lt;element name="fee" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stAmount"/&gt;
 *         &lt;element name="primaryCiIncurredFee" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stAmount" minOccurs="0"/&gt;
 *         &lt;element name="idBundle" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText70" minOccurs="0"/&gt;
 *         &lt;element name="idCiBundle" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText70" minOccurs="0"/&gt;
 *         &lt;element name="payer" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}ctSubject" minOccurs="0"/&gt;
 *         &lt;element name="applicationDate" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stISODate"/&gt;
 *         &lt;element name="transferDate" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stISODate"/&gt;
 *         &lt;element name="marcheDaBollo" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}ctListaMarcheDaBollo" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctOutcomeDetailsV2", propOrder = {
    "paymentMethod",
    "paymentChannel",
    "fee",
    "primaryCiIncurredFee",
    "idBundle",
    "idCiBundle",
    "payer",
    "applicationDate",
    "transferDate",
    "marcheDaBollo"
})
public class CtOutcomeDetailsV2 {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected StPaymentMethod paymentMethod;
    @XmlSchemaType(name = "string")
    protected StPaymentChannel paymentChannel;
    @XmlElement(required = true)
    protected BigDecimal fee;
    protected BigDecimal primaryCiIncurredFee;
    protected String idBundle;
    protected String idCiBundle;
    protected CtSubject payer;
    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar applicationDate;
    @XmlElement(required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar transferDate;
    protected CtListaMarcheDaBollo marcheDaBollo;

    /**
     * Gets the value of the paymentMethod property.
     * 
     * @return
     *     possible object is
     *     {@link StPaymentMethod }
     *     
     */
    public StPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * Sets the value of the paymentMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link StPaymentMethod }
     *     
     */
    public void setPaymentMethod(StPaymentMethod value) {
        this.paymentMethod = value;
    }

    /**
     * Gets the value of the paymentChannel property.
     * 
     * @return
     *     possible object is
     *     {@link StPaymentChannel }
     *     
     */
    public StPaymentChannel getPaymentChannel() {
        return paymentChannel;
    }

    /**
     * Sets the value of the paymentChannel property.
     * 
     * @param value
     *     allowed object is
     *     {@link StPaymentChannel }
     *     
     */
    public void setPaymentChannel(StPaymentChannel value) {
        this.paymentChannel = value;
    }

    /**
     * Gets the value of the fee property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getFee() {
        return fee;
    }

    /**
     * Sets the value of the fee property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setFee(BigDecimal value) {
        this.fee = value;
    }

    /**
     * Gets the value of the primaryCiIncurredFee property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getPrimaryCiIncurredFee() {
        return primaryCiIncurredFee;
    }

    /**
     * Sets the value of the primaryCiIncurredFee property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setPrimaryCiIncurredFee(BigDecimal value) {
        this.primaryCiIncurredFee = value;
    }

    /**
     * Gets the value of the idBundle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdBundle() {
        return idBundle;
    }

    /**
     * Sets the value of the idBundle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdBundle(String value) {
        this.idBundle = value;
    }

    /**
     * Gets the value of the idCiBundle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdCiBundle() {
        return idCiBundle;
    }

    /**
     * Sets the value of the idCiBundle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdCiBundle(String value) {
        this.idCiBundle = value;
    }

    /**
     * Gets the value of the payer property.
     * 
     * @return
     *     possible object is
     *     {@link CtSubject }
     *     
     */
    public CtSubject getPayer() {
        return payer;
    }

    /**
     * Sets the value of the payer property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtSubject }
     *     
     */
    public void setPayer(CtSubject value) {
        this.payer = value;
    }

    /**
     * Gets the value of the applicationDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getApplicationDate() {
        return applicationDate;
    }

    /**
     * Sets the value of the applicationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setApplicationDate(XMLGregorianCalendar value) {
        this.applicationDate = value;
    }

    /**
     * Gets the value of the transferDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTransferDate() {
        return transferDate;
    }

    /**
     * Sets the value of the transferDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTransferDate(XMLGregorianCalendar value) {
        this.transferDate = value;
    }

    /**
     * Gets the value of the marcheDaBollo property.
     * 
     * @return
     *     possible object is
     *     {@link CtListaMarcheDaBollo }
     *     
     */
    public CtListaMarcheDaBollo getMarcheDaBollo() {
        return marcheDaBollo;
    }

    /**
     * Sets the value of the marcheDaBollo property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtListaMarcheDaBollo }
     *     
     */
    public void setMarcheDaBollo(CtListaMarcheDaBollo value) {
        this.marcheDaBollo = value;
    }

}

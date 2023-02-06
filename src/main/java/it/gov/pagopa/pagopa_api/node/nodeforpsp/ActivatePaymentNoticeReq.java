
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.Adapter1;


/**
 * <p>Java class for activatePaymentNoticeReq complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="activatePaymentNoticeReq"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="idPSP" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIdPSP"/&gt;
 *         &lt;element name="idBrokerPSP" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIdBroker"/&gt;
 *         &lt;element name="idChannel" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIdChannel"/&gt;
 *         &lt;element name="password" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stPassword"/&gt;
 *         &lt;element name="idempotencyKey" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIdempotencyKey" minOccurs="0"/&gt;
 *         &lt;element name="qrCode" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}ctQrCode"/&gt;
 *         &lt;element name="expirationTime" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stExpirationTime" minOccurs="0"/&gt;
 *         &lt;element name="amount" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stAmount"/&gt;
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
@XmlType(name = "activatePaymentNoticeReq", propOrder = {
    "idPSP",
    "idBrokerPSP",
    "idChannel",
    "password",
    "idempotencyKey",
    "qrCode",
    "expirationTime",
    "amount",
    "dueDate",
    "paymentNote"
})
public class ActivatePaymentNoticeReq {

    @XmlElement(required = true)
    protected String idPSP;
    @XmlElement(required = true)
    protected String idBrokerPSP;
    @XmlElement(required = true)
    protected String idChannel;
    @XmlElement(required = true)
    protected String password;
    protected String idempotencyKey;
    @XmlElement(required = true)
    protected CtQrCode qrCode;
    protected BigInteger expirationTime;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "decimal")
    protected BigDecimal amount;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar dueDate;
    protected String paymentNote;

    /**
     * Gets the value of the idPSP property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdPSP() {
        return idPSP;
    }

    /**
     * Sets the value of the idPSP property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdPSP(String value) {
        this.idPSP = value;
    }

    /**
     * Gets the value of the idBrokerPSP property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdBrokerPSP() {
        return idBrokerPSP;
    }

    /**
     * Sets the value of the idBrokerPSP property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdBrokerPSP(String value) {
        this.idBrokerPSP = value;
    }

    /**
     * Gets the value of the idChannel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdChannel() {
        return idChannel;
    }

    /**
     * Sets the value of the idChannel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdChannel(String value) {
        this.idChannel = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Gets the value of the idempotencyKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    /**
     * Sets the value of the idempotencyKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdempotencyKey(String value) {
        this.idempotencyKey = value;
    }

    /**
     * Gets the value of the qrCode property.
     * 
     * @return
     *     possible object is
     *     {@link CtQrCode }
     *     
     */
    public CtQrCode getQrCode() {
        return qrCode;
    }

    /**
     * Sets the value of the qrCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtQrCode }
     *     
     */
    public void setQrCode(CtQrCode value) {
        this.qrCode = value;
    }

    /**
     * Gets the value of the expirationTime property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getExpirationTime() {
        return expirationTime;
    }

    /**
     * Sets the value of the expirationTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setExpirationTime(BigInteger value) {
        this.expirationTime = value;
    }

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

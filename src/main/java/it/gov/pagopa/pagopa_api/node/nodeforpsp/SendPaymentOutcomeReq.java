
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.StOutcome;


/**
 * <p>Java class for sendPaymentOutcomeReq complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sendPaymentOutcomeReq"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="idPSP" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIdPSP"/&gt;
 *         &lt;element name="idBrokerPSP" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIdBroker"/&gt;
 *         &lt;element name="idChannel" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIdChannel"/&gt;
 *         &lt;element name="password" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stPassword"/&gt;
 *         &lt;element name="idempotencyKey" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIdempotencyKey" minOccurs="0"/&gt;
 *         &lt;element name="paymentToken" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stPaymentToken"/&gt;
 *         &lt;element name="outcome" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stOutcome"/&gt;
 *         &lt;element name="details" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}ctOutcomeDetails" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sendPaymentOutcomeReq", propOrder = {
    "idPSP",
    "idBrokerPSP",
    "idChannel",
    "password",
    "idempotencyKey",
    "paymentToken",
    "outcome",
    "details"
})
public class SendPaymentOutcomeReq {

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
    protected String paymentToken;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected StOutcome outcome;
    protected CtOutcomeDetails details;

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
     * Gets the value of the paymentToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPaymentToken() {
        return paymentToken;
    }

    /**
     * Sets the value of the paymentToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPaymentToken(String value) {
        this.paymentToken = value;
    }

    /**
     * Gets the value of the outcome property.
     * 
     * @return
     *     possible object is
     *     {@link StOutcome }
     *     
     */
    public StOutcome getOutcome() {
        return outcome;
    }

    /**
     * Sets the value of the outcome property.
     * 
     * @param value
     *     allowed object is
     *     {@link StOutcome }
     *     
     */
    public void setOutcome(StOutcome value) {
        this.outcome = value;
    }

    /**
     * Gets the value of the details property.
     * 
     * @return
     *     possible object is
     *     {@link CtOutcomeDetails }
     *     
     */
    public CtOutcomeDetails getDetails() {
        return details;
    }

    /**
     * Sets the value of the details property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtOutcomeDetails }
     *     
     */
    public void setDetails(CtOutcomeDetails value) {
        this.details = value;
    }

}

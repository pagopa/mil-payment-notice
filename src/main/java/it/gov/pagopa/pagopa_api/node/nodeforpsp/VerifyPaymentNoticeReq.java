
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for verifyPaymentNoticeReq complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="verifyPaymentNoticeReq"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="idPSP" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIdPSP"/&gt;
 *         &lt;element name="idBrokerPSP" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIdBroker"/&gt;
 *         &lt;element name="idChannel" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIdChannel"/&gt;
 *         &lt;element name="password" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stPassword"/&gt;
 *         &lt;element name="qrCode" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}ctQrCode"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "verifyPaymentNoticeReq", propOrder = {
    "idPSP",
    "idBrokerPSP",
    "idChannel",
    "password",
    "qrCode"
})
public class VerifyPaymentNoticeReq {

    @XmlElement(required = true)
    protected String idPSP;
    @XmlElement(required = true)
    protected String idBrokerPSP;
    @XmlElement(required = true)
    protected String idChannel;
    @XmlElement(required = true)
    protected String password;
    @XmlElement(required = true)
    protected CtQrCode qrCode;

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

}

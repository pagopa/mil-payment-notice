
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for demandPaymentNoticeRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="demandPaymentNoticeRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="idPSP" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIdPSP"/&gt;
 *         &lt;element name="idBrokerPSP" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIdBroker"/&gt;
 *         &lt;element name="idChannel" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIdChannel"/&gt;
 *         &lt;element name="password" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stPassword"/&gt;
 *         &lt;element name="idSoggettoServizio" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIdentificativoSoggettoServizio"/&gt;
 *         &lt;element name="datiSpecificiServizio" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "demandPaymentNoticeRequest", propOrder = {
    "idPSP",
    "idBrokerPSP",
    "idChannel",
    "password",
    "idSoggettoServizio",
    "datiSpecificiServizio"
})
public class DemandPaymentNoticeRequest {

    @XmlElement(required = true)
    protected String idPSP;
    @XmlElement(required = true)
    protected String idBrokerPSP;
    @XmlElement(required = true)
    protected String idChannel;
    @XmlElement(required = true)
    protected String password;
    @XmlElement(required = true)
    protected String idSoggettoServizio;
    @XmlElement(required = true)
    protected byte[] datiSpecificiServizio;

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
     * Gets the value of the idSoggettoServizio property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdSoggettoServizio() {
        return idSoggettoServizio;
    }

    /**
     * Sets the value of the idSoggettoServizio property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdSoggettoServizio(String value) {
        this.idSoggettoServizio = value;
    }

    /**
     * Gets the value of the datiSpecificiServizio property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getDatiSpecificiServizio() {
        return datiSpecificiServizio;
    }

    /**
     * Sets the value of the datiSpecificiServizio property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setDatiSpecificiServizio(byte[] value) {
        this.datiSpecificiServizio = value;
    }

}

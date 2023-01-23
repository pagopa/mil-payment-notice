
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ctMarcaDaBollo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctMarcaDaBollo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="paymentToken" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stPaymentToken"/&gt;
 *         &lt;element name="idTransfer" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIdTransfer"/&gt;
 *         &lt;element name="MBDAttachment" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctMarcaDaBollo", propOrder = {
    "paymentToken",
    "idTransfer",
    "mbdAttachment"
})
public class CtMarcaDaBollo {

    @XmlElement(required = true)
    protected String paymentToken;
    protected int idTransfer;
    @XmlElement(name = "MBDAttachment", required = true)
    protected byte[] mbdAttachment;

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
     * Gets the value of the idTransfer property.
     * 
     */
    public int getIdTransfer() {
        return idTransfer;
    }

    /**
     * Sets the value of the idTransfer property.
     * 
     */
    public void setIdTransfer(int value) {
        this.idTransfer = value;
    }

    /**
     * Gets the value of the mbdAttachment property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getMBDAttachment() {
        return mbdAttachment;
    }

    /**
     * Sets the value of the mbdAttachment property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setMBDAttachment(byte[] value) {
        this.mbdAttachment = value;
    }

}

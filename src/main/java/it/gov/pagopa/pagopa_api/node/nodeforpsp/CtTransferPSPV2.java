
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtMetadata;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtRichiestaMarcaDaBollo;


/**
 * <p>Java class for ctTransferPSPV2 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctTransferPSPV2"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="idTransfer" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIdTransfer"/&gt;
 *         &lt;element name="transferAmount" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stAmount"/&gt;
 *         &lt;element name="fiscalCodePA" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stFiscalCodePA"/&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="IBAN" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stIBAN"/&gt;
 *           &lt;element name="richiestaMarcaDaBollo" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}ctRichiestaMarcaDaBollo"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="remittanceInformation" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText140"/&gt;
 *         &lt;element name="metadata" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}ctMetadata" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctTransferPSPV2", propOrder = {
    "idTransfer",
    "transferAmount",
    "fiscalCodePA",
    "iban",
    "richiestaMarcaDaBollo",
    "remittanceInformation",
    "metadata"
})
public class CtTransferPSPV2 {

    protected int idTransfer;
    @XmlElement(required = true)
    protected BigDecimal transferAmount;
    @XmlElement(required = true)
    protected String fiscalCodePA;
    @XmlElement(name = "IBAN")
    protected String iban;
    protected CtRichiestaMarcaDaBollo richiestaMarcaDaBollo;
    @XmlElement(required = true)
    protected String remittanceInformation;
    protected CtMetadata metadata;

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
     * Gets the value of the transferAmount property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTransferAmount() {
        return transferAmount;
    }

    /**
     * Sets the value of the transferAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTransferAmount(BigDecimal value) {
        this.transferAmount = value;
    }

    /**
     * Gets the value of the fiscalCodePA property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFiscalCodePA() {
        return fiscalCodePA;
    }

    /**
     * Sets the value of the fiscalCodePA property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFiscalCodePA(String value) {
        this.fiscalCodePA = value;
    }

    /**
     * Gets the value of the iban property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIBAN() {
        return iban;
    }

    /**
     * Sets the value of the iban property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIBAN(String value) {
        this.iban = value;
    }

    /**
     * Gets the value of the richiestaMarcaDaBollo property.
     * 
     * @return
     *     possible object is
     *     {@link CtRichiestaMarcaDaBollo }
     *     
     */
    public CtRichiestaMarcaDaBollo getRichiestaMarcaDaBollo() {
        return richiestaMarcaDaBollo;
    }

    /**
     * Sets the value of the richiestaMarcaDaBollo property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtRichiestaMarcaDaBollo }
     *     
     */
    public void setRichiestaMarcaDaBollo(CtRichiestaMarcaDaBollo value) {
        this.richiestaMarcaDaBollo = value;
    }

    /**
     * Gets the value of the remittanceInformation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemittanceInformation() {
        return remittanceInformation;
    }

    /**
     * Sets the value of the remittanceInformation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemittanceInformation(String value) {
        this.remittanceInformation = value;
    }

    /**
     * Gets the value of the metadata property.
     * 
     * @return
     *     possible object is
     *     {@link CtMetadata }
     *     
     */
    public CtMetadata getMetadata() {
        return metadata;
    }

    /**
     * Sets the value of the metadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtMetadata }
     *     
     */
    public void setMetadata(CtMetadata value) {
        this.metadata = value;
    }

}

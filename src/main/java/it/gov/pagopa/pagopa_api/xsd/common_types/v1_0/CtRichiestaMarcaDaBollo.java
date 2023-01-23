
package it.gov.pagopa.pagopa_api.xsd.common_types.v1_0;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ctRichiestaMarcaDaBollo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctRichiestaMarcaDaBollo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="hashDocumento" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stBase64Binary72"/&gt;
 *         &lt;element name="tipoBollo" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stTipoBolloDigitale"/&gt;
 *         &lt;element name="provinciaResidenza" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stNazioneProvincia"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctRichiestaMarcaDaBollo", propOrder = {
    "hashDocumento",
    "tipoBollo",
    "provinciaResidenza"
})
public class CtRichiestaMarcaDaBollo {

    @XmlElement(required = true)
    protected byte[] hashDocumento;
    @XmlElement(required = true)
    protected String tipoBollo;
    @XmlElement(required = true)
    protected String provinciaResidenza;

    /**
     * Gets the value of the hashDocumento property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getHashDocumento() {
        return hashDocumento;
    }

    /**
     * Sets the value of the hashDocumento property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setHashDocumento(byte[] value) {
        this.hashDocumento = value;
    }

    /**
     * Gets the value of the tipoBollo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTipoBollo() {
        return tipoBollo;
    }

    /**
     * Sets the value of the tipoBollo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTipoBollo(String value) {
        this.tipoBollo = value;
    }

    /**
     * Gets the value of the provinciaResidenza property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProvinciaResidenza() {
        return provinciaResidenza;
    }

    /**
     * Sets the value of the provinciaResidenza property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProvinciaResidenza(String value) {
        this.provinciaResidenza = value;
    }

}

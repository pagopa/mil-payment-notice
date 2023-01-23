
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for nodoInviaFlussoRendicontazioneRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="nodoInviaFlussoRendicontazioneRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="identificativoPSP" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText35"/&gt;
 *         &lt;element name="identificativoIntermediarioPSP" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText35"/&gt;
 *         &lt;element name="identificativoCanale" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText35"/&gt;
 *         &lt;element name="password" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stPassword"/&gt;
 *         &lt;element name="identificativoDominio" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText35"/&gt;
 *         &lt;element name="identificativoFlusso" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dataOraFlusso" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="xmlRendicontazione" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nodoInviaFlussoRendicontazioneRequest", propOrder = {
    "identificativoPSP",
    "identificativoIntermediarioPSP",
    "identificativoCanale",
    "password",
    "identificativoDominio",
    "identificativoFlusso",
    "dataOraFlusso",
    "xmlRendicontazione"
})
public class NodoInviaFlussoRendicontazioneRequest {

    @XmlElement(required = true)
    protected String identificativoPSP;
    @XmlElement(required = true)
    protected String identificativoIntermediarioPSP;
    @XmlElement(required = true)
    protected String identificativoCanale;
    @XmlElement(required = true)
    protected String password;
    @XmlElement(required = true)
    protected String identificativoDominio;
    @XmlElement(required = true)
    protected String identificativoFlusso;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dataOraFlusso;
    @XmlElement(required = true)
    protected byte[] xmlRendicontazione;

    /**
     * Gets the value of the identificativoPSP property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentificativoPSP() {
        return identificativoPSP;
    }

    /**
     * Sets the value of the identificativoPSP property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentificativoPSP(String value) {
        this.identificativoPSP = value;
    }

    /**
     * Gets the value of the identificativoIntermediarioPSP property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentificativoIntermediarioPSP() {
        return identificativoIntermediarioPSP;
    }

    /**
     * Sets the value of the identificativoIntermediarioPSP property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentificativoIntermediarioPSP(String value) {
        this.identificativoIntermediarioPSP = value;
    }

    /**
     * Gets the value of the identificativoCanale property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentificativoCanale() {
        return identificativoCanale;
    }

    /**
     * Sets the value of the identificativoCanale property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentificativoCanale(String value) {
        this.identificativoCanale = value;
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
     * Gets the value of the identificativoDominio property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentificativoDominio() {
        return identificativoDominio;
    }

    /**
     * Sets the value of the identificativoDominio property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentificativoDominio(String value) {
        this.identificativoDominio = value;
    }

    /**
     * Gets the value of the identificativoFlusso property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentificativoFlusso() {
        return identificativoFlusso;
    }

    /**
     * Sets the value of the identificativoFlusso property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentificativoFlusso(String value) {
        this.identificativoFlusso = value;
    }

    /**
     * Gets the value of the dataOraFlusso property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDataOraFlusso() {
        return dataOraFlusso;
    }

    /**
     * Sets the value of the dataOraFlusso property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDataOraFlusso(XMLGregorianCalendar value) {
        this.dataOraFlusso = value;
    }

    /**
     * Gets the value of the xmlRendicontazione property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getXmlRendicontazione() {
        return xmlRendicontazione;
    }

    /**
     * Sets the value of the xmlRendicontazione property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setXmlRendicontazione(byte[] value) {
        this.xmlRendicontazione = value;
    }

}

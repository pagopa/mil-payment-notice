
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for nodoChiediCatalogoServiziV2Request complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="nodoChiediCatalogoServiziV2Request"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="identificativoPSP" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText35"/&gt;
 *         &lt;element name="identificativoIntermediarioPSP" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText35"/&gt;
 *         &lt;element name="identificativoCanale" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText35"/&gt;
 *         &lt;element name="password" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stPassword"/&gt;
 *         &lt;element name="identificativoDominio" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText35" minOccurs="0"/&gt;
 *         &lt;element name="categoria" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText35" minOccurs="0"/&gt;
 *         &lt;element name="commissione" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stCommissione" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nodoChiediCatalogoServiziV2Request", propOrder = {
    "identificativoPSP",
    "identificativoIntermediarioPSP",
    "identificativoCanale",
    "password",
    "identificativoDominio",
    "categoria",
    "commissione"
})
public class NodoChiediCatalogoServiziV2Request {

    @XmlElement(required = true)
    protected String identificativoPSP;
    @XmlElement(required = true)
    protected String identificativoIntermediarioPSP;
    @XmlElement(required = true)
    protected String identificativoCanale;
    @XmlElement(required = true)
    protected String password;
    protected String identificativoDominio;
    protected String categoria;
    @XmlSchemaType(name = "string")
    protected StCommissione commissione;

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
     * Gets the value of the categoria property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCategoria() {
        return categoria;
    }

    /**
     * Sets the value of the categoria property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCategoria(String value) {
        this.categoria = value;
    }

    /**
     * Gets the value of the commissione property.
     * 
     * @return
     *     possible object is
     *     {@link StCommissione }
     *     
     */
    public StCommissione getCommissione() {
        return commissione;
    }

    /**
     * Sets the value of the commissione property.
     * 
     * @param value
     *     allowed object is
     *     {@link StCommissione }
     *     
     */
    public void setCommissione(StCommissione value) {
        this.commissione = value;
    }

}

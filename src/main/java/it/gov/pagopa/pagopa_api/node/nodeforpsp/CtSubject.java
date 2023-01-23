
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ctSubject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctSubject"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="uniqueIdentifier" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}ctEntityUniqueIdentifier"/&gt;
 *         &lt;element name="fullName" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText70"/&gt;
 *         &lt;element name="streetName" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText70" minOccurs="0"/&gt;
 *         &lt;element name="civicNumber" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText16" minOccurs="0"/&gt;
 *         &lt;element name="postalCode" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText16" minOccurs="0"/&gt;
 *         &lt;element name="city" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText35" minOccurs="0"/&gt;
 *         &lt;element name="stateProvinceRegion" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText35" minOccurs="0"/&gt;
 *         &lt;element name="country" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stNazioneProvincia" minOccurs="0"/&gt;
 *         &lt;element name="e-mail" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stEMail" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctSubject", propOrder = {
    "uniqueIdentifier",
    "fullName",
    "streetName",
    "civicNumber",
    "postalCode",
    "city",
    "stateProvinceRegion",
    "country",
    "eMail"
})
public class CtSubject {

    @XmlElement(required = true)
    protected CtEntityUniqueIdentifier uniqueIdentifier;
    @XmlElement(required = true)
    protected String fullName;
    protected String streetName;
    protected String civicNumber;
    protected String postalCode;
    protected String city;
    protected String stateProvinceRegion;
    protected String country;
    @XmlElement(name = "e-mail")
    protected String eMail;

    /**
     * Gets the value of the uniqueIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link CtEntityUniqueIdentifier }
     *     
     */
    public CtEntityUniqueIdentifier getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    /**
     * Sets the value of the uniqueIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtEntityUniqueIdentifier }
     *     
     */
    public void setUniqueIdentifier(CtEntityUniqueIdentifier value) {
        this.uniqueIdentifier = value;
    }

    /**
     * Gets the value of the fullName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the value of the fullName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFullName(String value) {
        this.fullName = value;
    }

    /**
     * Gets the value of the streetName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStreetName() {
        return streetName;
    }

    /**
     * Sets the value of the streetName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStreetName(String value) {
        this.streetName = value;
    }

    /**
     * Gets the value of the civicNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCivicNumber() {
        return civicNumber;
    }

    /**
     * Sets the value of the civicNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCivicNumber(String value) {
        this.civicNumber = value;
    }

    /**
     * Gets the value of the postalCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the value of the postalCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPostalCode(String value) {
        this.postalCode = value;
    }

    /**
     * Gets the value of the city property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the value of the city property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCity(String value) {
        this.city = value;
    }

    /**
     * Gets the value of the stateProvinceRegion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStateProvinceRegion() {
        return stateProvinceRegion;
    }

    /**
     * Sets the value of the stateProvinceRegion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStateProvinceRegion(String value) {
        this.stateProvinceRegion = value;
    }

    /**
     * Gets the value of the country property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the value of the country property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountry(String value) {
        this.country = value;
    }

    /**
     * Gets the value of the eMail property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEMail() {
        return eMail;
    }

    /**
     * Sets the value of the eMail property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEMail(String value) {
        this.eMail = value;
    }

}

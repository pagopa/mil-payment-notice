
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.Adapter1;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtMetadata;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtResponse;


/**
 * <p>Java class for activatePaymentNoticeV2Response complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="activatePaymentNoticeV2Response"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}ctResponse"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="totalAmount" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stAmount" minOccurs="0"/&gt;
 *         &lt;element name="paymentDescription" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText140" minOccurs="0"/&gt;
 *         &lt;element name="fiscalCodePA" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stFiscalCodePA" minOccurs="0"/&gt;
 *         &lt;element name="companyName" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText140" minOccurs="0"/&gt;
 *         &lt;element name="officeName" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText140" minOccurs="0"/&gt;
 *         &lt;element name="paymentToken" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stPaymentToken" minOccurs="0"/&gt;
 *         &lt;element name="transferList" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}ctTransferListPSPV2" minOccurs="0"/&gt;
 *         &lt;element name="metadata" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}ctMetadata" minOccurs="0"/&gt;
 *         &lt;element name="creditorReferenceId" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText35" minOccurs="0"/&gt;
 *         &lt;element name="suggestedUserFee" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stAmount" minOccurs="0"/&gt;
 *         &lt;element name="suggestedPaFee" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stAmount" minOccurs="0"/&gt;
 *         &lt;element name="suggestedIdBundle" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText70" minOccurs="0"/&gt;
 *         &lt;element name="suggestedIdCiBundle" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText70" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "activatePaymentNoticeV2Response", propOrder = {
    "totalAmount",
    "paymentDescription",
    "fiscalCodePA",
    "companyName",
    "officeName",
    "paymentToken",
    "transferList",
    "metadata",
    "creditorReferenceId",
    "suggestedUserFee",
    "suggestedPaFee",
    "suggestedIdBundle",
    "suggestedIdCiBundle"
})
public class ActivatePaymentNoticeV2Response
    extends CtResponse
{

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "decimal")
    protected BigDecimal totalAmount;
    protected String paymentDescription;
    protected String fiscalCodePA;
    protected String companyName;
    protected String officeName;
    protected String paymentToken;
    protected CtTransferListPSPV2 transferList;
    protected CtMetadata metadata;
    protected String creditorReferenceId;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "decimal")
    protected BigDecimal suggestedUserFee;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "decimal")
    protected BigDecimal suggestedPaFee;
    protected String suggestedIdBundle;
    protected String suggestedIdCiBundle;

    /**
     * Gets the value of the totalAmount property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    /**
     * Sets the value of the totalAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTotalAmount(BigDecimal value) {
        this.totalAmount = value;
    }

    /**
     * Gets the value of the paymentDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPaymentDescription() {
        return paymentDescription;
    }

    /**
     * Sets the value of the paymentDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPaymentDescription(String value) {
        this.paymentDescription = value;
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
     * Gets the value of the companyName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Sets the value of the companyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompanyName(String value) {
        this.companyName = value;
    }

    /**
     * Gets the value of the officeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOfficeName() {
        return officeName;
    }

    /**
     * Sets the value of the officeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOfficeName(String value) {
        this.officeName = value;
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
     * Gets the value of the transferList property.
     * 
     * @return
     *     possible object is
     *     {@link CtTransferListPSPV2 }
     *     
     */
    public CtTransferListPSPV2 getTransferList() {
        return transferList;
    }

    /**
     * Sets the value of the transferList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtTransferListPSPV2 }
     *     
     */
    public void setTransferList(CtTransferListPSPV2 value) {
        this.transferList = value;
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

    /**
     * Gets the value of the creditorReferenceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreditorReferenceId() {
        return creditorReferenceId;
    }

    /**
     * Sets the value of the creditorReferenceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreditorReferenceId(String value) {
        this.creditorReferenceId = value;
    }

    /**
     * Gets the value of the suggestedUserFee property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BigDecimal getSuggestedUserFee() {
        return suggestedUserFee;
    }

    /**
     * Sets the value of the suggestedUserFee property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSuggestedUserFee(BigDecimal value) {
        this.suggestedUserFee = value;
    }

    /**
     * Gets the value of the suggestedPaFee property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BigDecimal getSuggestedPaFee() {
        return suggestedPaFee;
    }

    /**
     * Sets the value of the suggestedPaFee property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSuggestedPaFee(BigDecimal value) {
        this.suggestedPaFee = value;
    }

    /**
     * Gets the value of the suggestedIdBundle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSuggestedIdBundle() {
        return suggestedIdBundle;
    }

    /**
     * Sets the value of the suggestedIdBundle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSuggestedIdBundle(String value) {
        this.suggestedIdBundle = value;
    }

    /**
     * Gets the value of the suggestedIdCiBundle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSuggestedIdCiBundle() {
        return suggestedIdCiBundle;
    }

    /**
     * Sets the value of the suggestedIdCiBundle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSuggestedIdCiBundle(String value) {
        this.suggestedIdCiBundle = value;
    }

}
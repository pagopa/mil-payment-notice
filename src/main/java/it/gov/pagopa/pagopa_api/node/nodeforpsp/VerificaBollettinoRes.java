
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtResponse;


/**
 * <p>Java class for verificaBollettinoRes complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="verificaBollettinoRes"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}ctResponse"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="paymentBollettinoList" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}ctPaymentOptionsBollettinoDescriptionList" minOccurs="0"/&gt;
 *         &lt;element name="paymentDescription" type="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}stText210" minOccurs="0"/&gt;
 *         &lt;element name="fiscalCodePA" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stFiscalCodePA" minOccurs="0"/&gt;
 *         &lt;element name="noticeNumber" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stNoticeNumber" minOccurs="0"/&gt;
 *         &lt;element name="companyName" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText140" minOccurs="0"/&gt;
 *         &lt;element name="officeName" type="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}stText140" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "verificaBollettinoRes", propOrder = {
    "paymentBollettinoList",
    "paymentDescription",
    "fiscalCodePA",
    "noticeNumber",
    "companyName",
    "officeName"
})
public class VerificaBollettinoRes
    extends CtResponse
{

    protected CtPaymentOptionsBollettinoDescriptionList paymentBollettinoList;
    protected String paymentDescription;
    protected String fiscalCodePA;
    protected String noticeNumber;
    protected String companyName;
    protected String officeName;

    /**
     * Gets the value of the paymentBollettinoList property.
     * 
     * @return
     *     possible object is
     *     {@link CtPaymentOptionsBollettinoDescriptionList }
     *     
     */
    public CtPaymentOptionsBollettinoDescriptionList getPaymentBollettinoList() {
        return paymentBollettinoList;
    }

    /**
     * Sets the value of the paymentBollettinoList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtPaymentOptionsBollettinoDescriptionList }
     *     
     */
    public void setPaymentBollettinoList(CtPaymentOptionsBollettinoDescriptionList value) {
        this.paymentBollettinoList = value;
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
     * Gets the value of the noticeNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNoticeNumber() {
        return noticeNumber;
    }

    /**
     * Sets the value of the noticeNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNoticeNumber(String value) {
        this.noticeNumber = value;
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

}

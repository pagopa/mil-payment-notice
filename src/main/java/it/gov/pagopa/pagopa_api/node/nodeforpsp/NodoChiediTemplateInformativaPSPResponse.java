
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for nodoChiediTemplateInformativaPSPResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="nodoChiediTemplateInformativaPSPResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}risposta"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="xmlTemplateInformativa" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nodoChiediTemplateInformativaPSPResponse", propOrder = {
    "xmlTemplateInformativa"
})
public class NodoChiediTemplateInformativaPSPResponse
    extends Risposta
{

    protected byte[] xmlTemplateInformativa;

    /**
     * Gets the value of the xmlTemplateInformativa property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getXmlTemplateInformativa() {
        return xmlTemplateInformativa;
    }

    /**
     * Sets the value of the xmlTemplateInformativa property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setXmlTemplateInformativa(byte[] value) {
        this.xmlTemplateInformativa = value;
    }

}


package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for nodoChiediInformativaPAResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="nodoChiediInformativaPAResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://pagopa-api.pagopa.gov.it/node/nodeForPsp.xsd}risposta"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="xmlInformativa" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nodoChiediInformativaPAResponse", propOrder = {
    "xmlInformativa"
})
public class NodoChiediInformativaPAResponse
    extends Risposta
{

    protected byte[] xmlInformativa;

    /**
     * Gets the value of the xmlInformativa property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getXmlInformativa() {
        return xmlInformativa;
    }

    /**
     * Sets the value of the xmlInformativa property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setXmlInformativa(byte[] value) {
        this.xmlInformativa = value;
    }

}

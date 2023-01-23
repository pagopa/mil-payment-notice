
package it.gov.pagopa.pagopa_api.node.nodeforpsp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import it.gov.pagopa.pagopa_api.xsd.common_types.v1_0.CtResponse;


/**
 * <p>Java class for nodoChiediCatalogoServiziV2Response complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="nodoChiediCatalogoServiziV2Response"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://pagopa-api.pagopa.gov.it/xsd/common-types/v1.0.0/}ctResponse"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="xmlCatalogoServizi" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nodoChiediCatalogoServiziV2Response", propOrder = {
    "xmlCatalogoServizi"
})
public class NodoChiediCatalogoServiziV2Response
    extends CtResponse
{

    protected byte[] xmlCatalogoServizi;

    /**
     * Gets the value of the xmlCatalogoServizi property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getXmlCatalogoServizi() {
        return xmlCatalogoServizi;
    }

    /**
     * Sets the value of the xmlCatalogoServizi property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setXmlCatalogoServizi(byte[] value) {
        this.xmlCatalogoServizi = value;
    }

}

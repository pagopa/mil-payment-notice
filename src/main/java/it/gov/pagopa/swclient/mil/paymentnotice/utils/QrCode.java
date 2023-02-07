package it.gov.pagopa.swclient.mil.paymentnotice.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Class representation of the QR-Code encoding the data of a payment notice
 * @see <a href="https://docs.pagopa.it/avviso-pagamento/struttura/specifiche-tecniche/dati-per-il-pagamento/codice-qr">QR Code specification</a>
 */
public class QrCode {

    /**
     * Identifier code (static value "PAGOPA")
     */
    @NotNull
    @Pattern(regexp = "^PAGOPA$")
    private String idCode;

    /**
     * Version (static value 002)
     */
    @NotNull
    @Pattern(regexp = "^002$")
    private String version;

    /**
     * Notice number. Concatenation of AUX digit, application Code and IUV
     * @see <a href=https://docs.pagopa.it/saci/specifiche-attuative-dei-codici-identificativi-di-versamento-riversamento-e-rendicontazione/generazione-dellidentificativo-univoco-di-versamento#schema-a>
     *     Notice number specification, A scheme</a>
     */
    @NotNull
    @Size(min = 18, max = 18)
    @Pattern(regexp = "^\\d{18}$")
    private String noticeNumber;

    /**
     * Tax Code of the creditor
     */
    @NotNull
    @Size(min = 11, max = 11)
    @Pattern(regexp = "^\\d{11}$")
    private String paTaxCode;

    /**
     * Amount, in euro cents
     */
    @NotNull
    @Size(min = 2, max = 11)
    @Pattern(regexp = "^\\d{2,11}$")
    private String amount;

    private QrCode() {
    }

    /**
     * Creates a QrCode instance from its string representation
     * @param qrCode the string representation of the QR code
     * @return the {@link QrCode} instance
     */
    @Valid
    public static QrCode parse(String qrCode) {
        QrCode instance = new QrCode();
        String[] parts = StringUtils.split(qrCode, "|");
        instance.setIdCode(ArrayUtils.get(parts, 0, null));
        instance.setVersion(ArrayUtils.get(parts, 1, null));
        instance.setNoticeNumber(ArrayUtils.get(parts, 2, null));
        instance.setPaTaxCode(ArrayUtils.get(parts, 3, null));
        instance.setAmount(ArrayUtils.get(parts, 4, null));
        return instance;
    }


    /**
     * Gets idCode
     * @return value of idCode
     */
    public String getIdCode() {
        return idCode;
    }

    /**
     * Sets idCode
     * @param idCode value of idCode
     */
    public void setIdCode(String idCode) {
        this.idCode = idCode;
    }

    /**
     * Gets version
     * @return value of version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets version
     * @param version value of version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets noticeNumber
     * @return value of noticeNumber
     */
    public String getNoticeNumber() {
        return noticeNumber;
    }

    /**
     * Sets noticeNumber
     * @param noticeNumber value of noticeNumber
     */
    public void setNoticeNumber(String noticeNumber) {
        this.noticeNumber = noticeNumber;
    }

    /**
     * Gets paTaxCode
     * @return value of paTaxCode
     */
    public String getPaTaxCode() {
        return paTaxCode;
    }

    /**
     * Sets paTaxCode
     * @param paTaxCode value of paTaxCode
     */
    public void setPaTaxCode(String paTaxCode) {
        this.paTaxCode = paTaxCode;
    }

    /**
     * Gets amount
     * @return value of amount
     */
    public String getAmount() {
        return amount;
    }

    /**
     * Sets amount
     * @param amount value of amount
     */
    public void setAmount(String amount) {
        this.amount = amount;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QrCode{");
        sb.append("idCode='").append(idCode).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", noticeNumber='").append(noticeNumber).append('\'');
        sb.append(", paTaxCode='").append(paTaxCode).append('\'');
        sb.append(", amount='").append(amount).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

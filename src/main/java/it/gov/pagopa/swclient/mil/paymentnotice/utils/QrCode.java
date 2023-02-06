package it.gov.pagopa.swclient.mil.paymentnotice.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class QrCode {

    @NotNull
    @Pattern(regexp = "^PAGOPA$")
    private String idCode;

    @NotNull
    @Pattern(regexp = "^002$")
    private String version;

    @NotNull
    @Size(min = 8, max = 18)
    @Pattern(regexp = "^\\d{8,18}$")
    private String noticeNumber;

    @NotNull
    @Size(min = 11, max = 11)
    @Pattern(regexp = "^\\d{11}$")
    private String paTaxCode;

    @NotNull
    @Size(min = 2, max = 10)
    @Pattern(regexp = "^\\d{2,10}$")
    private String amount;

    private QrCode() {
    }

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

    public String getIdCode() {
        return idCode;
    }

    public void setIdCode(String idCode) {
        this.idCode = idCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNoticeNumber() {
        return noticeNumber;
    }

    public void setNoticeNumber(String noticeNumber) {
        this.noticeNumber = noticeNumber;
    }

    public String getPaTaxCode() {
        return paTaxCode;
    }

    public void setPaTaxCode(String paTaxCode) {
        this.paTaxCode = paTaxCode;
    }

    public String getAmount() {
        return amount;
    }

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

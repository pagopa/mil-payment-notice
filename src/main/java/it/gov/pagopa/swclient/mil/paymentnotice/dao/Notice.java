package it.gov.pagopa.swclient.mil.paymentnotice.dao;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Entity bean the data of a payment notice as returned by the activatePayment API of the node
 */
@RegisterForReflection
public class Notice {

    /**
     * The payment token returned by the node
     */
    private String paymentToken;

    /**
     *
     */
    private String paTaxCode;

    /**
     *
     */
    private String noticeNumber;

    /**
     *
     */
    private Long amount;

    /**
     *
     */
    private String description;

    /**
     *
     */
    private String company;

    /**
     *
     */
    private String office;

    /**
     *
     */
    private String creditorReferenceId;

    /**
     *
     */
    private String debtor;

    /**
     * Gets paymentToken
     * @return value of paymentToken
     */
    public String getPaymentToken() {
        return paymentToken;
    }

    /**
     * Sets paymentToken
     * @param paymentToken value of paymentToken
     */
    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
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
     * Gets amount
     * @return value of amount
     */
    public Long getAmount() {
        return amount;
    }

    /**
     * Sets amount
     * @param amount value of amount
     */
    public void setAmount(Long amount) {
        this.amount = amount;
    }

    /**
     * Gets description
     * @return value of description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description
     * @param description value of description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets company
     * @return value of company
     */
    public String getCompany() {
        return company;
    }

    /**
     * Sets company
     * @param company value of company
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * Gets office
     * @return value of office
     */
    public String getOffice() {
        return office;
    }

    /**
     * Sets office
     * @param office value of office
     */
    public void setOffice(String office) {
        this.office = office;
    }

    /**
     * Gets creditorReferenceId
     * @return value of creditorReferenceId
     */
    public String getCreditorReferenceId() {
        return creditorReferenceId;
    }

    /**
     * Sets creditorReferenceId
     * @param creditorReferenceId value of creditorReferenceId
     */
    public void setCreditorReferenceId(String creditorReferenceId) {
        this.creditorReferenceId = creditorReferenceId;
    }

    /**
     * Gets debtor
     * @return value of debtor
     */
    public String getDebtor() {
        return debtor;
    }

    /**
     * Sets debtor
     * @param debtor value of debtor
     */
    public void setDebtor(String debtor) {
        this.debtor = debtor;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Notice{");
        sb.append("paymentToken='").append(paymentToken).append('\'');
        sb.append(", paTaxCode='").append(paTaxCode).append('\'');
        sb.append(", noticeNumber='").append(noticeNumber).append('\'');
        sb.append(", amount=").append(amount);
        sb.append(", description='").append(description).append('\'');
        sb.append(", company='").append(company).append('\'');
        sb.append(", office='").append(office).append('\'');
        sb.append(", creditorReferenceId='").append(creditorReferenceId).append('\'');
        sb.append(", debtor='").append(debtor).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

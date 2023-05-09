package it.pagopa.swclient.mil.paymentnotice.bean;

import io.quarkus.runtime.annotations.RegisterForReflection;
import it.pagopa.swclient.mil.paymentnotice.dao.PaymentTransaction;

import java.util.List;

/**
 * Response of the getPayments API
 */
@RegisterForReflection
public class GetPaymentsResponse {

    /**
     * List of payment transactions executed by the terminal
     */
    List<PaymentTransaction> transactions;

    /**
     * Gets transactions
     * @return value of transactions
     */
    public List<PaymentTransaction> getTransactions() {
        return transactions;
    }

    /**
     * Sets transactions
     * @param transactions value of transactions
     */
    public void setTransactions(List<PaymentTransaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GetPaymentsResponse{");
        sb.append("transactions=").append(transactions);
        sb.append('}');
        return sb.toString();
    }
}

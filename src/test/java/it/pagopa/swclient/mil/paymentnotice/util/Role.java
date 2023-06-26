package it.pagopa.swclient.mil.paymentnotice.util;

public enum Role {

    NODO("Nodo"),
    NOTICE_PAYER("NoticePayer"),
    INSTITUTION_PORTAL("InstitutionPortal"),
    SERVICE_LIST_REQUESTER("ServiceListRequester"),
    SLAVE_POS("SlavePos");

    public final String label;

    Role(String label) {
        this.label = label;
    }

}


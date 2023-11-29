package it.pagopa.swclient.mil.paymentnotice.client.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ADAccessToken {
    @JsonProperty("token_type")
    private String type;

    @JsonProperty("expires_on")
    private long expiresOn;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("resource")
    private String resource;

    @JsonProperty("access_token")
    private String token;

    public void setType(String type) {
        this.type = type;
    }

    public void setExpiresOn(long expiresOn) {
        this.expiresOn = expiresOn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
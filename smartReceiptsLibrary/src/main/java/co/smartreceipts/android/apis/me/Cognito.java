package co.smartreceipts.android.apis.me;

import android.support.annotation.Nullable;

import java.io.Serializable;

public class Cognito implements Serializable {

    private final String cognito_token;
    private final long cognito_token_expires_at;
    private final String identity_id;

    public Cognito(@Nullable String cognito_token, long cognito_token_expires_at, @Nullable String identity_id) {
        this.cognito_token = cognito_token;
        this.cognito_token_expires_at = cognito_token_expires_at;
        this.identity_id = identity_id;
    }

    @Nullable
    public String getCognitoToken() {
        return cognito_token;
    }

    public long getCognitoTokenExpiresAt() {
        return cognito_token_expires_at;
    }

    @Nullable
    public String getIdentityId() {
        return this.identity_id;
    }
}

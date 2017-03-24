package co.smartreceipts.android.identity.apis.me;

import android.support.annotation.Nullable;

import java.io.Serializable;

public class Cognito implements Serializable {

    private final String cognito_token;
    private final String identity_id;
    private final long cognito_token_expires_at;

    public Cognito(@Nullable String cognito_token, @Nullable String identity_id, long cognito_token_expires_at) {
        this.cognito_token = cognito_token;
        this.identity_id = identity_id;
        this.cognito_token_expires_at = cognito_token_expires_at;
    }

    @Nullable
    public String getCognitoToken() {
        return cognito_token;
    }

    @Nullable
    public String getIdentityId() {
        return this.identity_id;
    }

    public long getCognitoTokenExpiresAt() {
        return cognito_token_expires_at;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cognito)) return false;

        Cognito cognito = (Cognito) o;

        if (cognito_token_expires_at != cognito.cognito_token_expires_at) return false;
        if (cognito_token != null ? !cognito_token.equals(cognito.cognito_token) : cognito.cognito_token != null)
            return false;
        return identity_id != null ? identity_id.equals(cognito.identity_id) : cognito.identity_id == null;

    }

    @Override
    public int hashCode() {
        int result = cognito_token != null ? cognito_token.hashCode() : 0;
        result = 31 * result + (identity_id != null ? identity_id.hashCode() : 0);
        result = 31 * result + (int) (cognito_token_expires_at ^ (cognito_token_expires_at >>> 32));
        return result;
    }
}

package co.smartreceipts.android.apis.me;

import android.support.annotation.Nullable;

public class User {

    private final String id;
    private final String email;
    private final long created_at;
    private final String name;
    private final String display_name;
    private final String cognito_token;
    private final long cognito_token_expires_at;
    private final String identity_id;

    public User(@Nullable String id, @Nullable String email, long created_at, @Nullable String name, @Nullable String display_name,
                @Nullable String cognito_token, @Nullable long cognito_token_expires_at, @Nullable String identity_id) {
        this.id = id;
        this.email = email;
        this.created_at = created_at;
        this.name = name;
        this.display_name = display_name;
        this.cognito_token = cognito_token;
        this.cognito_token_expires_at = cognito_token_expires_at;
        this.identity_id = identity_id;
    }

    @Nullable
    public String getCognitoToken() {
        return cognito_token;
    }

    @Nullable
    public long getCognitoTokenExpiresAt() {
        return cognito_token_expires_at;
    }
}

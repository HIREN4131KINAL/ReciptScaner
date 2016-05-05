package co.smartreceipts.android.apis.me;

import android.support.annotation.Nullable;

public class User {

    private final String id;
    private final String email;
    private final long created_at;
    private final String name;
    private final String display_name;
    private final Cognito cognito;

    public User(@Nullable String id, @Nullable String email, long created_at, @Nullable String name, @Nullable String display_name, @Nullable Cognito cognito) {
        this.id = id;
        this.email = email;
        this.created_at = created_at;
        this.name = name;
        this.display_name = display_name;
        this.cognito = cognito;
    }

    @Nullable
    public Cognito getCognito() {
        return this.cognito;
    }

}

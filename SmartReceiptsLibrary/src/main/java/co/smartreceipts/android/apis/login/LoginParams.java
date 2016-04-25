package co.smartreceipts.android.apis.login;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

public abstract class LoginParams implements Serializable {

    private final String type;
    private final String email;
    private final String password;
    private final String token;

    public LoginParams(@NonNull String type, @Nullable String email, @Nullable String password, @Nullable String token) {
        this.type = type;
        this.email = email;
        this.password = password;
        this.token = token;
    }

    @Nullable
    public String getType() {
        return type;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    @Nullable
    public String getToken() {
        return token;
    }
}

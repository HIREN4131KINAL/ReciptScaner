package co.smartreceipts.android.apis.login;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

public class LoginResponse implements Serializable {

    private final String token;

    public LoginResponse(@NonNull String token) {
        this.token = token;
    }

    @Nullable
    public String getToken() {
        return token;
    }
}

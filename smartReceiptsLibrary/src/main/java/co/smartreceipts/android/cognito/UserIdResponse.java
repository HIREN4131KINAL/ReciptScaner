package co.smartreceipts.android.cognito;

import android.support.annotation.Nullable;

import java.io.Serializable;

public class UserIdResponse implements Serializable {

    private final String status;
    private final String token;

    public UserIdResponse(@Nullable String status, @Nullable String token) {
        this.status = status;
        this.token = token;
    }

    @Nullable
    public String getStatus() {
        return status;
    }

    @Nullable
    public String getToken() {
        return token;
    }
}

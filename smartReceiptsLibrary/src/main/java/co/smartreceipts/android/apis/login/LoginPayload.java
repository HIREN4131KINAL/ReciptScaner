package co.smartreceipts.android.apis.login;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LoginPayload implements Serializable {

    @SerializedName("login_params")
    private final LoginParams loginParams;

    public LoginPayload(@NonNull LoginParams loginParams) {
        this.loginParams = loginParams;
    }

    @Nullable
    public LoginParams getLoginParams() {
        return loginParams;
    }
}

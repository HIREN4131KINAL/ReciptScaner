package co.smartreceipts.android.identity.apis.signup;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import co.smartreceipts.android.identity.apis.login.UserCredentialsPayload;

public class SignUpPayload implements Serializable {

    @SerializedName("signup_params")
    private final UserCredentialsPayload signUpParams;

    public SignUpPayload(@NonNull UserCredentialsPayload signUpParams) {
        this.signUpParams = signUpParams;
    }

    @Nullable
    public UserCredentialsPayload getSignUpParams() {
        return signUpParams;
    }
}
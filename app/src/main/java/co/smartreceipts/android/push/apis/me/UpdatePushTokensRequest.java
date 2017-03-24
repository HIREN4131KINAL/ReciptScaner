package co.smartreceipts.android.push.apis.me;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class UpdatePushTokensRequest {

    @SerializedName("user")
    private UpdateUserPushTokens user;

    public UpdatePushTokensRequest(@NonNull UpdateUserPushTokens user) {
        this.user = user;
    }

    @Nullable
    public UpdateUserPushTokens getUser() {
        return user;
    }
}

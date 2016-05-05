package co.smartreceipts.android.apis.me;

import android.support.annotation.Nullable;

public class MeResponse {

    private final User user;

    public MeResponse(@Nullable User user) {
        this.user = user;
    }

    @Nullable
    public User getUser() {
        return user;
    }

}

package co.smartreceipts.android.apis.me;

import android.support.annotation.Nullable;

public class UserResponse {

    private final User user;

    public UserResponse(@Nullable User user) {
        this.user = user;
    }

    @Nullable
    public User getUser() {
        return user;
    }

}

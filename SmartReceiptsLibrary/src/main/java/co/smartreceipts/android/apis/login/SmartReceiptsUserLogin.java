package co.smartreceipts.android.apis.login;

import android.support.annotation.NonNull;

public class SmartReceiptsUserLogin extends LoginParams {

    public SmartReceiptsUserLogin(@NonNull String email, @NonNull String password) {
        super("login", email, password, null);
    }
}

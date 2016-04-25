package co.smartreceipts.android.identity;

import android.support.annotation.NonNull;

public class EmailAddress extends PIIString {

    public EmailAddress(@NonNull String email) {
        super(email);
    }
}

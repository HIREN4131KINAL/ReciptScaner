package co.smartreceipts.android.identity;

import android.support.annotation.NonNull;

import co.smartreceipts.android.identity.pii.PIIString;

public class EmailAddress extends PIIString {

    public EmailAddress(@NonNull String email) {
        super(email);
    }
}

package co.smartreceipts.android.identity;

import android.support.annotation.NonNull;

import co.smartreceipts.android.identity.pii.PIIString;

public class Token extends PIIString {

    public Token(@NonNull String token) {
        super(token);
    }
}

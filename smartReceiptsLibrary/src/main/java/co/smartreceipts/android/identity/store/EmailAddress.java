package co.smartreceipts.android.identity.store;

import android.support.annotation.NonNull;

import co.smartreceipts.android.sync.model.impl.Identifier;

public class EmailAddress extends Identifier {

    public EmailAddress(@NonNull String email) {
        super(email);
    }
}

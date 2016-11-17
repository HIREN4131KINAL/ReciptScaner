package co.smartreceipts.android.identity;

import android.support.annotation.NonNull;

import co.smartreceipts.android.sync.model.impl.Identifier;

public class Token extends Identifier {

    public Token(@NonNull String token) {
        super(token);
    }
}

package co.smartreceipts.android.cognito;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.smartreceipts.android.apis.me.User;
import co.smartreceipts.android.identity.pii.PIIString;

public class CognitoToken extends PIIString {

    private final long mExpirationTimestamp;

    public CognitoToken(@NonNull User user) {
        super(user.getCognitoToken());
        mExpirationTimestamp = user.getCognitoTokenExpiresAt();
    }

    public CognitoToken(@Nullable String token, long expirationTimestamp) {
        super(token);
        mExpirationTimestamp = expirationTimestamp;
    }

    public long getExpirationTimestamp() {
        return mExpirationTimestamp;
    }

    public boolean isExpired() {
        return get() == null || mExpirationTimestamp < System.currentTimeMillis();
    }

}

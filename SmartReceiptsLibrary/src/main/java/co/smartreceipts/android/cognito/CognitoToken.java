package co.smartreceipts.android.cognito;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.smartreceipts.android.apis.me.User;
import co.smartreceipts.android.identity.pii.PIIString;

public class CognitoToken extends PIIString {

    private final long mExpirationTimestamp;
    private final String mIdentityId;

    public CognitoToken(@NonNull User user) {
        super(user.getCognitoToken());
        mExpirationTimestamp = user.getCognitoTokenExpiresAt();
        mIdentityId = user.getIdentityId();
    }

    public CognitoToken(@Nullable String token, @Nullable String identityId, long expirationTimestamp) {
        super(token);
        mIdentityId = identityId;
        mExpirationTimestamp = expirationTimestamp;
    }

    @Nullable
    public String getIdentityId() {
        return mIdentityId;
    }

    public long getExpirationTimestamp() {
        return mExpirationTimestamp;
    }

    public boolean isExpired() {
        return get() == null || mIdentityId == null || mExpirationTimestamp < System.currentTimeMillis();
    }

}

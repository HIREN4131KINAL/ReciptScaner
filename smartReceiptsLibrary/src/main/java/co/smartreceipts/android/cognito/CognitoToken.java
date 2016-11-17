package co.smartreceipts.android.cognito;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.smartreceipts.android.apis.me.Cognito;

public class CognitoToken {

    private final String mCognitoToken;
    private final long mExpirationTimestamp;
    private final String mIdentityId;

    public CognitoToken(@NonNull Cognito cognito) {
        this(cognito.getCognitoToken(), cognito.getIdentityId(), cognito.getCognitoTokenExpiresAt());
    }

    public CognitoToken(@Nullable String cognitoToken, @Nullable String identityId, long expirationTimestamp) {
        mCognitoToken = cognitoToken;
        mIdentityId = identityId;
        mExpirationTimestamp = expirationTimestamp;
    }

    @Nullable
    public String getCognitoToken() {
        return mCognitoToken;
    }

    @Nullable
    public String getIdentityId() {
        return mIdentityId;
    }

    public long getExpirationTimestamp() {
        return mExpirationTimestamp;
    }

    public boolean isExpired() {
        return mCognitoToken == null || mIdentityId == null || mExpirationTimestamp < System.currentTimeMillis();
    }

}

package co.smartreceipts.android.cognito;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

public class LocalCognitoTokenStore {

    private static final String KEY_COGNITO_TOKEN = "key_cognito_token";
    private static final String KEY_COGNITO_IDENTITY_ID = "key_cognito_identity_id";
    private static final String KEY_COGNITO_TOKEN_EXPIRATION = "key_cognito_token_expiration";

    private final SharedPreferences mSharedPreferences;

    public LocalCognitoTokenStore(@NonNull Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public LocalCognitoTokenStore(@NonNull SharedPreferences sharedPreferences) {
        mSharedPreferences = sharedPreferences;
    }

    @NonNull
    public CognitoToken getCognitoToken() {
        final String token = mSharedPreferences.getString(KEY_COGNITO_TOKEN, null);
        final String identityId = mSharedPreferences.getString(KEY_COGNITO_IDENTITY_ID, null);
        final long expirationTimeStamp = mSharedPreferences.getLong(KEY_COGNITO_TOKEN_EXPIRATION, -1);
        return new CognitoToken(token, identityId, expirationTimeStamp);
    }

    public void persist(@NonNull CognitoToken cognitoToken) {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_COGNITO_TOKEN, cognitoToken.getCognitoToken());
        editor.putString(KEY_COGNITO_IDENTITY_ID, cognitoToken.getIdentityId());
        editor.putLong(KEY_COGNITO_TOKEN_EXPIRATION, cognitoToken.getExpirationTimestamp());
        editor.apply();
    }

}

package co.smartreceipts.android.identity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

final class IdentityStore {

    private static final String KEY_EMAIL = "identity_email_address";
    private static final String KEY_TOKEN = "identity_token";

    private final SharedPreferences mSharedPreferences;

    public IdentityStore(@NonNull Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public IdentityStore(@NonNull SharedPreferences sharedPreferences) {
        mSharedPreferences = sharedPreferences;
    }

    @Nullable
    public EmailAddress getEmail() {
        final String email = mSharedPreferences.getString(KEY_EMAIL, null);
        if (email != null) {
            return new EmailAddress(email);
        } else {
            return null;
        }
    }

    @Nullable
    public Token getToken() {
        final String token = mSharedPreferences.getString(KEY_TOKEN, null);
        if (token != null) {
            return new Token(token);
        } else {
            return null;
        }
    }

    public void setEmailAddress(@NonNull EmailAddress emailAddress) {
        setEmailAddress(emailAddress.get());
    }

    public void setEmailAddress(@NonNull String emailAddress) {
        mSharedPreferences.edit().putString(KEY_EMAIL, emailAddress).apply();
    }

    public void setToken(@NonNull Token token) {
        setToken(token.get());
    }

    public void setToken(@NonNull String token) {
        mSharedPreferences.edit().putString(KEY_TOKEN, token).apply();
    }


}

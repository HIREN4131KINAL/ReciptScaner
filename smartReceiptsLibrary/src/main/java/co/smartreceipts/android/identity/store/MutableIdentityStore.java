package co.smartreceipts.android.identity.store;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class MutableIdentityStore implements IdentityStore {

    private static final String KEY_EMAIL = "identity_email_address";
    private static final String KEY_TOKEN = "identity_token";

    private final SharedPreferences mSharedPreferences;

    public MutableIdentityStore(@NonNull Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public MutableIdentityStore(@NonNull SharedPreferences sharedPreferences) {
        mSharedPreferences = sharedPreferences;
    }

    @Nullable
    @Override
    public EmailAddress getEmail() {
        final String email = mSharedPreferences.getString(KEY_EMAIL, null);
        if (email != null) {
            return new EmailAddress(email);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Token getToken() {
        final String token = mSharedPreferences.getString(KEY_TOKEN, null);
        if (token != null) {
            return new Token(token);
        } else {
            return null;
        }
    }

    @Override
    public boolean isLoggedIn() {
        return getEmail() != null && getToken() != null;
    }

    public void setEmailAndToken(@Nullable String emailAddress, @Nullable String token) {
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEY_EMAIL, emailAddress);
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

}

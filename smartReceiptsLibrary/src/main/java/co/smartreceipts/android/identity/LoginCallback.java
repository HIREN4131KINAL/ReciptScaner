package co.smartreceipts.android.identity;

public interface LoginCallback {

    /**
     * Called when a login request completes successfully
     */
    void onLoginSuccess();

    /**
     * Called when a login request fails
     */
    void onLoginFailure();
}

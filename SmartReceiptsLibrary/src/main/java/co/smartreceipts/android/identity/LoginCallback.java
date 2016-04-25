package co.smartreceipts.android.identity;

public interface LoginCallback {

    /**
     * Called when a login request completes successfully
     */
    void onSuccess();

    /**
     * Called when a login request fails
     */
    void onFailure();
}

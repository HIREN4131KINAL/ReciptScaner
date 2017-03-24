package co.smartreceipts.android.push.services;

import com.google.firebase.iid.FirebaseInstanceIdService;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.utils.log.Logger;

public class FirebaseTokenRefreshService extends FirebaseInstanceIdService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        Logger.info(this, "onTokenRefresh");
        final SmartReceiptsApplication application = (SmartReceiptsApplication) getApplication();
        application.getPushManager().onTokenRefresh();
    }
}

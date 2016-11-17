package co.smartreceipts.android.cognito;

import android.content.Context;
import android.support.annotation.NonNull;

import com.amazonaws.auth.AWSAbstractCognitoIdentityProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.regions.Regions;

import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.LoginCallback;

public class SmartReceiptsCognitoSyncManager {

    private static final Regions REGION = Regions.US_EAST_1;

    private final AWSAbstractCognitoIdentityProvider mCognitoIdentityProvider;
    private final CognitoCachingCredentialsProvider mCredentialsProvider;
    private final CognitoSyncManager mCognitoSyncManager;

    public SmartReceiptsCognitoSyncManager(@NonNull Context context, @NonNull IdentityManager identityManager, @NonNull ServiceManager serviceManager) {
        mCognitoIdentityProvider = new SmartReceiptsAuthenticationProvider(context, identityManager, serviceManager, REGION);
        mCredentialsProvider = new CognitoCachingCredentialsProvider(context, mCognitoIdentityProvider, REGION);
        mCognitoSyncManager = new CognitoSyncManager(context, REGION, mCredentialsProvider);
    }

    public SmartReceiptsCognitoSyncManager(@NonNull CognitoCachingCredentialsProvider credentialsProvider, @NonNull AWSAbstractCognitoIdentityProvider cognitoIdentityProvider, @NonNull CognitoSyncManager cognitoSyncManager) {
        mCredentialsProvider = credentialsProvider;
        mCognitoIdentityProvider = cognitoIdentityProvider;
        mCognitoSyncManager = cognitoSyncManager;
    }

}

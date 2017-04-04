package co.smartreceipts.android.push;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.apis.me.MeResponse;
import co.smartreceipts.android.push.apis.me.UpdatePushTokensRequest;
import co.smartreceipts.android.push.apis.me.UpdateUserPushTokens;
import co.smartreceipts.android.push.internal.FcmTokenRetriever;
import co.smartreceipts.android.push.store.PushDataStore;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

@ApplicationScope
public class PushManager {

    private final IdentityManager identityManager;
    private final FcmTokenRetriever fcmTokenRetriever;
    private final PushDataStore pushDataStore;
    private final Scheduler subscribeOnScheduler;
    private final CopyOnWriteArrayList<PushMessageReceiver> pushMessageReceivers = new CopyOnWriteArrayList<>();

    @Inject
    public PushManager(Context context, IdentityManager identityManager) {
        this(identityManager, new FcmTokenRetriever(), new PushDataStore(context), Schedulers.io());
    }

    @VisibleForTesting
    public PushManager(@NonNull IdentityManager identityManager, @NonNull FcmTokenRetriever fcmTokenRetriever,
                       @NonNull PushDataStore pushDataStore, @NonNull Scheduler subscribeOnScheduler) {
        this.identityManager = Preconditions.checkNotNull(identityManager);
        this.fcmTokenRetriever = Preconditions.checkNotNull(fcmTokenRetriever);
        this.pushDataStore = Preconditions.checkNotNull(pushDataStore);
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    public void initialize() {
        identityManager.isLoggedInStream()
                .subscribeOn(subscribeOnScheduler)
                .flatMap(new Func1<Boolean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(final Boolean isLoggedIn) {
                        return pushDataStore.isRemoteRefreshRequiredObservable()
                                .map(new Func1<Boolean, Boolean>() {
                                    @Override
                                    public Boolean call(Boolean isRefreshRequired) {
                                        return isRefreshRequired && isLoggedIn;
                                    }
                                });
                    }
                })
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean shouldPushTokenBeUploaded) {
                        Logger.debug(PushManager.this, "Is a push token update required? {}.", shouldPushTokenBeUploaded);
                        return shouldPushTokenBeUploaded;
                    }
                })
                .flatMap(new Func1<Boolean, Observable<String>>() {
                    @Override
                    public Observable<String> call(Boolean aBoolean) {
                        return fcmTokenRetriever.getFcmTokenObservable();
                    }
                })
                .flatMap(new Func1<String, Observable<MeResponse>>() {
                    @Override
                    public Observable<MeResponse> call(@NonNull String token) {
                        final UpdatePushTokensRequest request = new UpdatePushTokensRequest(new UpdateUserPushTokens(Collections.singletonList(Preconditions.checkNotNull(token))));
                        return identityManager.updateMe(request);
                    }
                })
                .subscribe(new Action1<MeResponse>() {
                    @Override
                    public void call(MeResponse meResponse) {
                        Logger.info(PushManager.this, "Successfully uploaded our push notification token");
                        pushDataStore.setRemoteRefreshRequired(false);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.error(PushManager.this, "Failed to upload our push notification token", throwable);
                    }
                });
    }

    public void registerReceiver(@NonNull PushMessageReceiver receiver) {
        pushMessageReceivers.add(Preconditions.checkNotNull(receiver));
    }

    public void unregisterReceiver(@NonNull PushMessageReceiver receiver) {
        pushMessageReceivers.remove(Preconditions.checkNotNull(receiver));
    }

    public void onTokenRefresh() {
        pushDataStore.setRemoteRefreshRequired(true);
        initialize();
    }

    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        for (final PushMessageReceiver pushMessageReceiver : pushMessageReceivers) {
            pushMessageReceiver.onMessageReceived(remoteMessage);
        }
    }

}

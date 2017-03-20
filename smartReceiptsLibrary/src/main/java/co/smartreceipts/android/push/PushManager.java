package co.smartreceipts.android.push;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.apis.me.MeResponse;
import co.smartreceipts.android.identity.apis.me.User;
import co.smartreceipts.android.push.apis.me.UpdatePushTokensRequest;
import co.smartreceipts.android.push.apis.me.UpdateUserPushTokens;
import co.smartreceipts.android.push.internal.FcmTokenRetriever;
import co.smartreceipts.android.push.store.PushDataStore;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class PushManager {

    private final Context context;
    private final IdentityManager identityManager;
    private final FcmTokenRetriever fcmTokenRetriever;
    private final PushDataStore pushDataStore;
    private final Scheduler subscribeOnScheduler;
    private final CopyOnWriteArrayList<PushMessageReceiver> pushMessageReceivers = new CopyOnWriteArrayList<>();

    public PushManager(@NonNull Context context, @NonNull IdentityManager identityManager) {
        this(context, identityManager, new FcmTokenRetriever(), new PushDataStore(context), Schedulers.io());
    }

    public PushManager(@NonNull Context context, @NonNull IdentityManager identityManager, @NonNull FcmTokenRetriever fcmTokenRetriever,
                       @NonNull PushDataStore pushDataStore, @NonNull Scheduler subscribeOnScheduler) {
        this.context = Preconditions.checkNotNull(context);
        this.identityManager = Preconditions.checkNotNull(identityManager);
        this.fcmTokenRetriever = Preconditions.checkNotNull(fcmTokenRetriever);
        this.pushDataStore = Preconditions.checkNotNull(pushDataStore);
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    public void initialize() {
        Observable.zip(pushDataStore.isRemoteRefreshRequiredObservable(), identityManager.isLoggedInObservable(), new Func2<Boolean, Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean isRefreshRequired, Boolean isLoggedIn) {
                        return isRefreshRequired && isLoggedIn;
                    }
                })
                .subscribeOn(subscribeOnScheduler)
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

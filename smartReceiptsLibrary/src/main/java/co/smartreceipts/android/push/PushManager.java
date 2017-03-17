package co.smartreceipts.android.push;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.CopyOnWriteArrayList;

import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.push.internal.FcmTokenRetriever;
import co.smartreceipts.android.push.store.PushDataStore;
import rx.Observable;
import rx.functions.Func1;

public class PushManager {

    private final Context context;
    private final ServiceManager serviceManager;
    private final FcmTokenRetriever fcmTokenRetriever;
    private final PushDataStore pushDataStore;
    private final CopyOnWriteArrayList<PushMessageReceiver> pushMessageReceivers = new CopyOnWriteArrayList<>();

    public PushManager(@NonNull Context context, @NonNull ServiceManager serviceManager) {
        this(context, serviceManager, new FcmTokenRetriever(), new PushDataStore(context));
    }

    public PushManager(@NonNull Context context, @NonNull ServiceManager serviceManager, @NonNull FcmTokenRetriever fcmTokenRetriever,
                       @NonNull PushDataStore pushDataStore) {
        this.context = Preconditions.checkNotNull(context);
        this.serviceManager = Preconditions.checkNotNull(serviceManager);
        this.fcmTokenRetriever = Preconditions.checkNotNull(fcmTokenRetriever);
        this.pushDataStore = Preconditions.checkNotNull(pushDataStore);
    }

    public void initialize() {
        pushDataStore.isRemoteRefreshRequiredObservable()
                .flatMap(new Func1<Boolean, Observable<String>>() {
                    @Override
                    public Observable<String> call(Boolean aBoolean) {
                        return fcmTokenRetriever.getFcmTokenObservable();
                    }
                })
                .flatMap(new Func1<String, Observable<?>>() {
                    @Override
                    public Observable<?> call(String s) {
                        // TODO: User our identity manager to PUT the token with our APIs
                        return null;
                    }
                });
        // TODO: Subscribe -> Save Store on Results
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

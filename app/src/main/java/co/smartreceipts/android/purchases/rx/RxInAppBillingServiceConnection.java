package co.smartreceipts.android.purchases.rx;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;

import com.android.vending.billing.IInAppBillingService;
import com.google.common.base.Preconditions;

import java.util.concurrent.atomic.AtomicBoolean;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

public class RxInAppBillingServiceConnection implements ServiceConnection {

    private final Context context;
    private final AtomicBoolean isBound = new AtomicBoolean(false);
    private final BehaviorSubject<IInAppBillingService> inAppBillingServiceSubject = BehaviorSubject.create();

    public RxInAppBillingServiceConnection(@NonNull Context context) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        inAppBillingServiceSubject.onNext(IInAppBillingService.Stub.asInterface(service));
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        inAppBillingServiceSubject.onNext(null);
    }

    @NonNull
    public Observable<IInAppBillingService> bindToInAppBillingService() {
        if (!isBound.getAndSet(true)) {
            final Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
            serviceIntent.setPackage("com.android.vending");
            context.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
        }

        return inAppBillingServiceSubject
                .filter(new Func1<IInAppBillingService, Boolean>() {
                    @Override
                    public Boolean call(IInAppBillingService inAppBillingService) {
                        return inAppBillingService != null;
                    }
                })
                .take(1)
                .asObservable();
    }
}

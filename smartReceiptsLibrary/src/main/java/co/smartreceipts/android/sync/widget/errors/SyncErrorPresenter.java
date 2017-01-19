package co.smartreceipts.android.sync.widget.errors;

import android.support.annotation.NonNull;
import android.view.View;

import co.smartreceipts.android.sync.errors.SyncErrorType;
import co.smartreceipts.android.sync.provider.SyncProvider;
import rx.Observable;

public class SyncErrorPresenter {

    public SyncErrorPresenter(@NonNull View view) {

    }

    public void present(@NonNull SyncErrorType syncErrorType) {

    }

    public void present(@NonNull SyncProvider syncProvider) {

    }

    @NonNull
    public Observable<SyncErrorType> getClickStream() {
        return Observable.empty();
    }

}

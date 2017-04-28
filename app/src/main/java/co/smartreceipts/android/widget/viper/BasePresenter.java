package co.smartreceipts.android.widget.viper;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import io.reactivex.disposables.CompositeDisposable;

public abstract class BasePresenter<ViewType, InteractorType> implements Presenter<ViewType, InteractorType> {

    protected final ViewType view;
    protected final InteractorType interactor;
    protected final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public BasePresenter(@NonNull ViewType view, @NonNull InteractorType interactor) {
        this.view = Preconditions.checkNotNull(view);
        this.interactor = Preconditions.checkNotNull(interactor);
    }

    @Override
    @CallSuper
    public void unsubscribe() {
        compositeDisposable.clear();
    }
}

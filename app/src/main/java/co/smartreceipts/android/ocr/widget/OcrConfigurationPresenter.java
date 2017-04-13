package co.smartreceipts.android.ocr.widget;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.smartreceipts.android.R;
import co.smartreceipts.android.identity.store.EmailAddress;
import co.smartreceipts.android.widget.Presenter;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class OcrConfigurationPresenter implements Presenter {

    private final OcrConfigurationInteractor interactor;
    private final Unbinder unbinder;
    private CompositeSubscription compositeSubscription;

    @BindView(R.id.ocr_configuration_welcome) TextView welcomeText;
    @BindView(R.id.ocr_configuration_scans_remaining) TextView scansRemaining;

    public OcrConfigurationPresenter(@NonNull OcrConfigurationInteractor interactor, @NonNull View view) {
        this.interactor = Preconditions.checkNotNull(interactor);
        this.unbinder = ButterKnife.bind(this, view);

        present(interactor.getEmail());
    }

    @Override
    public void onResume() {
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(interactor.getRemainingScansStream()
            .subscribe(new Action1<Integer>() {
                @Override
                public void call(@NonNull Integer remainingScans) {
                    present(remainingScans);
                }
            }));
    }

    @Override
    public void onPause() {
        if (compositeSubscription != null) {
            compositeSubscription.unsubscribe();
            compositeSubscription = null;
        }
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
    }

    private void present(@Nullable EmailAddress emailAddress) {
        welcomeText.setText(welcomeText.getContext().getString(R.string.ocr_configuration_welcome, emailAddress));
    }

    private void present(int remainingScans) {
        scansRemaining.setText(scansRemaining.getContext().getString(R.string.ocr_configuration_scans_remaining, remainingScans));
    }
}

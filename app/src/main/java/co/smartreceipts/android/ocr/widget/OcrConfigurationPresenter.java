package co.smartreceipts.android.ocr.widget;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.smartreceipts.android.R;
import co.smartreceipts.android.identity.store.EmailAddress;
import co.smartreceipts.android.purchases.model.AvailablePurchase;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.Presenter;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class OcrConfigurationPresenter implements Presenter {

    private final OcrConfigurationInteractor interactor;
    private final Unbinder unbinder;

    private OcrPurchasesListAdapter ocrPurchasesListAdapter;
    private CompositeSubscription compositeSubscription;

    @BindView(R.id.ocr_configuration_welcome) TextView welcomeText;
    @BindView(R.id.ocr_configuration_scans_remaining) TextView scansRemaining;

    public OcrConfigurationPresenter(@NonNull OcrConfigurationInteractor interactor, @NonNull View headerView,
                                     @NonNull RecyclerView recyclerView) {
        this.interactor = Preconditions.checkNotNull(interactor);
        this.unbinder = ButterKnife.bind(this, headerView);
        this.ocrPurchasesListAdapter = new OcrPurchasesListAdapter(headerView);

        recyclerView.setAdapter(this.ocrPurchasesListAdapter);
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

        compositeSubscription.add(interactor.getAvailableOcrPurchases()
            .subscribe(new Action1<List<AvailablePurchase>>() {
                @Override
                public void call(List<AvailablePurchase> availablePurchases) {
                    Logger.info(OcrConfigurationPresenter.this, "Presenting list of purchases: {}.", availablePurchases);
                    ocrPurchasesListAdapter.setAvailablePurchases(availablePurchases);
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    Logger.warn(OcrConfigurationPresenter.this, "Failed to get available purchases.", throwable);
                }
            }));

        compositeSubscription.add(ocrPurchasesListAdapter.getAvailablePurchaseClicks()
            .subscribe(new Action1<AvailablePurchase>() {
                @Override
                public void call(AvailablePurchase availablePurchase) {
                    interactor.startOcrPurchase(availablePurchase);
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
        ocrPurchasesListAdapter = null;
    }

    private void present(@Nullable EmailAddress emailAddress) {
        welcomeText.setText(welcomeText.getContext().getString(R.string.ocr_configuration_welcome, emailAddress));
    }

    private void present(int remainingScans) {
        scansRemaining.setText(scansRemaining.getContext().getString(R.string.ocr_configuration_scans_remaining, remainingScans));
    }
}

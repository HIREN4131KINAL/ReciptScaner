package co.smartreceipts.android.ocr.widget.configuration;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.common.base.Preconditions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.smartreceipts.android.R;
import co.smartreceipts.android.purchases.model.AvailablePurchase;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.Presenter;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class OcrConfigurationPresenter implements Presenter {

    private final OcrConfigurationInteractor interactor;
    private final OcrConfigurationToolbarView ocrConfigurationToolbarView;
    private final Unbinder unbinder;

    private OcrPurchasesListAdapter ocrPurchasesListAdapter;
    private CompositeSubscription compositeSubscription;

    @BindView(R.id.ocr_save_scans_to_improve_results) CheckBox allowUsToSaveImagesRemotelyCheckbox;

    public OcrConfigurationPresenter(@NonNull OcrConfigurationInteractor interactor, @NonNull View headerView,
                                     @NonNull RecyclerView recyclerView, @NonNull OcrConfigurationToolbarView ocrConfigurationToolbarView) {
        this.interactor = Preconditions.checkNotNull(interactor);
        this.ocrConfigurationToolbarView = Preconditions.checkNotNull(ocrConfigurationToolbarView);
        this.unbinder = ButterKnife.bind(this, headerView);
        this.ocrPurchasesListAdapter = new OcrPurchasesListAdapter(headerView);

        recyclerView.setAdapter(this.ocrPurchasesListAdapter);
        allowUsToSaveImagesRemotelyCheckbox.setChecked(this.interactor.getAllowUsToSaveImagesRemotely());
        allowUsToSaveImagesRemotelyCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                OcrConfigurationPresenter.this.interactor.setAllowUsToSaveImagesRemotely(isChecked);
            }
        });
    }

    @Override
    public void onResume() {
        ocrConfigurationToolbarView.present(interactor.getEmail());

        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(interactor.getRemainingScansStream()
            .subscribe(new Action1<Integer>() {
                @Override
                public void call(@NonNull Integer remainingScans) {
                    ocrConfigurationToolbarView.present(remainingScans);
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

}

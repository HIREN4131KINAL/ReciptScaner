package co.smartreceipts.android.ocr.widget.configuration;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.common.base.Preconditions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.smartreceipts.android.R;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.Presenter;
import io.reactivex.disposables.CompositeDisposable;

public class OcrConfigurationPresenter implements Presenter {

    private final OcrConfigurationInteractor interactor;
    private final OcrConfigurationToolbarView ocrConfigurationToolbarView;
    private final Unbinder unbinder;

    private OcrPurchasesListAdapter ocrPurchasesListAdapter;
    private CompositeDisposable compositeDisposable;

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

        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(interactor.getRemainingScansStream()
            .subscribe(ocrConfigurationToolbarView::present));

        compositeDisposable.add(interactor.getAvailableOcrPurchases()
            .subscribe(availablePurchases -> {
                    Logger.info(OcrConfigurationPresenter.this, "Presenting list of purchases: {}.", availablePurchases);
                    ocrPurchasesListAdapter.setAvailablePurchases(availablePurchases);
            }, throwable -> Logger.warn(OcrConfigurationPresenter.this, "Failed to get available purchases.", throwable)));

        compositeDisposable.add(ocrPurchasesListAdapter.getAvailablePurchaseClicks()
            .subscribe(availablePurchase -> interactor.startOcrPurchase(availablePurchase)));
    }

    @Override
    public void onPause() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
            compositeDisposable = null;
        }
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        ocrPurchasesListAdapter = null;
    }

}

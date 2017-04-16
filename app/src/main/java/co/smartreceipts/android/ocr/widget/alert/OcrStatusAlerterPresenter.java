package co.smartreceipts.android.ocr.widget.alert;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.google.common.base.Preconditions;
import com.tapadoo.alerter.Alert;
import com.tapadoo.alerter.Alerter;

import co.smartreceipts.android.R;
import co.smartreceipts.android.ocr.OcrInteractor;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.Presenter;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class OcrStatusAlerterPresenter implements Presenter {

    private final OcrInteractor ocrInteractor;
    private Alerter alerter;
    private Alert alert;

    private CompositeSubscription compositeSubscription;

    public OcrStatusAlerterPresenter(@NonNull Activity activity, @NonNull OcrInteractor ocrInteractor) {
        this.ocrInteractor = Preconditions.checkNotNull(ocrInteractor);
        this.alerter = Alerter.create(activity)
                .setTitle(R.string.ocr_status_title)
                .setBackgroundColor(R.color.smart_receipts_colorAccent)
                .setIcon(R.drawable.ic_receipt_white_24dp);
    }

    @Override
    public void onResume() {
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(ocrInteractor.getOcrProcessingStatus()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnUnsubscribe(new Action0() {
                @Override
                public void call() {
                    if (alert != null) {
                        alert.hide();
                    }
                }
            })
            .subscribe(new Action1<OcrProcessingStatus>() {
                @Override
                public void call(OcrProcessingStatus ocrProcessingStatus) {
                    Logger.debug(OcrStatusAlerterPresenter.this, "Displaying OCR Status: {}", ocrProcessingStatus);
                    if (ocrProcessingStatus == OcrProcessingStatus.UploadingImage) {
                        setTextAndShow(R.string.ocr_status_message_uploading_image);
                    } else if (ocrProcessingStatus == OcrProcessingStatus.PerformingScan) {
                        setTextAndShow(R.string.ocr_status_message_performing_scan);
                    } else if (ocrProcessingStatus == OcrProcessingStatus.RetrievingResults) {
                        // Intentional no-op
                    } else {
                        if (alert != null) {
                            alert.hide();
                        }
                    }
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
        alert = null;
        alerter = null;
    }

    private void setTextAndShow(@StringRes int stringResId) {
        if (alert == null) {
            alerter.setText(stringResId);
            alert = alerter.show();
        } else {
            alert.setText(stringResId);
            alert.invalidate();
        }
    }
}

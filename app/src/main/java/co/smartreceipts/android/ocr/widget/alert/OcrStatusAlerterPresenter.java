package co.smartreceipts.android.ocr.widget.alert;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.google.common.base.Preconditions;
import com.tapadoo.alerter.Alert;
import com.tapadoo.alerter.Alerter;

import co.smartreceipts.android.R;
import co.smartreceipts.android.ocr.OcrInteractor;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.OldPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;


public class OcrStatusAlerterPresenter implements OldPresenter {

    private final OcrInteractor ocrInteractor;
    private Alerter alerter;
    private Alert alert;

    private CompositeDisposable compositeDisposable;

    public OcrStatusAlerterPresenter(@NonNull Activity activity, @NonNull OcrInteractor ocrInteractor) {
        this.ocrInteractor = Preconditions.checkNotNull(ocrInteractor);
        this.alerter = Alerter.create(activity)
                .setTitle(R.string.ocr_status_title)
                .setBackgroundColor(R.color.smart_receipts_colorAccent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.alerter.setIcon(R.drawable.ic_receipt_white_24dp);
        }
    }

    @Override
    public void onResume() {
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(ocrInteractor.getOcrProcessingStatus()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose(() -> {
                    if (alert != null) {
                        alert.hide();
                    }
                })
                .subscribe(ocrProcessingStatus -> {
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
                }));
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

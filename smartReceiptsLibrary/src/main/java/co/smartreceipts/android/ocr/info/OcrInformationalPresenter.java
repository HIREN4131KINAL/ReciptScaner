package co.smartreceipts.android.ocr.info;

import android.support.annotation.NonNull;
import android.view.View;

import com.google.common.base.Preconditions;

public class OcrInformationalPresenter {

    private final OcrInformationalInteractor mInteractor;

    public OcrInformationalPresenter(@NonNull View view, @NonNull OcrInformationalInteractor interactor) {
        mInteractor = Preconditions.checkNotNull(interactor);
    }

    public void onResume() {

    }

    public void onPause() {

    }
}

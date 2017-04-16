package co.smartreceipts.android.ocr.widget.configuration;

import android.support.annotation.Nullable;

import co.smartreceipts.android.identity.store.EmailAddress;

public interface OcrConfigurationToolbarView {

    void present(@Nullable EmailAddress emailAddress);

    void present(int remainingScans);
}

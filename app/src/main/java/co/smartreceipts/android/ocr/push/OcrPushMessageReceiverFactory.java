package co.smartreceipts.android.ocr.push;

import android.support.annotation.NonNull;

public class OcrPushMessageReceiverFactory  {

    @NonNull
    public OcrPushMessageReceiver get() {
        return new OcrPushMessageReceiver();
    }

}

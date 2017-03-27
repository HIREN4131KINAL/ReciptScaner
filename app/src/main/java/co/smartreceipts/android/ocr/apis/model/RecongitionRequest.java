package co.smartreceipts.android.ocr.apis.model;

import android.support.annotation.NonNull;

public class RecongitionRequest {

    private Recognition recognition;

    public RecongitionRequest(@NonNull String s3Path) {
        this.recognition = new Recognition();
        this.recognition.s3_path = s3Path;
    }

    public static class Recognition {
        private String s3_path;
    }
}

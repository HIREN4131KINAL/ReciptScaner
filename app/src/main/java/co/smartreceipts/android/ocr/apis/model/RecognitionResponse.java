package co.smartreceipts.android.ocr.apis.model;

import android.support.annotation.Nullable;

public class RecognitionResponse {

    private Recognition recognition;

    @Nullable
    public Recognition getRecognition() {
        return recognition;
    }

    public static class Recognition {

        private String id;
        private String status;
        private String s3_path;
        private String data;
        private long created_at;

        @Nullable
        public String getId() {
            return id;
        }

        @Nullable
        public String getStatus() {
            return status;
        }

        @Nullable
        public String getS3Path() {
            return s3_path;
        }

        @Nullable
        public String getData() {
            return data;
        }

        @Nullable
        public long getCreatedAt() {
            return created_at;
        }

    }
}

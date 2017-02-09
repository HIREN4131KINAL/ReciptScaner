package co.smartreceipts.android.workers.reports.tables;

import android.support.annotation.NonNull;

import java.io.IOException;

public class TooManyColumnsException extends IOException {

    public TooManyColumnsException() {
        super("Cannot squeeze all columns in a single page");
    }

    public TooManyColumnsException(@NonNull Throwable cause) {
        super(cause.getMessage(), cause);
    }

}

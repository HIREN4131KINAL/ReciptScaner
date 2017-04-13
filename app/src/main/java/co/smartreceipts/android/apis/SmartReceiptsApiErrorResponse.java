package co.smartreceipts.android.apis;

import android.support.annotation.Nullable;

import java.util.List;

public class SmartReceiptsApiErrorResponse {

    private List<String> errors;

    @Nullable
    public List<String> getErrors() {
        return errors;
    }
}

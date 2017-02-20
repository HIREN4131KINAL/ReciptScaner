package co.smartreceipts.android.identity.apis.organizations;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

public class Error {

    private boolean hasError;
    private List<String> errors;

    public boolean hasError() {
        return hasError;
    }

    @NonNull
    public List<String> getErrors() {
        return errors != null ? errors : Collections.<String>emptyList();
    }
}

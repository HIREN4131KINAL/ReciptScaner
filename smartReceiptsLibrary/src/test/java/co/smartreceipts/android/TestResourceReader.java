package co.smartreceipts.android;

import android.support.annotation.NonNull;

import java.io.InputStream;

public class TestResourceReader {

    @NonNull
    public InputStream openStream(@NonNull String resourceName) {
        return getClass().getClassLoader().getResourceAsStream(resourceName);
    }
}

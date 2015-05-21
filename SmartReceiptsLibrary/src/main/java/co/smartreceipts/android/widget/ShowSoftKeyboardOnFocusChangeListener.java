package co.smartreceipts.android.widget;

import android.app.Activity;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.ref.WeakReference;

public class ShowSoftKeyboardOnFocusChangeListener implements View.OnFocusChangeListener {

    private final WeakReference<Window> mWindowReference;

    public ShowSoftKeyboardOnFocusChangeListener(@NonNull Activity activity) {
        this(activity.getWindow());
    }

    public ShowSoftKeyboardOnFocusChangeListener(@NonNull Window window) {
        mWindowReference = new WeakReference<>(window);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus && view != null) {
            if (view.getResources().getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
                final Window window = mWindowReference.get();
                if (window != null) {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        }
    }
}

package co.smartreceipts.android.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class ShowSoftKeyboardOnFocusChangeListener implements View.OnFocusChangeListener {

    /**
     * After we resume Smart Receipts after using the camera, the input manager isn't quite ready yet to actually
     * show the keyboard for some reason (as opposed to using a text receipt). I wasn't able to find any checks
     * for why this fails programtically, so I built in a hacky hardcoded delay to help resolve this
     */
    private static final int DELAY_TO_SHOW_KEYBOARD_MILLIS = 250;

    @Override
    public void onFocusChange(final View view, final boolean hasFocus) {
        if (hasFocus && view != null) {
            if (view.getResources().getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
                final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    // Try both immediately
                    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // And delayed
                            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                        }
                    }, DELAY_TO_SHOW_KEYBOARD_MILLIS);
                }
            }
        }
    }
}

package co.smartreceipts.android.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class ShowSoftKeyboardOnFocusChangeListener implements View.OnFocusChangeListener {

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus && view != null) {
            if (view.getResources().getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
                final InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }
    }
}

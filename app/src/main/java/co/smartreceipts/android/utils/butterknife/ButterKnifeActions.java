package co.smartreceipts.android.utils.butterknife;

import android.support.annotation.NonNull;
import android.view.View;

import butterknife.ButterKnife;

public class ButterKnifeActions {

    public static ButterKnife.Action<View> setEnabled(final boolean isEnabled) {
        return new ButterKnife.Action<View>() {
            @Override
            public void apply(@NonNull View view, int index) {
                view.setEnabled(isEnabled);
            }
        };
    }
}

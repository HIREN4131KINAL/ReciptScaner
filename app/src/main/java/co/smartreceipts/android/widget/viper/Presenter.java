package co.smartreceipts.android.widget.viper;

import android.support.v4.app.Fragment;

/**
 * Used to standardize our core presentation layer to ensure that we're using common patters (eg
 * subscribing/un-subscribing in onResume/onPause, destroying our views in onDestroyView).
 */
public interface Presenter<ViewType, InteractorType> {

    /**
     * Should be called immediately after a the call to {@link Fragment#onResume()}
     */
    void onResume();

    /**
     * Should be called immediately before a the call to {@link Fragment#onPause()}
     */
    void onPause();

}

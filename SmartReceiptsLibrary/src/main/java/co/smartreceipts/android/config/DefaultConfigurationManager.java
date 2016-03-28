package co.smartreceipts.android.config;

import android.content.Context;
import android.support.annotation.NonNull;

import co.smartreceipts.android.R;

/**
 * The default implementation of the Smart Receipts {@link ConfigurationManager} to enable/disable all standard
 * components within the app.
 */
public final class DefaultConfigurationManager implements ConfigurationManager {

    private final Context mContext;

    public DefaultConfigurationManager(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public boolean isSettingsMenuAvailable() {
        return mContext.getResources().getBoolean(R.bool.config_is_settings_menu_available);
    }


    @Override
    public boolean isTextReceiptsOptionAvailable() {
        return mContext.getResources().getBoolean(R.bool.config_is_settings_menu_available);
    }


    @Override
    public boolean isDistanceTrackingOptionAvailable() {
        return mContext.getResources().getBoolean(R.bool.config_is_settings_menu_available);
    }
}

package co.smartreceipts.android.config;

import android.content.Context;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.di.scopes.ApplicationScope;

/**
 * The default implementation of the Smart Receipts {@link ConfigurationManager} to enable/disable all standard
 * components within the app.
 */
@ApplicationScope
public final class DefaultConfigurationManager implements ConfigurationManager {

    @Inject Context context;

    @Inject
    public DefaultConfigurationManager() {
    }

    @Override
    public boolean isSettingsMenuAvailable() {
        return context.getResources().getBoolean(R.bool.config_is_settings_menu_available);
    }


    @Override
    public boolean isTextReceiptsOptionAvailable() {
        return context.getResources().getBoolean(R.bool.config_is_settings_menu_available);
    }


    @Override
    public boolean isDistanceTrackingOptionAvailable() {
        return context.getResources().getBoolean(R.bool.config_is_settings_menu_available);
    }
}

package co.smartreceipts.shadows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

@Implements(PreferenceActivity.class)
public class ShadowPreferenceActivity extends org.robolectric.shadows.ShadowPreferenceActivity {

    private PreferenceManager mPreferenceManager;

    /**
    * The starting request code given out to preference framework.
    */
    private static final int FIRST_REQUEST_CODE = 100;

    @Override
    @Implementation
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	try {
    		// For some reason, this constructor does not appear as valid in Eclipse... No idea why
    		// Using reflection to get it
    		Constructor<PreferenceManager> constructor = PreferenceManager.class.getConstructor(Activity.class, int.class);
    		mPreferenceManager = constructor.newInstance(realActivity, FIRST_REQUEST_CODE);

    	}
    	catch (Exception e) {
    		System.err.println(e.toString());
    	}
    }

    @Override
    @Implementation
    public Preference findPreference(CharSequence key) {
    	Preference preference = super.findPreference(key);
    	if (preference != null && mPreferenceManager != null) {
    		try {
    			Method method = Preference.class.getDeclaredMethod("onAttachedToHierarchy", PreferenceManager.class);
    			method.invoke(preference, mPreferenceManager);
    		}
    		catch (Exception e) {
        		System.err.println(e.toString());
    		}
    	}
    	return preference;
    }

    @Implementation
	public PreferenceManager getPreferenceManager() {
        return mPreferenceManager;
    }
}

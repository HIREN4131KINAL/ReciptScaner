package co.smartreceipts.android.testutils.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.bytecode.RobolectricInternals;

import android.content.Context;
import android.preference.Preference;

@Implements(Preference.class)
public class ShadowPreference {

	@RealObject
	protected Preference realPreference;

	public void __constructor__() {
		RobolectricInternals.getConstructor(Preference.class, realPreference, new Class[] { Context.class} );
	}
}

package co.smartreceipts.android.persistence;

import android.content.SharedPreferences;

/**
 * Defines each of the {@link SharedPreferences} definitions that I
 * am using, so I can load these asynchronously immediately (as opposed
 * to waiting until they are first accessed). Some definitions here
 * are also hard-coded from other libraries that I do not have direct
 * access to in order to speed up their accesses as well
 *
 * @author Will Baumann
 *
 */
public enum SharedPreferenceDefinitions {

	SmartReceipts_Preferences("SmartReceiptsPrefFile"),
	ReceiptFragment_Preferences("ReceiptsFragment.xml"),
	Flex_Preferences("Flex_Preferences"),
	Subclass_Preferences("SubClassPrefs"),
	ReceiptImageFragment_Preferences("ReceiptImageFragment.xml"),
	LegacyCamera_Preferences("CameraPrefsFile");

	private final String mName;

	private SharedPreferenceDefinitions(String name) {
		mName = name;
	}

	@Override
	public String toString() {
		return mName;
	}

}

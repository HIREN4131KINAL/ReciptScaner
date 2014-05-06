package co.smartreceipts.android.activities;

import android.content.Context;
import android.content.Intent;

public class SRNavUtils {

	private SRNavUtils() {}

	public static void showSettings(Context context) {
		final Intent intent = new Intent(context, SettingsActivity.class);
		context.startActivity(intent);
	}
}

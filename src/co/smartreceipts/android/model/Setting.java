package co.smartreceipts.android.model;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.R;
import android.content.Context;

public class Setting {

	private final String mTitle;
	private final String mFragment;
	
	private Setting(String title, String fragment) { 
		mTitle = title;
		mFragment = fragment;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public String getSummary() {
		return "";
	}
	
	public String getFragmentClassPath() {
		return mFragment;
	}
	
	public static List<Setting> getSettings(Context context) {
		final ArrayList<Setting> settings = new ArrayList<Setting>();
		final String[] titles = context.getResources().getStringArray(R.array.settings_titles);
		final String[] fragments = context.getResources().getStringArray(R.array.settings_fragments);
		if (titles.length != fragments.length) {
			throw new RuntimeException("All titles must have a fragment defined...");
		}
		for (int i=0; i < titles.length; i++) {
			settings.add(new Setting(titles[i], fragments[i]));
		}
		return settings;
	}
	
}

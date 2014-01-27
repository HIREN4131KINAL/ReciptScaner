package co.smartreceipts.android.fragments.preferences;

import co.smartreceipts.android.R;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import co.smartreceipts.android.activities.SettingsActivity;
import co.smartreceipts.android.activities.SettingsDetailsActivity;
import co.smartreceipts.android.activities.SettingsActivity.ExtraCodes;
import co.smartreceipts.android.model.Setting;
import co.smartreceipts.android.utils.Utils;

import com.actionbarsherlock.app.SherlockListFragment;

public class SettingsList extends SherlockListFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new SettingsAdapter(getSherlockActivity(), Setting.getSettings(getSherlockActivity())));
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.settings_list, container, false);
		return rootView;
	}
	
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onListItemClick(ListView l, View v, int position, long id) {
		final Setting setting = (Setting) getListAdapter().getItem(position);
		if (getResources().getBoolean(R.bool.isTablet)) {
			// TODO: Add tag here
			getFragmentManager().beginTransaction()
								.replace(R.id.content_details, SettingsActivity.getFragmentFromClassPath(setting.getFragmentClassPath()))
								.commit();
			if (Utils.ApiHelper.hasHoneycomb()) {
				v.setActivated(true);
			}
		}
		else {
			final Intent intent = new Intent(getSherlockActivity(), SettingsDetailsActivity.class);
			intent.putExtra(ExtraCodes.FRAGMENT_EXTRA, setting.getFragmentClassPath());
			startActivity(intent);
		}
	}
		
}

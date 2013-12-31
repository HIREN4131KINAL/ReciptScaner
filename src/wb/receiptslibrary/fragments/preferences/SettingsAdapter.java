package wb.receiptslibrary.fragments.preferences;

import java.util.List;

import wb.receiptslibrary.R;
import wb.receiptslibrary.model.Setting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SettingsAdapter extends BaseAdapter {

	private final LayoutInflater mInflater;
	private final List<Setting> mSettings;
	
	public SettingsAdapter(Context context, List<Setting> settings) {
		mInflater = LayoutInflater.from(context);
		mSettings = settings;
	}
	
	@Override
	public int getCount() {
		return mSettings.size();
	}

	@Override
	public Setting getItem(int position) {
		return mSettings.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	private static class MyViewHolder {
		ImageView icon;
    	TextView title, summary;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Setting setting = getItem(position);
		MyViewHolder holder;
		if (convertView == null) {
    		holder = new MyViewHolder();
			convertView = mInflater.inflate(R.layout.settings_list_item, parent, false);
			holder.title = (TextView) convertView.findViewById(android.R.id.title);
			holder.summary = (TextView) convertView.findViewById(android.R.id.summary);
			holder.icon = (ImageView) convertView.findViewById(android.R.id.icon);
	    	convertView.setTag(holder);
    	}
    	else {
    		holder = (MyViewHolder) convertView.getTag();
    	}
    	holder.title.setText(setting.getTitle());
    	holder.summary.setText(setting.getSummary());
        return convertView;
	}

}

package co.smartreceipts.android.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import co.smartreceipts.android.R;

/**
 * Manages the ActionBar custom view and drop-down window
 */
public class ActionBarController implements OnNavigationListener {

	private final LayoutInflater mInflater;
	private final Resources mResources;
	private final ActionBar mActionBar;
	private final ActionBarAdapter mActionBarAdapter;
	private OnNavigationListener mListener;
	private CharSequence mSubTitle;
	private CharSequence mTitle;
	private boolean mHideSubtitle;

	// The Action Bar sends a synthetic navigation event to  list item 0 whenever the ActionBar is laid out
	// in Navigation Mode. We want to avoid this first call, since the Controller will be generating it
	private boolean mIgnoreInitialSyntheticNaviagation;

	public ActionBarController(Context context, ActionBar actionBar, String[] list) {
		mInflater = LayoutInflater.from(context);
		mResources = context.getResources();
		mActionBar = actionBar;
		mActionBarAdapter = new ActionBarAdapter(context, list);
		if (list.length > 0) {
			mSubTitle = list[0];
		}
		// Setting Reasonable Defaults
		displayStandardActionBar();
		mIgnoreInitialSyntheticNaviagation = false;
	}

	public int getActionBarLayoutId() {
		return R.layout.actionbar_title_item;
	}

	public int getActionBarDropDownLayout() {
		return R.layout.actionbar_dropdown_item;
	}

	public int getActionBarTitleId() {
		return android.R.id.text1;
	}

	public int getActionBarSubTitleId() {
		return android.R.id.text2;
	}

	public void setTitle(CharSequence title) {
		mTitle = title;
		mActionBarAdapter.notifyDataSetChanged();
	}

	public void setTitle(int resId) {
		setTitle(mResources.getString(resId));
	}

	public void setSubTitle(CharSequence subtitle) {
		if (!TextUtils.isEmpty(subtitle)) {
			if (!subtitle.equals(mSubTitle)) {
				mSubTitle = subtitle;
				displayNaviagationActionBar();
			}
		}
		else {
			setHideSubtitle(true);
		}
	}

	public void setSubTitle(int resId) {
		setSubTitle(mResources.getString(resId));
	}

	public void setHideSubtitle(boolean hideSubtitle) {
		mHideSubtitle = hideSubtitle;
		if (hideSubtitle) {
			mListener = null;
		}
		mActionBarAdapter.notifyDataSetChanged();
	}

	public void setOnNavigationListener(OnNavigationListener listener) {
		mListener = listener;
	}

	public void displayStandardActionBar() {
		if (mActionBar.getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			mActionBar.setDisplayShowTitleEnabled(true);
	        setHideSubtitle(true);
		}
	}

	public void displayNaviagationActionBar() {
		if (mActionBar.getNavigationMode() != ActionBar.NAVIGATION_MODE_LIST) {
			mIgnoreInitialSyntheticNaviagation = true;
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			mActionBar.setListNavigationCallbacks(mActionBarAdapter, this);
			mActionBar.setDisplayShowTitleEnabled(false);
	        setHideSubtitle(false);
		}
	}

	public void refreshActionBar() {
		mActionBarAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (mIgnoreInitialSyntheticNaviagation) {
			mIgnoreInitialSyntheticNaviagation = false;
			return true;
		}
		if (mListener != null) {
			mListener.onNavigationItemSelected(itemPosition, itemId);
			return true;
		}
		else {
			return false;
		}
	}

    private static class ViewHolder {
		public TextView title;
		public TextView subtitle;
	}

	private static class DropDownViewHolder {
	    public TextView title;
	}

    private class ActionBarAdapter extends ArrayAdapter<String> {

    	public ActionBarAdapter(Context context, String[] objects) {
    		super(context, getActionBarLayoutId(), objects);
    	}

    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		ViewHolder holder = null;
    		if (convertView == null) {
    			holder = new ViewHolder();
    	        convertView = mInflater.inflate(getActionBarLayoutId(), parent, false);
    	        holder.title = (TextView) convertView.findViewById(getActionBarTitleId());
    	        holder.subtitle = (TextView) convertView.findViewById(getActionBarSubTitleId());
    	        convertView.setTag(holder);
    		}
    		else {
    			holder = (ViewHolder) convertView.getTag();
    		}

    		holder.title.setText(mTitle);
    		holder.subtitle.setText(mSubTitle);
    		if (mHideSubtitle) {
    			holder.subtitle.setVisibility(View.GONE);
    		}
    		else {
    			holder.subtitle.setVisibility(View.VISIBLE);
    		}
    	    return convertView;

    	}

    	@Override
    	public View getDropDownView(int position, View convertView, ViewGroup parent) {
    	    DropDownViewHolder holder = null;
    	    if (convertView == null) {
    	    	holder = new DropDownViewHolder();
    	        convertView = mInflater.inflate(getActionBarDropDownLayout(), parent, false);
    	        holder.title = (TextView) convertView.findViewById(getActionBarTitleId());
    	        convertView.setTag(holder);
    	    } else {
    	        holder = (DropDownViewHolder) convertView.getTag();
    	    }

    	    holder.title.setText(getItem(position));
    	    return convertView;
    	}

    }

}
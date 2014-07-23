package co.smartreceipts.android.fragments.preferences;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.model.Columns;

public abstract class ColumnsListFragment extends WBFragment implements AdapterView.OnItemSelectedListener {

	public static String TAG = "ColumnsListFragment";

	private BaseAdapter mAdapter;
	private ListView mListView;
	private Columns mColumns;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mColumns = getColumns();
		setHasOptionsMenu(true);
	}

	public abstract Columns getColumns();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.simple_list, container, false);
		mListView = (ListView) rootView.findViewById(android.R.id.list);
		mAdapter = getAdapter();
		mListView.setAdapter(mAdapter);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		getSupportActionBar().setSubtitle(null);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_settings_columns, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_settings_add) {
			addColumn();
			mAdapter.notifyDataSetChanged();
			return true;
		}
		else if (item.getItemId() == R.id.menu_settings_delete) {
			deleteLastColumn();
			mAdapter.notifyDataSetChanged();
			return true;
		}
		else if (item.getItemId() == android.R.id.home) {
			getActivity().onBackPressed();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		SpinnerTag spinnerTag = (SpinnerTag) parent.getTag();
		updateColumn(spinnerTag.index, position);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// Stub
	}

	protected BaseAdapter getAdapter() {
		return new SettingsListAdapter(LayoutInflater.from(getActivity()));
	}

	protected int getListItemLayoutId() {
		return R.layout.settings_column_card_item;
	}

	public abstract void addColumn();

	public abstract void deleteLastColumn();

	public abstract void updateColumn(int arrayListIndex, int optionIndex);

	private static final class SpinnerTag {
		public int index; //0s index
	}

	private static final class MyViewHolder {
		public TextView column;
		public Spinner spinner;
	}

	private class SettingsListAdapter extends BaseAdapter {

		private final LayoutInflater mInflater;
		private final ArrayAdapter<CharSequence> mSpinnerAdapter;

		public SettingsListAdapter(LayoutInflater inflater) {
			mInflater = inflater;
			mSpinnerAdapter = mColumns.generateArrayAdapter(getActivity(), getFlex());
			mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		}

		@Override
		public int getCount() {
			return mColumns.size();
		}

		@Override
		public String getItem(int i) {
			return mColumns.getType(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View convertView, ViewGroup parent) {
			MyViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(getListItemLayoutId(), parent, false);
				holder = new MyViewHolder();
				holder.column = (TextView) convertView.findViewById(android.R.id.title);
				holder.spinner = (Spinner) convertView.findViewById(R.id.column_spinner);
				holder.spinner.setAdapter(mSpinnerAdapter);
				holder.spinner.setOnItemSelectedListener(ColumnsListFragment.this);
				holder.spinner.setTag(new SpinnerTag());
				convertView.setTag(holder);
			}
			else {
				holder = (MyViewHolder) convertView.getTag();
			}
			holder.column.setText(getString(R.string.column_item, Integer.toString(i+1))); //Add +1 to make it not 0-th index
			SpinnerTag spinnerTag = (SpinnerTag) holder.spinner.getTag();
			String type = getItem(i);
	        int pos = mSpinnerAdapter.getPosition(type);
	        if (pos < 0) { //This was a customized, non-accessible entry
	        	mSpinnerAdapter.add(type);
	        	holder.spinner.setSelection(mSpinnerAdapter.getPosition(type));
	        }
	        else {
	        	holder.spinner.setSelection(pos);
	        }
	        spinnerTag.index = i;
			return convertView;
		}

	}

}
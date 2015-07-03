package co.smartreceipts.android.fragments.preferences;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
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

import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;

public abstract class ColumnsListFragment extends WBFragment implements AdapterView.OnItemSelectedListener {

	public static String TAG = "ColumnsListFragment";

	private BaseAdapter mAdapter;
    private Toolbar mToolbar;
	private ListView mListView;
	private List<Column<Receipt>> mColumns;
    private ArrayAdapter<Column<Receipt>> mSpinnerAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mColumns = getColumns();
        mSpinnerAdapter = new ArrayAdapter<Column<Receipt>>(getActivity(), android.R.layout.simple_spinner_item, getColumnDefinitions().getAllColumns());
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		setHasOptionsMenu(true);
	}

	public abstract List<Column<Receipt>> getColumns();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.simple_list, container, false);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
		mListView = (ListView) rootView.findViewById(android.R.id.list);
		mAdapter = getAdapter();
		mListView.setAdapter(mAdapter);
		return rootView;
	}

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setSupportActionBar(mToolbar);
    }

    @Override
	public void onResume() {
		super.onResume();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(null);
        }
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
        final Column<Receipt> oldColumn = mColumns.get(spinnerTag.index);
        final Column<Receipt> newColumn = mSpinnerAdapter.getItem(position);
		updateColumn(oldColumn, newColumn);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// Stub
	}

    protected ColumnDefinitions<Receipt> getColumnDefinitions() {
        return new ReceiptColumnDefinitions(getActivity(), getPersistenceManager(), getFlex());
    }

	protected BaseAdapter getAdapter() {
		return new SettingsListAdapter(LayoutInflater.from(getActivity()));
	}

	protected int getListItemLayoutId() {
		return R.layout.settings_column_card_item;
	}

	public abstract void addColumn();

	public abstract void deleteLastColumn();

	public abstract void updateColumn(Column<Receipt> oldColumn, Column<Receipt> newColumn);

	private static final class SpinnerTag {
		public int index; //0s index
	}

	private static final class MyViewHolder {
		public TextView column;
		public Spinner spinner;
	}

	private class SettingsListAdapter extends BaseAdapter {

		private final LayoutInflater mInflater;

		public SettingsListAdapter(LayoutInflater inflater) {
			mInflater = inflater;

		}

		@Override
		public int getCount() {
			return mColumns.size();
		}

		@Override
		public Column<Receipt> getItem(int i) {
			return mColumns.get(i);
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
	        spinnerTag.index = i;
			return convertView;
		}

	}

}
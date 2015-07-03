package co.smartreceipts.android.fragments.preferences;

import java.util.List;

import wb.android.dialog.BetterDialogBuilder;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.WBFragment;

public class CategoriesListFragment extends WBFragment implements View.OnClickListener {

	public static String TAG = "CategoriesListFragment";

	private BaseAdapter mAdapter;
    private Toolbar mToolbar;
	private ListView mListView;
	private List<CharSequence> mCategories;

	public static CategoriesListFragment newInstance() {
		return new CategoriesListFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCategories = getPersistenceManager().getDatabase().getCategoriesList();
		setHasOptionsMenu(true);
	}

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
            actionBar.setTitle(R.string.menu_main_categories);
            actionBar.setSubtitle(null);
        }
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_settings_categories, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_settings_add) {
			addCategory();
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

	protected BaseAdapter getAdapter() {
		return new CategoriesListAdapter(LayoutInflater.from(getActivity()));
	}

	protected int getListItemLayoutId() {
		return R.layout.settings_category_card_item;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.edit) {
			Integer index = (Integer) view.getTag();
			editCategory(index);
		}
		else if (view.getId() == R.id.delete) {
			Integer index = (Integer) view.getTag();
			deleteCategory(index);
		}
	}

	private void addCategory() {
		final BetterDialogBuilder innerBuilder = new BetterDialogBuilder(getActivity());
		final LinearLayout layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setGravity(Gravity.BOTTOM);
		layout.setPadding(6, 6, 6, 6);
		final TextView nameLabel = new TextView(getActivity()); nameLabel.setText(R.string.category_name);
		final EditText nameBox = new EditText(getActivity());
		final TextView codeLabel = new TextView(getActivity()); codeLabel.setText(R.string.category_code);
		final EditText codeBox = new EditText(getActivity());
		layout.addView(nameLabel);
		layout.addView(nameBox);
		layout.addView(codeLabel);
		layout.addView(codeBox);
		innerBuilder.setTitle(getString(R.string.dialog_category_add))
					.setView(layout)
					.setCancelable(true)
					.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							final String name = nameBox.getText().toString();
							final String code = codeBox.getText().toString();
							try {
								if (getPersistenceManager().getDatabase().insertCategory(name, code)) {
									mAdapter.notifyDataSetChanged();
									mListView.smoothScrollToPosition(mCategories.indexOf(name));
								}
								else {
									Toast.makeText(getActivity(), getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
								}
							}
							catch (SQLException e) {
								 Toast.makeText(getActivity(), getString(R.string.toast_error_category_exists), Toast.LENGTH_SHORT).show();
							}
						}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
					.show();
	}

	private void editCategory(int position) {
		final String oldName = mCategories.get(position).toString();
		final String oldCode = getPersistenceManager().getDatabase().getCategoryCode(oldName);

		final BetterDialogBuilder innerBuilder = new BetterDialogBuilder(getActivity());
		final LinearLayout layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setGravity(Gravity.BOTTOM);
		layout.setPadding(6, 6, 6, 6);
		final TextView nameLabel = new TextView(getActivity());
		nameLabel.setText(R.string.item_name);
		final EditText nameBox = new EditText(getActivity());
		nameBox.setText(oldName);
		final TextView codeLabel = new TextView(getActivity());
		codeLabel.setText(R.string.item_code);
		final EditText codeBox = new EditText(getActivity());
		codeBox.setText(oldCode);
		layout.addView(nameLabel);
		layout.addView(nameBox);
		layout.addView(codeLabel);
		layout.addView(codeBox);
		innerBuilder.setTitle(R.string.dialog_category_edit)
					.setView(layout)
					.setCancelable(true)
					.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							final String newName = nameBox.getText().toString();
							final String newCode = codeBox.getText().toString();
							try {
								if (getPersistenceManager().getDatabase().updateCategory(oldName, newName, newCode)) {
									mAdapter.notifyDataSetChanged();
									mListView.smoothScrollToPosition(mCategories.indexOf(newName));
								}
								else {
									Toast.makeText(getActivity(), getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
								}
							}
							catch (SQLException e) {
								 Toast.makeText(getActivity(), getString(R.string.toast_error_category_exists), Toast.LENGTH_SHORT).show();
							}
						}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
					.show();
	}

	private void deleteCategory(int position) {
		final String name = mCategories.get(position).toString();

		final AlertDialog.Builder innerBuilder = new AlertDialog.Builder(getActivity());
		innerBuilder.setTitle(getString(R.string.delete_item, name))
					.setCancelable(true)
					.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (getPersistenceManager().getDatabase().deleteCategory(name)) {
								mAdapter.notifyDataSetChanged();
							}
							else {
								Toast.makeText(getActivity(), getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
							}
						}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
					.show();
	}

	private static final class MyViewHolder {
		public TextView category;
		public TextView code;
		public View edit;
		public View delete;
	}

	private class CategoriesListAdapter extends BaseAdapter {

		private final LayoutInflater mInflater;

		public CategoriesListAdapter(LayoutInflater inflater) {
			mInflater = inflater;
		}

		@Override
		public int getCount() {
			return mCategories.size();
		}

		@Override
		public CharSequence getItem(int i) {
			return mCategories.get(i);
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
				holder.category = (TextView) convertView.findViewById(android.R.id.title);
				holder.code = (TextView) convertView.findViewById(android.R.id.summary);
				holder.edit = convertView.findViewById(R.id.edit);
				holder.delete = convertView.findViewById(R.id.delete);
				holder.edit.setOnClickListener(CategoriesListFragment.this);
				holder.delete.setOnClickListener(CategoriesListFragment.this);
				convertView.setTag(holder);
			}
			else {
				holder = (MyViewHolder) convertView.getTag();
			}
			holder.category.setText(getItem(i));
			holder.code.setText(getPersistenceManager().getDatabase().getCategoryCode(mCategories.get(i)));
			holder.edit.setTag(i);
			holder.delete.setTag(i);
			return convertView;
		}

	}

}
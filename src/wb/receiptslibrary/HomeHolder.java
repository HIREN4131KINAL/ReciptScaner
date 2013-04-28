package wb.receiptslibrary;

import wb.android.dialog.BetterDialogBuilder;
import wb.android.dialog.DirectDialogOnClickListener;
import wb.android.dialog.DirectLongLivedOnClickListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.wb.navigation.ViewHolderGroup;

public class HomeHolder extends ViewHolderGroup<SmartReceiptsActivity> {

	private static final boolean D = SmartReceiptsActivity.D;
	private static final String TAG = "HomeHolder";
	
	private final RelativeLayout mainLayout;
	private final ListView listView;
	private final Bundle bundle;
	
	public HomeHolder(final SmartReceiptsActivity activity, final RelativeLayout mainLayout, final ListView listView) {
		super(activity);
		this.mainLayout = mainLayout;
		this.listView = listView;
		this.bundle = null;
	}
	
	public HomeHolder(final SmartReceiptsActivity activity, final RelativeLayout mainLayout, final ListView listView, Bundle bundle) {
		super(activity);
		this.mainLayout = mainLayout;
		this.listView = listView;
		this.bundle = bundle;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		TripViewHolder child = new TripViewHolder(activity);
		child.setParent(this);
		if (bundle == null)
			this.pushChild(child);
		else {
			if (D) Log.d(TAG, "Restoring");
			this.pushChildButDontRender(child);
			ReceiptViewHolder receiptViewHolder = new ReceiptViewHolder(activity, bundle);
			receiptViewHolder.setParent(this);
			this.pushChild(receiptViewHolder);
		}
		if (D) Log.d(TAG, "onCreate");
	}
	
	void setMainLayout() {
		activity.setContentView(mainLayout);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = activity.getSupportMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return true;
	}
	
	public void setAdapter(BaseAdapter adapter) {
		listView.setAdapter(adapter);
	}
	
	public void viewTrip(final TripRow trip) {
		ReceiptViewHolder child = new ReceiptViewHolder(activity, trip);
		child.setParent(this);
		this.pushChild(child);
    }
	
    public boolean onParentOptionsItemSelected(MenuItem item) {
    	if (item.getItemId() == R.id.menu_main_about) {
    		String about = activity.getFlex().getString(R.string.DIALOG_ABOUT_MESSAGE);
    		try {
    			about = about.replace("VERSION_NAME", activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName);
    		} catch (NameNotFoundException e) { }
	    	final BetterDialogBuilder builder = new BetterDialogBuilder(activity);
			builder.setTitle(activity.getFlex().getString(R.string.DIALOG_ABOUT_TITLE))
				   .setMessage(about)
				   .setCancelable(true)
				   .show();
			return true;
    	}
    	else if (item.getItemId() == R.id.menu_main_settings) {
    		final Preferences preferences = activity.getPreferences();
	    	final BetterDialogBuilder builder = new BetterDialogBuilder(activity);
	    	final View scrollView = activity.getFlex().getView(R.layout.dialog_settings);
	    	final EditText email = (EditText) activity.getFlex().getSubView(scrollView, R.id.dialog_settings_email); 
	    	final EditText days = (EditText) activity.getFlex().getSubView(scrollView, R.id.dialog_settings_duration);
	    	final Spinner currencySpinner = (Spinner) activity.getFlex().getSubView(scrollView, R.id.dialog_settings_currency);
	    	final EditText minPrice = (EditText) activity.getFlex().getSubView(scrollView, R.id.dialog_settings_minprice);
	    	final EditText userID = (EditText) activity.getFlex().getSubView(scrollView, R.id.dialog_settings_userid);
	    	final CheckBox predictCategoires = (CheckBox) activity.getFlex().getSubView(scrollView, R.id.dialog_settings_predictcategories);
	    	final CheckBox useNativeCamera = (CheckBox) activity.getFlex().getSubView(scrollView, R.id.dialog_settings_usenativecamera);
	    	final CheckBox includeTaxField = (CheckBox) activity.getFlex().getSubView(scrollView, R.id.dialog_settings_tax);
	    	final CheckBox matchNameToCategory = (CheckBox) activity.getFlex().getSubView(scrollView, R.id.dialog_settings_matchnametocategory);
	    	final CheckBox matchCommentsToCategory = (CheckBox) activity.getFlex().getSubView(scrollView, R.id.dialog_settings_matchcommenttocategory);
	    	final CheckBox onlyIncludeExpensableItems = (CheckBox) activity.getFlex().getSubView(scrollView, R.id.dialog_settings_onlyreportexpensable);
	    	final CheckBox enableAutoCompleteSuggestions = (CheckBox) activity.getFlex().getSubView(scrollView, R.id.dialog_settings_enableautocompletesuggestions);
	    	
	    	Log.d(TAG, "" + (email == null));
	    	Log.d(TAG, "" + (preferences == null));
	    	email.setText(preferences.getDefaultEmailReceipient());
	    	days.setText(Integer.toString(preferences.getDefaultTripDuration()));
	    	userID.setText(preferences.getUserID());
			predictCategoires.setChecked(preferences.predictCategories());
			useNativeCamera.setChecked(preferences.useNativeCamera());
			includeTaxField.setChecked(preferences.includeTaxField());
			matchNameToCategory.setChecked(preferences.matchNameToCategory());
			matchCommentsToCategory.setChecked(preferences.matchCommentToCategory());
			onlyIncludeExpensableItems.setChecked(preferences.onlyIncludeExpensableReceiptsInReports());
			enableAutoCompleteSuggestions.setChecked(preferences.enableAutoCompleteSuggestions());
			
			//TODO: Abstract the Float Max stuff into the preferences file
			if (preferences.getMinimumReceiptPriceToIncludeInReports() != -Float.MAX_VALUE) minPrice.setText(Float.toString(preferences.getMinimumReceiptPriceToIncludeInReports()));
			final ArrayAdapter<CharSequence> currenices = new ArrayAdapter<CharSequence>(activity, android.R.layout.simple_spinner_item, activity.getDB().getCurrenciesList());
			currenices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			currencySpinner.setAdapter(currenices); currencySpinner.setPrompt("Default Currency");
			int idx = currenices.getPosition(preferences.getDefaultCurreny());
			if (idx > 0) currencySpinner.setSelection(idx);
			builder.setTitle("Settings")
				   .setView(scrollView)
				   .setCancelable(true)
				   .setPositiveButton("Save", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
			           public void onClick(DialogInterface dialog, int id) {
			        	   try {
				        	   if (days.getText().toString() != null && days.getText().toString().length() > 0 && days.getText().toString().length() < 4)
				        		   preferences.setDefaultTripDuration(Integer.parseInt(days.getText().toString()));
			        	   } catch (java.lang.NumberFormatException e) { 
			        		   if (D) Log.e(TAG, e.toString());
			        	   }
			        	   try {
				        	   if (minPrice.getText().toString() != null) {
				        		   if (minPrice.getText().toString().length() > 0 && minPrice.getText().toString().length() < 4)
				        			   preferences.setMinimumReceiptPriceToIncludeInReports(Integer.parseInt(minPrice.getText().toString()));
				        		   else if (minPrice.getText().toString().length() == 0)
				        			   preferences.setMinimumReceiptPriceToIncludeInReports(-Float.MAX_VALUE);
				        	   }
			        	   } catch (java.lang.NumberFormatException e) { 
			        		   if (D) Log.e(TAG, e.toString());
			        	   }
			               preferences.setDefaultEmailReceipient(email.getText().toString());
			               preferences.setDefaultCurreny(currencySpinner.getSelectedItem().toString());
			               preferences.setPredictCategories(predictCategoires.isChecked());
			               preferences.setUseNativeCamera(useNativeCamera.isChecked());
			               preferences.setMatchNameToCategory(matchNameToCategory.isChecked());
			               preferences.setMatchCommentToCategory(matchCommentsToCategory.isChecked());
			               preferences.setOnlyIncludeExpensableReceiptsInReports(onlyIncludeExpensableItems.isChecked());
			               preferences.setIncludeTaxField(includeTaxField.isChecked());
			               preferences.setEnableAutoCompleteSuggestions(enableAutoCompleteSuggestions.isChecked());
			               preferences.setUserID(userID.getText().toString());
				           preferences.commit();
			           }
			       })
				   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       })
				   .show();
			return true;
    	}
    	else if (item.getItemId() == R.id.menu_main_categories) {
    		this.showCategoriesMenu();
    		return true;
    	}
    	else if (item.getItemId() == R.id.menu_main_csv) {
    		activity.showCustomCSVMenu();
    		return true;
    	}
    	else if (item.getItemId() == R.id.menu_main_export) {
    		final BetterDialogBuilder builder = new BetterDialogBuilder(activity);
    		builder.setTitle("Export your receipts?")
    			   .setCancelable(true)
    			   .setPositiveButton("Export", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							(new ExportTask(HomeHolder.this,"Exporting your receipts...")).execute();
						}
    			    })
    			   .setNegativeButton("Cancel", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
    			    })
    			   .show();
    	}
    	return false;
    }
	
	private void showCategoriesMenu() {
    	final BetterDialogBuilder builder = new BetterDialogBuilder(activity);
		final LinearLayout outerLayout = new LinearLayout(activity);
		outerLayout.setOrientation(LinearLayout.VERTICAL);
		outerLayout.setGravity(Gravity.BOTTOM);
		outerLayout.setPadding(6, 6, 6, 6);
		final Spinner categoriesSpinner = new Spinner(activity);
		final ArrayAdapter<CharSequence> categories = new ArrayAdapter<CharSequence>(activity, android.R.layout.simple_spinner_item, activity.getDB().getCategoriesList());
		categories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categoriesSpinner.setAdapter(categories); categoriesSpinner.setPrompt("Category");
		outerLayout.addView(categoriesSpinner);
		builder.setTitle("Select A Category")
			   .setView(outerLayout)
			   .setCancelable(true)
			   .setLongLivedPositiveButton("Add", new DirectLongLivedOnClickListener<SmartReceiptsActivity>(activity) {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						final BetterDialogBuilder innerBuilder = new BetterDialogBuilder(activity);
						final LinearLayout layout = new LinearLayout(activity);
						layout.setOrientation(LinearLayout.VERTICAL);
						layout.setGravity(Gravity.BOTTOM);
						layout.setPadding(6, 6, 6, 6);
						final TextView nameLabel = new TextView(activity); nameLabel.setText("Name:");
						final EditText nameBox = new EditText(activity);
						final TextView codeLabel = new TextView(activity); codeLabel.setText("Code:");
						final EditText codeBox = new EditText(activity);
						layout.addView(nameLabel);
						layout.addView(nameBox);
						layout.addView(codeLabel);
						layout.addView(codeBox);
						innerBuilder.setTitle("Add Category")
									.setView(layout)
									.setCancelable(true)
									.setPositiveButton("Add", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											final String name = nameBox.getText().toString();
											final String code = codeBox.getText().toString();
											try {
												if (activity.getDB().insertCategory(name, code)) {
													categories.notifyDataSetChanged();
													categoriesSpinner.setSelection(categories.getPosition(name));
												}
												else {
													Toast.makeText(activity, activity.getFlex().getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
												}
											}
											catch (SQLException e) {
												 Toast.makeText(activity, "Error: An category with that name already exists", Toast.LENGTH_SHORT).show();
											}
										}
									})
									.setNegativeButton("Cancel", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
										}
									})
									.show();
					} 
			   })
			   .setLongLivedNeutralButton("Edit", new DirectLongLivedOnClickListener<SmartReceiptsActivity>(activity) {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						final BetterDialogBuilder innerBuilder = new BetterDialogBuilder(activity);
						final LinearLayout layout = new LinearLayout(activity);
						layout.setOrientation(LinearLayout.VERTICAL);
						layout.setGravity(Gravity.BOTTOM);
						layout.setPadding(6, 6, 6, 6);
						final String oldName = categoriesSpinner.getSelectedItem().toString();
						final TextView nameLabel = new TextView(activity); nameLabel.setText("Name:");
						final EditText nameBox = new EditText(activity); nameBox.setText(oldName);
						final String oldCode = activity.getDB().getCategoryCode(oldName);
						final TextView codeLabel = new TextView(activity); codeLabel.setText("Code:");
						final EditText codeBox = new EditText(activity); codeBox.setText(oldCode);
						layout.addView(nameLabel);
						layout.addView(nameBox);
						layout.addView(codeLabel);
						layout.addView(codeBox);
						innerBuilder.setTitle("Edit Category")
									.setView(layout)
									.setCancelable(true)
									.setPositiveButton("Update", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											final String newName = nameBox.getText().toString();
											final String newCode = codeBox.getText().toString();
											try {
												if (activity.getDB().updateCategory(oldName, newName, newCode)) {
													categories.notifyDataSetChanged();
													categoriesSpinner.setSelection(categories.getPosition(newName));
												}
												else {
													Toast.makeText(activity, activity.getFlex().getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
												}
											}
											catch (SQLException e) {
												 Toast.makeText(activity, "Error: An category with that name already exists", Toast.LENGTH_SHORT).show();
											}
										}
									})
									.setNegativeButton("Cancel", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
										}
									})
									.show();	
					} 
			   })
			   .setLongLivedNegativeButton("Delete", new DirectLongLivedOnClickListener<SmartReceiptsActivity>(activity) {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						if (categoriesSpinner.getSelectedItem() == null) //There are no categories left to delete
							dialog.cancel();
						final BetterDialogBuilder innerBuilder = new BetterDialogBuilder(activity);
						innerBuilder.setTitle("Delete " + categoriesSpinner.getSelectedItem().toString() + "?")
									.setCancelable(true)
									.setPositiveButton("Delete", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											final String name = categoriesSpinner.getSelectedItem().toString();
											if (activity.getDB().deleteCategory(name)) {
												categories.notifyDataSetChanged();
											}
											else {
												Toast.makeText(activity, activity._flex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
											}
										}
									})
									.setNegativeButton("Cancel", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
										}
									})
									.show();
					} 
			   })
			   .show();
    }
    
    public synchronized void onExportComplete(Uri uri) {
    	if (uri == null) {
    		Toast.makeText(activity, activity.getFlex().getString(R.string.EXPORT_ERROR), Toast.LENGTH_LONG).show();
    		return;
    	}
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);  
		emailIntent.setType("application/octet-stream");
		emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
    	activity.startActivity(Intent.createChooser(emailIntent, "Export To..."));
    }
	
	public final void emailTrip(final TripRow trip) {
		ReceiptViewHolder receiptHolder = new ReceiptViewHolder(activity, trip);
		receiptHolder.emailTrip();
	}
}
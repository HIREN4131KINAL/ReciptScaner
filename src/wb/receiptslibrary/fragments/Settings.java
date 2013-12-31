package wb.receiptslibrary.fragments;

import wb.android.dialog.BetterDialogBuilder;
import wb.android.dialog.LongLivedOnClickListener;
import wb.android.flex.Flex;
import wb.receiptslibrary.BuildConfig;
import wb.receiptslibrary.CSVColumns;
import wb.receiptslibrary.R;
import wb.receiptslibrary.SmartReceiptsApplication;
import wb.receiptslibrary.persistence.DatabaseHelper;
import wb.receiptslibrary.persistence.PersistenceManager;
import wb.receiptslibrary.persistence.Preferences;
import wb.receiptslibrary.workers.ExportTask;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This will become a framgent shortly...
 * But for the time being, this utility class will suffice
 * @author WRB
 *
 */
public class Settings implements ExportTask.Listener {
	
	private static final String TAG = "Settings";
	
	private Flex mFlex;
	private SmartReceiptsApplication mApp;
	private PersistenceManager mPersistenceManager;
	
	public Settings(SmartReceiptsApplication app) {
		mApp = app;
		mPersistenceManager = app.getPersistenceManager();
		mFlex = app.getFlex();
	}
		
	public void showSettingsMenu() {
		final Preferences preferences = mPersistenceManager.getPreferences();
    	final BetterDialogBuilder builder = new BetterDialogBuilder(mApp.getCurrentActivity());
    	final View scrollView = mFlex.getView(R.layout.dialog_settings);
    	final EditText email = (EditText) mFlex.getSubView(scrollView, R.id.dialog_settings_email); 
    	final EditText days = (EditText) mFlex.getSubView(scrollView, R.id.dialog_settings_duration);
    	final Spinner currencySpinner = (Spinner) mFlex.getSubView(scrollView, R.id.dialog_settings_currency);
    	final Spinner dateSeparatorSpinner = (Spinner) mFlex.getSubView(scrollView, R.id.dialog_settings_date_separator);
    	final EditText minPrice = (EditText) mFlex.getSubView(scrollView, R.id.dialog_settings_minprice);
    	final EditText userID = (EditText) mFlex.getSubView(scrollView, R.id.dialog_settings_userid);
    	final CheckBox predictCategoires = (CheckBox) mFlex.getSubView(scrollView, R.id.dialog_settings_predictcategories);
    	final CheckBox useNativeCamera = (CheckBox) mFlex.getSubView(scrollView, R.id.dialog_settings_usenativecamera);
    	final CheckBox includeTaxField = (CheckBox) mFlex.getSubView(scrollView, R.id.dialog_settings_tax);
    	final CheckBox matchNameToCategory = (CheckBox) mFlex.getSubView(scrollView, R.id.dialog_settings_matchnametocategory);
    	final CheckBox matchCommentsToCategory = (CheckBox) mFlex.getSubView(scrollView, R.id.dialog_settings_matchcommenttocategory);
    	final CheckBox onlyIncludeExpensableItems = (CheckBox) mFlex.getSubView(scrollView, R.id.dialog_settings_onlyreportexpensable);
    	final CheckBox enableAutoCompleteSuggestions = (CheckBox) mFlex.getSubView(scrollView, R.id.dialog_settings_enableautocompletesuggestions);
    	final CheckBox defaultToFirstReportDate = (CheckBox) mFlex.getSubView(scrollView, R.id.dialog_settings_defaultToFirstReportDate);

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
		defaultToFirstReportDate.setChecked(preferences.defaultToFirstReportDate());
		
		//TODO: Abstract the Float Max stuff into the preferences file
		if (preferences.getMinimumReceiptPriceToIncludeInReports() != -Float.MAX_VALUE) minPrice.setText(Float.toString(preferences.getMinimumReceiptPriceToIncludeInReports()));
		final ArrayAdapter<CharSequence> currenices = new ArrayAdapter<CharSequence>(mApp.getCurrentActivity(), android.R.layout.simple_spinner_item, mPersistenceManager.getDatabase().getCurrenciesList());
		currenices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		currencySpinner.setAdapter(currenices); currencySpinner.setPrompt("Default Currency");
		int idx = currenices.getPosition(preferences.getDefaultCurreny());
		if (idx > 0) currencySpinner.setSelection(idx);
		
		final ArrayAdapter<CharSequence> dateSeparators = new ArrayAdapter<CharSequence>(mApp.getCurrentActivity(), android.R.layout.simple_spinner_item);
		final String defaultSepartor = mPersistenceManager.getPreferences().getDateSeparator();
		dateSeparators.add("-");
		dateSeparators.add("/");
		if (!defaultSepartor.equals("-") && !defaultSepartor.equals("/")) {
			dateSeparators.add(defaultSepartor);
		}
		dateSeparators.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dateSeparatorSpinner.setAdapter(dateSeparators); dateSeparatorSpinner.setPrompt("Default Currency");
		int idx2 = dateSeparators.getPosition(defaultSepartor);
		if (idx2 > 0) dateSeparatorSpinner.setSelection(idx2);
		
		builder.setTitle("Settings")
			   .setView(scrollView)
			   .setCancelable(true)
			   .setPositiveButton("Save", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   try {
			        	   if (days.getText().toString() != null && days.getText().toString().length() > 0 && days.getText().toString().length() < 4)
			        		   preferences.setDefaultTripDuration(Integer.parseInt(days.getText().toString()));
		        	   } catch (java.lang.NumberFormatException e) { 
		        		   if (BuildConfig.DEBUG) Log.e(TAG, e.toString());
		        	   }
		        	   try {
			        	   if (minPrice.getText().toString() != null) {
			        		   if (minPrice.getText().toString().length() > 0 && minPrice.getText().toString().length() < 4)
			        			   preferences.setMinimumReceiptPriceToIncludeInReports(Integer.parseInt(minPrice.getText().toString()));
			        		   else if (minPrice.getText().toString().length() == 0)
			        			   preferences.setMinimumReceiptPriceToIncludeInReports(-Float.MAX_VALUE);
			        	   }
		        	   } catch (java.lang.NumberFormatException e) { 
		        		   if (BuildConfig.DEBUG) Log.e(TAG, e.toString());
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
		               preferences.setDateSeparator(dateSeparatorSpinner.getSelectedItem().toString());
		               preferences.setDefaultToFirstReportDate(defaultToFirstReportDate.isChecked());
			           preferences.commit();
		           }
		       })
			   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       })
			   .show();
	}
	
	public void showCategoriesMenu() {
    	final BetterDialogBuilder builder = new BetterDialogBuilder(mApp.getCurrentActivity());
		final LinearLayout outerLayout = new LinearLayout(mApp.getCurrentActivity());
		outerLayout.setOrientation(LinearLayout.VERTICAL);
		outerLayout.setGravity(Gravity.BOTTOM);
		outerLayout.setPadding(6, 6, 6, 6);
		final Spinner categoriesSpinner = new Spinner(mApp.getCurrentActivity());
		final ArrayAdapter<CharSequence> categories = new ArrayAdapter<CharSequence>(mApp.getCurrentActivity(), android.R.layout.simple_spinner_item, mPersistenceManager.getDatabase().getCategoriesList());
		categories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categoriesSpinner.setAdapter(categories); categoriesSpinner.setPrompt("Category");
		outerLayout.addView(categoriesSpinner);
		builder.setTitle("Select A Category")
			   .setView(outerLayout)
			   .setCancelable(true)
			   .setLongLivedPositiveButton("Add", new LongLivedOnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						final BetterDialogBuilder innerBuilder = new BetterDialogBuilder(mApp.getCurrentActivity());
						final LinearLayout layout = new LinearLayout(mApp.getCurrentActivity());
						layout.setOrientation(LinearLayout.VERTICAL);
						layout.setGravity(Gravity.BOTTOM);
						layout.setPadding(6, 6, 6, 6);
						final TextView nameLabel = new TextView(mApp.getCurrentActivity()); nameLabel.setText("Name:");
						final EditText nameBox = new EditText(mApp.getCurrentActivity());
						final TextView codeLabel = new TextView(mApp.getCurrentActivity()); codeLabel.setText("Code:");
						final EditText codeBox = new EditText(mApp.getCurrentActivity());
						layout.addView(nameLabel);
						layout.addView(nameBox);
						layout.addView(codeLabel);
						layout.addView(codeBox);
						innerBuilder.setTitle("Add Category")
									.setView(layout)
									.setCancelable(true)
									.setPositiveButton("Add", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											final String name = nameBox.getText().toString();
											final String code = codeBox.getText().toString();
											try {
												if (mPersistenceManager.getDatabase().insertCategory(name, code)) {
													categories.notifyDataSetChanged();
													categoriesSpinner.setSelection(categories.getPosition(name));
												}
												else {
													Toast.makeText(mApp.getCurrentActivity(), mFlex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
												}
											}
											catch (SQLException e) {
												 Toast.makeText(mApp.getCurrentActivity(), "Error: An category with that name already exists", Toast.LENGTH_SHORT).show();
											}
										}
									})
									.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
										}
									})
									.show();
					} 
			   })
			   .setLongLivedNeutralButton("Edit", new LongLivedOnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						final BetterDialogBuilder innerBuilder = new BetterDialogBuilder(mApp.getCurrentActivity());
						final LinearLayout layout = new LinearLayout(mApp.getCurrentActivity());
						layout.setOrientation(LinearLayout.VERTICAL);
						layout.setGravity(Gravity.BOTTOM);
						layout.setPadding(6, 6, 6, 6);
						final String oldName = categoriesSpinner.getSelectedItem().toString();
						final TextView nameLabel = new TextView(mApp.getCurrentActivity()); nameLabel.setText("Name:");
						final EditText nameBox = new EditText(mApp.getCurrentActivity()); nameBox.setText(oldName);
						final String oldCode = mPersistenceManager.getDatabase().getCategoryCode(oldName);
						final TextView codeLabel = new TextView(mApp.getCurrentActivity()); codeLabel.setText("Code:");
						final EditText codeBox = new EditText(mApp.getCurrentActivity()); codeBox.setText(oldCode);
						layout.addView(nameLabel);
						layout.addView(nameBox);
						layout.addView(codeLabel);
						layout.addView(codeBox);
						innerBuilder.setTitle("Edit Category")
									.setView(layout)
									.setCancelable(true)
									.setPositiveButton("Update", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											final String newName = nameBox.getText().toString();
											final String newCode = codeBox.getText().toString();
											try {
												if (mPersistenceManager.getDatabase().updateCategory(oldName, newName, newCode)) {
													categories.notifyDataSetChanged();
													categoriesSpinner.setSelection(categories.getPosition(newName));
												}
												else {
													Toast.makeText(mApp.getCurrentActivity(), mFlex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
												}
											}
											catch (SQLException e) {
												 Toast.makeText(mApp.getCurrentActivity(), "Error: A category with that name already exists", Toast.LENGTH_SHORT).show();
											}
										}
									})
									.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
										}
									})
									.show();	
					} 
			   })
			   .setLongLivedNegativeButton("Delete", new LongLivedOnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						if (categoriesSpinner.getSelectedItem() == null) //There are no categories left to delete
							dialog.cancel();
						final BetterDialogBuilder innerBuilder = new BetterDialogBuilder(mApp.getCurrentActivity());
						innerBuilder.setTitle("Delete " + categoriesSpinner.getSelectedItem().toString() + "?")
									.setCancelable(true)
									.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											final String name = categoriesSpinner.getSelectedItem().toString();
											if (mPersistenceManager.getDatabase().deleteCategory(name)) {
												categories.notifyDataSetChanged();
											}
											else {
												Toast.makeText(mApp.getCurrentActivity(), mFlex.getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
											}
										}
									})
									.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
	
	public void showAbout() {
		String about = mFlex.getString(R.string.DIALOG_ABOUT_MESSAGE);
		try {
			about = about.replace("VERSION_NAME", mApp.getCurrentActivity().getPackageManager().getPackageInfo(mApp.getCurrentActivity().getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) { }
    	final BetterDialogBuilder builder = new BetterDialogBuilder(mApp.getCurrentActivity());
		builder.setTitle(mFlex.getString(R.string.DIALOG_ABOUT_TITLE))
			   .setMessage(about)
			   .setCancelable(true)
			   .show();
	}
	
	public void showExport() {
		final BetterDialogBuilder builder = new BetterDialogBuilder(mApp.getCurrentActivity());
		builder.setTitle(R.string.dialog_export_title)
			   .setMessage(R.string.dialog_export_text)
			   .setCancelable(true)
			   .setPositiveButton(R.string.dialog_export_positive, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						(new ExportTask(mApp.getCurrentActivity(), "Exporting your receipts...", mPersistenceManager, Settings.this)).execute();
					}
			    })
			   .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
			    })
			   .show();
	}
	
	public void showCustomCSVMenu() {
        final BetterDialogBuilder builder = new BetterDialogBuilder(mApp.getCurrentActivity());
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        final LinearLayout parent = new LinearLayout(mApp.getCurrentActivity());
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.setGravity(Gravity.BOTTOM);
        parent.setPadding(6, 6, 6, 6);
        ScrollView scrollView = new ScrollView(mApp.getCurrentActivity());
        final LinearLayout layout = new LinearLayout(mApp.getCurrentActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.BOTTOM);
        layout.setPadding(6, 6, 6, 6);
        final CSVColumns csvColumns = mPersistenceManager.getDatabase().getCSVColumns();
        for (int i=0; i < csvColumns.size(); i++) {
                final LinearLayout horiz = addHorizontalCSVLayoutItem(csvColumns, i);
                layout.addView(horiz, params);
        }
        scrollView.addView(layout);
        final CheckBox checkBox = new CheckBox(mApp.getCurrentActivity());
        checkBox.setText("Include Header Columns");
        checkBox.setChecked(mPersistenceManager.getPreferences().includeCSVHeaders());
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	mPersistenceManager.getPreferences().setIncludeCSVHeaders(isChecked);
            }
                
        });
        parent.addView(checkBox, params);
        parent.addView(scrollView, params);
        builder.setTitle("Customize CSV File")
               .setView(parent)
               .setCancelable(true)
               .setLongLivedPositiveButton("Add Column", new LongLivedOnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	mPersistenceManager.getDatabase().insertCSVColumn();
                        layout.addView(addHorizontalCSVLayoutItem(csvColumns, csvColumns.size() - 1), params);
                    }
                })
                .setLongLivedNegativeButton("Remove Column", new LongLivedOnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (csvColumns.isEmpty()) {
                                return;
                        }
                        mPersistenceManager.getDatabase().deleteCSVColumn();
                        layout.removeViews(csvColumns.size(), 1);
                    }
                })
                .show();
	}
	
	private final LinearLayout addHorizontalCSVLayoutItem(CSVColumns csvColumns, int i) {
        final LinearLayout horiz = new LinearLayout(mApp.getCurrentActivity());
        final CSVColumnSelectionListener selectionListener = new CSVColumnSelectionListener(mPersistenceManager.getDatabase(), i);
        horiz.setOrientation(LinearLayout.HORIZONTAL);
        final Spinner spinner = new Spinner(mApp.getCurrentActivity());
        final ArrayAdapter<CharSequence> options = CSVColumns.getNewArrayAdapter(mApp.getCurrentActivity(), mFlex);
        options.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(options); spinner.setPrompt("Column Type");
        String type = csvColumns.getType(i);
        int pos = options.getPosition(type);
        if (pos < 0) { //This was a customized, non-accessbile entry
            options.add(type);
            spinner.setSelection(options.getPosition(type));
            spinner.setEnabled(false);
        }
        else {
            spinner.setSelection(pos);
        }
        spinner.setOnItemSelectedListener(selectionListener);
        final TextView textView = new TextView(mApp.getCurrentActivity());
        textView.setPadding(12, 0, 0, 0);
        textView.setText("Col. " + (i+1));
        textView.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mApp.getCurrentActivity().getResources().getDisplayMetrics()));
        horiz.addView(textView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 2f));
        horiz.addView(spinner, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
        return horiz;
	}
	
	private class CSVColumnSelectionListener implements OnItemSelectedListener {
        private DatabaseHelper _db;
        private int _index;
        private boolean _firstCall; //During the Spinner Creation, onItemSelected() is automatically called. This boolean ignores the initial call
        public CSVColumnSelectionListener(DatabaseHelper db, int index) {_db = db; _index = index; _firstCall = true;}
        
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (_firstCall) { //Ignore creation call
                _firstCall = false;
                return;
            }
            _db.updateCSVColumn(_index, position);
        }
        
        @Override 
        public void onNothingSelected(AdapterView<?> arg0) {}
        
	}

	@Override
	public void onExportComplete(Uri uri) {
		if (uri == null) {
            Toast.makeText(mApp.getCurrentActivity(), mFlex.getString(R.string.EXPORT_ERROR), Toast.LENGTH_LONG).show();
            return;
		}
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("application/octet-stream");
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        mApp.getCurrentActivity().startActivity(Intent.createChooser(emailIntent, "Export To..."));
	}
	
}

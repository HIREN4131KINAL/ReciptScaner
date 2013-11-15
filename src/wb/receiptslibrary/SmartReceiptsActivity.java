package wb.receiptslibrary;

import java.io.File;
import java.io.IOException;

import wb.android.dialog.BetterDialogBuilder;
import wb.android.dialog.DirectLongLivedOnClickListener;
import wb.android.flex.Flex;
import wb.android.flex.Flexable;
import wb.android.storage.SDCardStateException;
import wb.android.storage.StorageManager;
import wb.android.util.AppRating;
import wb.receiptslibrary.fragments.ReceiptImageFragment;
import wb.receiptslibrary.fragments.ReceiptsFragment;
import wb.receiptslibrary.fragments.Settings;
import wb.receiptslibrary.fragments.TripFragment;
import wb.receiptslibrary.model.ReceiptRow;
import wb.receiptslibrary.model.TripRow;
import wb.receiptslibrary.persistence.DatabaseHelper;
import wb.receiptslibrary.persistence.PersistenceManager;
import wb.receiptslibrary.persistence.Preferences;
import wb.receiptslibrary.workers.WorkerManager;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public abstract class SmartReceiptsActivity extends SherlockFragmentActivity implements Flexable, Navigable {
    
	//logging variables
    private static final String TAG = "SmartReceiptsActivity";
    
    //Camera Request Extras
    public static final String STRING_DATA = "strData";
    public static final int DIR = 0;
    public static final int NAME = 1;
    
    //Receiver Settings
    protected static final String FILTER_ACTION = "wb.receiptslibrary";
    
    //AppRating
    private static final int LAUNCHES_UNTIL_PROMPT = 15;
    
    //Instace Vars
    private Uri _actionSendUri;
    private boolean _calledFromActionSend;
    
    //Package-Accessible Instance Variables. None of these require get/set to improve performance
    protected Flex _flex;
    
    private WorkerManager mWorkerManager;
    private PersistenceManager mPersistenceManager;
	
	//Preference Identifiers - SubClasses Only
    protected static final String SUBCLASS_PREFS = "SubClassPrefs";
    protected static final String PREF1 = "pref1";
    
    //Settings
    private Settings mSettings; 
    
    protected final void onCreate(final Bundle savedInstanceState, final RelativeLayout mainLayout, final ListView listView) {
    	if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
    	super.onCreate(savedInstanceState);
        setResult(Activity.RESULT_CANCELED); //In case the user backs out
        _flex = Flex.getInstance(this, this);
        _calledFromActionSend = false;
        mWorkerManager = new WorkerManager(this);
        mPersistenceManager = new PersistenceManager(this);
        if (this.getIntent() != null && this.getIntent().getAction() != null && this.getIntent().getAction().equalsIgnoreCase(Intent.ACTION_SEND)) {
        	_calledFromActionSend = true;
        	if (this.getIntent().getExtras() != null) {
    	        String[] proj = {MediaStore.Images.Media.DATA};
    	        Cursor cursor = managedQuery((Uri) this.getIntent().getExtras().get(Intent.EXTRA_STREAM), proj, null, null, null);
    	        if (cursor == null) {
    	        	Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.IMG_SEND_ERROR), Toast.LENGTH_LONG).show();
    	        	return;
    	        }
    	        int col = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    	        cursor.moveToFirst();
    	        _actionSendUri = Uri.fromFile(new File(cursor.getString(col)));
    	        cursor.close();
    	        this.stopManagingCursor(cursor);
        	}
        	else {
        		Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.IMG_SEND_ERROR), Toast.LENGTH_LONG).show();
	        	return;
        	}
        }
        setContentView(R.layout.main);
        viewTrips();
        AppRating.onLaunch(this, LAUNCHES_UNTIL_PROMPT, "Smart Receipts", getPackageName());
    }
    
    //This is called after _sdCard is available but before _db is
    //This was added after version 78 (version 79 is the first "new" one)
    //Make this a listener
    public void onVersionUpgrade(int oldVersion, int newVersion) {
    	if (BuildConfig.DEBUG) Log.d(TAG, "Upgrading the app from version " + oldVersion + " to " + newVersion);
    	if (oldVersion <= 78) {
			try {
				StorageManager external = StorageManager.getExternalInstance(this);
				File db = this.getDatabasePath(DatabaseHelper.DATABASE_NAME); //Internal db file
				if (db != null && db.exists()) {
					File sdDB = external.getFile("receipts.db"); 
					if (sdDB.exists())
						sdDB.delete();
					if (BuildConfig.DEBUG) Log.d(TAG, "Copying the database file from " + db.getAbsolutePath() + " to " + sdDB.getAbsolutePath());
					try {
						external.copy(db, sdDB, true);
					}
					catch (IOException e) {
						if (BuildConfig.DEBUG) Log.e(TAG, e.toString());
					}
				}
			}
			catch (SDCardStateException e) { }
			oldVersion++;
		}
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	if (!mPersistenceManager.getStorageManager().isExternal())
    		Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.SD_WARNING), Toast.LENGTH_LONG).show();
    } 
    
    @Override
    protected final void onResume() {
    	super.onResume();
    	_flex = Flex.getInstance(this, this);
    	_flex.onResume();
    	if (_calledFromActionSend) {
    		final Preferences preferences = mPersistenceManager.getPreferences();
    		if (preferences.showActionSendHelpDialog()) {
	        	BetterDialogBuilder builder = new BetterDialogBuilder(this);
	        	builder.setTitle("Add Picture to Receipt")
	        		   .setMessage("Tap on an existing receipt to add this image to it.")
	        		   .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
	    					@Override
	    					public void onClick(DialogInterface dialog, int which) {
	    						dialog.cancel();
	    					}
	        		   })
	        		   .setNegativeButton("Don't Show Again", new DialogInterface.OnClickListener() {
	    					@Override
	    					public void onClick(DialogInterface dialog, int which) {
	    						preferences.setShowActionSendHelpDialog(false);
	    						preferences.commit();
	    						dialog.cancel();
	    					}
	        		   })
	        		   .show();
    		}
    	}
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if (_flex != null) _flex.onPause();
    }
    
    @Override
    protected final void onDestroy() {
    	super.onDestroy();
    	getPersistenceManager().onDestroy();
    }
    
    /**
     * This method is only called the first time Smart Receipts is run.
     * It gives a brief overview of how to use the app.
     */
    public void onFirstRun() {
    	final BetterDialogBuilder builder = new BetterDialogBuilder(this);
    	builder.setTitle(_flex.getString(R.string.DIALOG_WELCOME_TITLE))
    		   .setMessage(_flex.getString(R.string.DIALOG_WELCOME_MESSAGE))
    		   .setPositiveButton(_flex.getString(R.string.DIALOG_WELCOME_POSITIVE_BUTTON), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
    		   });
    	this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				builder.show();
			}
		});
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
		@Override public void onNothingSelected(AdapterView<?> arg0) {}
    	
    }
    public void showCustomCSVMenu() {
    	final BetterDialogBuilder builder = new BetterDialogBuilder(this);
    	final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    	final LinearLayout parent = new LinearLayout(this);
    	parent.setOrientation(LinearLayout.VERTICAL);
    	parent.setGravity(Gravity.BOTTOM);
    	parent.setPadding(6, 6, 6, 6);
    	ScrollView scrollView = new ScrollView(this);
		final LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setGravity(Gravity.BOTTOM);
		layout.setPadding(6, 6, 6, 6);
		final CSVColumns csvColumns = getPersistenceManager().getDatabase().getCSVColumns(_flex); 
		for (int i=0; i < csvColumns.size(); i++) {
			final LinearLayout horiz = addHorizontalCSVLayoutItem(csvColumns, i);
			layout.addView(horiz, params);
		}
		scrollView.addView(layout);
		final CheckBox checkBox = new CheckBox(this);
		checkBox.setText("Include Header Columns");
		checkBox.setChecked(getPersistenceManager().getPreferences().includeCSVHeaders());
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				getPersistenceManager().getPreferences().setIncludeCSVHeaders(isChecked);
			}
			
		});
		parent.addView(checkBox, params);
		parent.addView(scrollView, params);
		builder.setTitle("Customize CSV File")
			   .setView(parent)
			   .setCancelable(true)
			   .setLongLivedPositiveButton("Add Column", new DirectLongLivedOnClickListener<SmartReceiptsActivity>(this) {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						activity.getPersistenceManager().getDatabase().insertCSVColumn();
						layout.addView(addHorizontalCSVLayoutItem(csvColumns, csvColumns.size() - 1), params);
					}
				})
				.setLongLivedNegativeButton("Remove Column", new DirectLongLivedOnClickListener<SmartReceiptsActivity>(this) {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (csvColumns.isEmpty())
							return;
						activity.getPersistenceManager().getDatabase().deleteCSVColumn();
						layout.removeViews(csvColumns.size(), 1);
					}
				})
			   .show();
    }
    
    private final LinearLayout addHorizontalCSVLayoutItem(CSVColumns csvColumns, int i) {
		final LinearLayout horiz = new LinearLayout(this);
		final CSVColumnSelectionListener selectionListener = new CSVColumnSelectionListener(getPersistenceManager().getDatabase(), i);
		horiz.setOrientation(LinearLayout.HORIZONTAL);
		final Spinner spinner = new Spinner(this);
		final ArrayAdapter<CharSequence> options = CSVColumns.getNewArrayAdapter(this, _flex);
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
		final TextView textView = new TextView(this);
		textView.setPadding(12, 0, 0, 0);
		textView.setText("Col. " + (i+1));
		textView.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
		horiz.addView(textView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 2f)); 
		horiz.addView(spinner, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
		return horiz;
    }
    
    @Override
    public int getFleXML() {
    	return Flexable.UNDEFINED;
    }
    
    public void insertCSVDefaults(final DatabaseHelper db) { //Called in onCreate and onUpgrade
		db.insertCSVColumnNoCache(CSVColumns.CATEGORY_CODE(_flex));
		db.insertCSVColumnNoCache(CSVColumns.NAME(_flex));
		db.insertCSVColumnNoCache(CSVColumns.PRICE(_flex));
		db.insertCSVColumnNoCache(CSVColumns.CURRENCY(_flex));
		db.insertCSVColumnNoCache(CSVColumns.DATE(_flex));
	}
    
    public void insertCategoryDefaults(final DatabaseHelper db) {
		db.insertCategoryNoCache("<Category>", "NUL");
		db.insertCategoryNoCache("Airfare", "AIRP");
		db.insertCategoryNoCache("Breakfast", "BRFT");
		db.insertCategoryNoCache("Dinner", "DINN");
		db.insertCategoryNoCache("Entertainment", "ENT");
		db.insertCategoryNoCache("Gasoline", "GAS");
		db.insertCategoryNoCache("Gift", "GIFT");
		db.insertCategoryNoCache("Hotel", "HTL");
		db.insertCategoryNoCache("Laundry", "LAUN");
		db.insertCategoryNoCache("Lunch", "LNCH");
		db.insertCategoryNoCache("Other", "MISC");
		db.insertCategoryNoCache("Parking/Tolls", "PARK");
		db.insertCategoryNoCache("Postage/Shipping", "POST");
		db.insertCategoryNoCache("Car Rental", "RCAR");
		db.insertCategoryNoCache("Taxi/Bus", "TAXI");
		db.insertCategoryNoCache("Telephone/Fax", "TELE");
		db.insertCategoryNoCache("Tip", "TIP");
		db.insertCategoryNoCache("Train", "TRN");
		db.insertCategoryNoCache("Books/Periodicals", "ZBKP");
		db.insertCategoryNoCache("Cell Phone", "ZCEL");
		db.insertCategoryNoCache("Dues/Subscriptions", "ZDUE");
		db.insertCategoryNoCache("Meals (Justified)", "ZMEO");
		db.insertCategoryNoCache("Stationery/Stations", "ZSTS");
		db.insertCategoryNoCache("Training Fees", "ZTRN");
    }
    
    public Flex getFlex() {
    	return _flex;
    }
    
    public boolean calledFromActionSend() {
    	return _calledFromActionSend;
    }
    
    public Uri actionSendUri() {
    	return _actionSendUri;
    }
    
    ///////////////////////////////////////////////////////////////////////
    public WorkerManager getWorkerManager() {
    	if (mWorkerManager == null) {
    		mWorkerManager = new WorkerManager(this);
    	}
    	return mWorkerManager;
    }
    
    public PersistenceManager getPersistenceManager() {
    	if (mPersistenceManager == null) {
    		mPersistenceManager = new PersistenceManager(this);
    	}
    	return mPersistenceManager;
    }
    
    ///////////////////////
    @Override
    public void naviagteBackwards() {
    	getSupportFragmentManager().popBackStack();
    }
    
    @Override
	public void viewTrips() {
		getSupportFragmentManager().beginTransaction()
								   .replace(R.id.content_frame, TripFragment.newInstance())
								   .addToBackStack(null)
								   .commit();
	}
	
    @Override
	public void viewReceipts(TripRow trip) {
		getSupportFragmentManager().beginTransaction()
								   .replace(R.id.content_frame, ReceiptsFragment.newInstance(trip))
								   .addToBackStack(null)
								   .commit();
	}
	
    @Override
	public void viewReceiptImage(ReceiptRow receipt, TripRow trip) {
		getSupportFragmentManager().beginTransaction()
								   .replace(R.id.content_frame, ReceiptImageFragment.newInstance(receipt, trip))
								   .addToBackStack(null)
								   .commit();
	}
	
    @Override
	public void viewSettings() {
		getSettings().showSettingsMenu(this);
	}
	
    @Override
	public void viewCategories() {
		getSettings().showCategoriesMenu(this);
	}
	
    @Override
	public void viewAbout() {
		getSettings().showAbout(this);
	}
	
    @Override
	public void viewExport() {
		getSettings().showExport(this);
	}
    
    private Settings getSettings() {
    	if (mSettings == null) {
    		mSettings = new Settings();
    	}
    	return mSettings;
    }
	   
}
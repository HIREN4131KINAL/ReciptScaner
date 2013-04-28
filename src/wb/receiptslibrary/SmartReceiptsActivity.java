package wb.receiptslibrary;

import java.io.File;
import java.io.IOException;

import wb.android.async.BooleanTaskCompleteDelegate;
import wb.android.dialog.BetterDialogBuilder;
import wb.android.dialog.DirectDialogOnClickListener;
import wb.android.dialog.DirectLongLivedOnClickListener;
import wb.android.flex.Flex;
import wb.android.flex.Flexable;
import wb.android.storage.StorageManager;
import wb.android.util.AppRating;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
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
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wb.navigation.ViewController;
import com.wb.navigation.WBActivity;

public abstract class SmartReceiptsActivity extends WBActivity implements Flexable {
    
	//logging variables
    public static final boolean D = true;
    private static final String TAG = "SmartReceiptsActivity";
    
    //Camera Request Extras
    static final String STRING_DATA = "strData";
    static final int DIR = 0;
    static final int NAME = 1;
    
    //Receiver Settings
    protected static final String FILTER_ACTION = "wb.receiptslibrary";
    
    //AppRating
    private static final int LAUNCHES_UNTIL_PROMPT = 15;
    
    //Instace Vars
    private Uri _actionSendUri;
    private boolean _calledFromActionSend;
    
    //Package-Accessible Instance Variables. None of these require get/set to improve performance
    private StorageManager _sdCard;
    private DatabaseHelper _db; 
    protected Flex _flex;
    private ViewHolderFactory _factory;
    private Preferences _preferences;
	
	//Preference Identifiers - SubClasses Only
    protected static final String SUBCLASS_PREFS = "SubClassPrefs";
    protected static final String PREF1 = "pref1";
    
    @Override
	protected final ViewController buildController() {
		ViewController controller = new ViewController(this);
		_factory = new ViewHolderFactory(controller, this);
		return controller;
	}
    
    protected final void onCreate(final Bundle savedInstanceState, final RelativeLayout mainLayout, final ListView listView) {
    	if(D) Log.d(TAG, "onCreate");
    	super.onCreate(savedInstanceState);
        setResult(Activity.RESULT_CANCELED); //In case the user backs out
        _flex = Flex.getInstance(this, this);
        _sdCard = StorageManager.getInstance(this);
        _preferences = new Preferences(this);
        _calledFromActionSend = false;
	    _db = DatabaseHelper.getInstance(this);
        if (this.getIntent().getAction().equalsIgnoreCase(Intent.ACTION_SEND)) {
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
        _factory.buildHomeHolder(mainLayout, listView);
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	if (_sdCard == null)
    		_sdCard = StorageManager.getInstance(this);
    	if (_sdCard != null && !_sdCard.isExternal())
    		Toast.makeText(SmartReceiptsActivity.this, _flex.getString(R.string.SD_WARNING), Toast.LENGTH_LONG).show();
    }
    
    public abstract String getPackageName(); 
    
    @Override
    protected final void onResume() {
    	super.onResume();
    	_flex = Flex.getInstance(this, this);
    	_flex.onResume();
    	if (_calledFromActionSend) {
    		if (_preferences.showActionSendHelpDialog()) {
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
	    						_preferences.setShowActionSendHelpDialog(false);
	    						_preferences.commit();
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
    	if (_db != null) _db.onDestroy();
    }
    
    /**
     * This method is only called the first time Smart Receipts is run.
     * It gives a brief overview of how to use the app.
     */
    protected void onFirstRun() {
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
    void showCustomCSVMenu() {
    	final BetterDialogBuilder builder = new BetterDialogBuilder(this);
    	ScrollView scrollView = new ScrollView(this);
    	final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		final LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setGravity(Gravity.BOTTOM);
		layout.setPadding(6, 6, 6, 6);
		final CSVColumns csvColumns = _db.getCSVColumns(_flex); 
		for (int i=0; i < csvColumns.size(); i++) {
			final LinearLayout horiz = addHorizontalCSVLayoutItem(csvColumns, i);
			layout.addView(horiz, params);
		}
		scrollView.addView(layout);
		builder.setTitle("Customize CSV File")
			   .setView(scrollView)
			   .setCancelable(true)
			   .setLongLivedPositiveButton("Add Column", new DirectLongLivedOnClickListener<SmartReceiptsActivity>(this) {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						activity._db.insertCSVColumn();
						layout.addView(addHorizontalCSVLayoutItem(csvColumns, csvColumns.size() - 1), params);
					}
				})
				.setLongLivedNegativeButton("Remove Column", new DirectLongLivedOnClickListener<SmartReceiptsActivity>(this) {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (csvColumns.isEmpty())
							return;
						activity._db.deleteCSVColumn();
						layout.removeViews(csvColumns.size(), 1);
					}
				})
			   .show();
    }
    
    private final LinearLayout addHorizontalCSVLayoutItem(CSVColumns csvColumns, int i) {
		final LinearLayout horiz = new LinearLayout(this);
		final CSVColumnSelectionListener selectionListener = new CSVColumnSelectionListener(_db, i);
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
    
    protected void insertCSVDefaults(final DatabaseHelper db) { //Called in onCreate and onUpgrade
		db.insertCSVColumnNoCache(CSVColumns.CATEGORY_CODE(_flex));
		db.insertCSVColumnNoCache(CSVColumns.NAME(_flex));
		db.insertCSVColumnNoCache(CSVColumns.PRICE(_flex));
		db.insertCSVColumnNoCache(CSVColumns.CURRENCY(_flex));
		db.insertCSVColumnNoCache(CSVColumns.DATE(_flex));
	}
    
    protected void insertCategoryDefaults(final DatabaseHelper db) {
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
		db.insertCategoryNoCache("Stantionery/Stations", "ZSTS");
		db.insertCategoryNoCache("Training Fees", "ZTRN");
    }
    ///////////////////////////////////////////////////////////////////////////////////
    //Dynamic Shared Code (Make either static or multithreaded
    
    //If imageDestination == null, then it's set to the imageUri location
    final File transformNativeCameraBitmap(final Uri imageUri, final Intent data, Uri imageDestination) {
		// Move this all to a separate thread
		System.gc();
		Uri imageUriCopy;
		if (imageUri != null)
			imageUriCopy = Uri.parse(imageUri.toString());
		else {
			if (data != null)
				imageUriCopy = data.getData();
			else
				return null;
		}
		if (imageDestination == null)
			imageDestination = imageUriCopy;
		File imgFile = new File(imageDestination.getPath());
		final int maxDimension = 1024;
		BitmapFactory.Options fullOpts = new BitmapFactory.Options();
		fullOpts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imageUriCopy.getPath(), fullOpts);
		int fullWidth=fullOpts.outWidth, fullHeight=fullOpts.outHeight;
		fullOpts = null;
		int scale=1;
		while(fullWidth > maxDimension && fullHeight > maxDimension){
			fullWidth>>>=1;
			fullHeight>>>=1;
			scale<<=1;
		}
		BitmapFactory.Options smallerOpts = new BitmapFactory.Options();
		smallerOpts.inSampleSize=scale;
		System.gc();
		Bitmap endBitmap = BitmapFactory.decodeFile(imageUriCopy.getPath(), smallerOpts);
		if (!this.getStorageManager().writeBitmap(imageDestination, endBitmap, CompressFormat.JPEG, 85)) {
			Toast.makeText(this, "Error: The Image Failed to Save Properly", Toast.LENGTH_SHORT).show();
			imgFile = null;
		}
    	return imgFile;
    }
    
    final void deleteDuplicateGalleryImage() { //Deletes any gallery images that were taken within the last 5 seconds (i.e. duplicates)
    	try {
	    	final String[] imageColumns = { MediaStore.Images.Media._ID };
	    	Cursor c = MediaStore.Images.Media.query(this.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, MediaStore.Images.Media.DATE_TAKEN + " > " + (System.currentTimeMillis() - 5000), null); //Get all images that occured within the last 5secs
	        if(c.moveToFirst()){
	            int id = c.getInt(c.getColumnIndex(MediaStore.Images.Media._ID));
	            this.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media._ID + "=?", new String[]{ Integer.toString(id) } );
	        }
	        c.close();
    	}
    	catch (Exception e) { } //Ignore any errors
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////
    void naviagateBackwards() {
    	_factory.naviagateBackwards();
    }
    
    void navigateToShowReceiptImage(TripRow currentTrip, ReceiptRow currentReceipt) {
		_factory.buildReceiptImageViewHolder(currentTrip, currentReceipt);
	}
    
    
    ///////////////////////////////////////////////////////////////////////////////////
    
    Preferences getPreferences() {
    	return _preferences;
    }
    
    DatabaseHelper getDB() {
    	if (_db == null) _db = DatabaseHelper.getInstance(this);
    	return _db;
    }
    
    Flex getFlex() {
    	return _flex;
    }
    
    StorageManager getStorageManager() {
    	return _sdCard;
    }
    
    boolean calledFromActionSend() {
    	return _calledFromActionSend;
    }
    
    Uri actionSendUri() {
    	return _actionSendUri;
    }
    
    ///////////////////////
    // Utils
    void toastLong(int stringID) {
    	Toast.makeText(this, getFlex().getString(stringID), Toast.LENGTH_LONG).show();
    }
    
    void toastShort(int stringID) {
    	Toast.makeText(this, getFlex().getString(stringID), Toast.LENGTH_SHORT).show();
    }
	   
}
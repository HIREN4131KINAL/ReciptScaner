package wb.receiptslibrary.fragments;

import java.io.File;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;

import wb.android.autocomplete.AutoCompleteAdapter;
import wb.android.dialog.BetterDialogBuilder;
import wb.android.dialog.LongLivedOnClickListener;
import wb.android.google.camera.PhotoModule;
import wb.android.google.camera.app.GalleryApp;
import wb.receiptslibrary.BuildConfig;
import wb.receiptslibrary.R;
import wb.receiptslibrary.date.DateEditText;
import wb.receiptslibrary.legacycamera.MyCameraActivity;
import wb.receiptslibrary.model.ReceiptRow;
import wb.receiptslibrary.model.TripRow;
import wb.receiptslibrary.persistence.DatabaseHelper;
import wb.receiptslibrary.persistence.Preferences;
import wb.receiptslibrary.workers.EmailAssistant;
import wb.receiptslibrary.workers.ImageGalleryWorker;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.SQLException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;

public class ReceiptsFragment extends WBFragment implements DatabaseHelper.ReceiptRowListener {

	private static final String TAG = "ReceiptsFragment";
	
	//Activity Request ints
    private static final int NEW_RECEIPT_CAMERA_REQUEST = 1;
    private static final int ADD_PHOTO_CAMERA_REQUEST = 2;
    private static final int NATIVE_NEW_RECEIPT_CAMERA_REQUEST = 3;
    private static final int NATIVE_ADD_PHOTO_CAMERA_REQUEST = 4;
    
    //Preferences
  	private static final String PREFERENCES = "ReceiptsFragment.xml";
  	private static final String PREFERENCE_TRIP_NAME = "tripName";
  	private static final String PREFERENCE_HIGHLIGHTED_RECEIPT_ID = "highlightedReceiptId";
  	private static final String PREFERENCE_IMAGE_URI = "imageUri";
	
	private ReceiptAdapter mAdapter;
	private ReceiptRow mHighlightedReceipt;
	private TripRow mCurrentTrip;
	private Uri mImageUri;
	private AutoCompleteAdapter mAutoCompleteAdapter;
	private Date mCachedDate;
	
	// Cached views for autocomplete
	private AutoCompleteTextView mNameBox;
	private EditText mPriceBox;
	private Spinner mCategoriesSpinner;
	
	private static final String BUNDLE_TRIP_PARCEL_KEY = "wb.receiptslibrary.model.TripRow#Parcel";
	public static ReceiptsFragment newInstance(TripRow currentTrip) {
		ReceiptsFragment fragment = new ReceiptsFragment();
		Bundle args = new Bundle();
		args.putParcelable(BUNDLE_TRIP_PARCEL_KEY, currentTrip);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mAdapter = new ReceiptAdapter(this, new ReceiptRow[0]);
		getWorkerManager().getLogger().logInformation("/ReceiptView");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(getLayoutId(), container, false);
		ListView listView = (ListView) rootView.findViewById(R.id.listview);
		listView.setAdapter(mAdapter);
		return rootView;
	}
	
	public int getLayoutId() {
		return R.layout.listlayout;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (mAutoCompleteAdapter != null) {
			mAutoCompleteAdapter.onPause();
		}
		mCachedDate = null;
		if (mCurrentTrip != null) {
			// Save persistent data state
	    	SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCES, 0);
	    	SharedPreferences.Editor editor = preferences.edit();
	    	editor.putString(PREFERENCE_TRIP_NAME, mCurrentTrip.getName());
	    	final int id = (mHighlightedReceipt == null) ? -1 : mHighlightedReceipt.getId();
	    	editor.putInt(PREFERENCE_HIGHLIGHTED_RECEIPT_ID, id);
	    	final String uriPath = (mImageUri == null) ? null : mImageUri.toString();
	    	editor.putString(PREFERENCE_IMAGE_URI, uriPath);
	    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
	    		editor.apply();
	    	else
	    		editor.commit();
    	}
		getPersistenceManager().getDatabase().unregisterReceiptRowListener();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getPersistenceManager().getDatabase().registerReceiptRowListener(this);
		if (mCurrentTrip == null) {
			if (getArguments() != null) {
				Parcelable parcel = getArguments().getParcelable(BUNDLE_TRIP_PARCEL_KEY);
				if (parcel == null || !(parcel instanceof TripRow)) {
					restoreData();
				}
				else {
					setTrip((TripRow)parcel);
				}
			}
			else {
				restoreData();
			}
		}
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	// Restore persistent data
	private void restoreData() {
		final DatabaseHelper db = getPersistenceManager().getDatabase();
		SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCES, 0);
		final String tripName = preferences.getString(PREFERENCE_TRIP_NAME, "");
		mCurrentTrip = db.getTripByName(tripName);
    	final int receiptId = preferences.getInt(PREFERENCE_HIGHLIGHTED_RECEIPT_ID, -1);
    	if (receiptId > 0) {
    		mHighlightedReceipt = db.getReceiptByID(receiptId);
    	}
    	final String uriPath = preferences.getString(PREFERENCE_IMAGE_URI, "");
    	if (uriPath != null) {
    		mImageUri = Uri.parse(uriPath);
    	}
    	this.updateActionBarTitle();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		getPersistenceManager().getDatabase().unregisterReceiptRowListener();
	}
	
	public void setTrip(TripRow trip) {
		mCurrentTrip = trip;
		getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
		this.updateActionBarTitle();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			getNavigator().naviagteBackwards();
			return true;
		}
		else
			return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    	if (BuildConfig.DEBUG) Log.d(TAG, "Result Code: " + resultCode);
    	if (BuildConfig.DEBUG) Log.d(TAG, "Request Code: " + requestCode);
    	if (resultCode == Activity.RESULT_OK) { //-1
    		File imgFile = new File(mImageUri.getPath());
    		if (requestCode == NATIVE_NEW_RECEIPT_CAMERA_REQUEST || requestCode == NATIVE_ADD_PHOTO_CAMERA_REQUEST) {
    			final ImageGalleryWorker worker = getWorkerManager().getImageGalleryWorker();
    			worker.deleteDuplicateGalleryImage(); //Some devices duplicate the gallery images
    			imgFile = worker.transformNativeCameraBitmap(mImageUri, data, null);
    		}
			if (imgFile == null) {
				Toast.makeText(getSherlockActivity(), getFlexString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
				return;
			}
    		switch (requestCode) {
				case NATIVE_NEW_RECEIPT_CAMERA_REQUEST:
				case NEW_RECEIPT_CAMERA_REQUEST:
					receiptMenu(mCurrentTrip, null, imgFile);
				break;
				case NATIVE_ADD_PHOTO_CAMERA_REQUEST:
				case ADD_PHOTO_CAMERA_REQUEST:
					final ReceiptRow updatedReceipt = getPersistenceManager().getDatabase().updateReceiptImg(mHighlightedReceipt, imgFile);
					if (updatedReceipt != null) {
						getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
						Toast.makeText(getSherlockActivity(), "Receipt Image Successfully Added to " + mHighlightedReceipt.getName(), Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(getSherlockActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
						getPersistenceManager().getStorageManager().delete(imgFile); //Rollback
						return;
					}
				break;
				default:
					if (BuildConfig.DEBUG) Log.e(TAG, "Unrecognized Request Code: " + requestCode);
					super.onActivityResult(requestCode, resultCode, data);
				break;
			}
    	}
    	else if (resultCode == MyCameraActivity.PICTURE_SUCCESS) {  //51
	    	switch (requestCode) {
				case NEW_RECEIPT_CAMERA_REQUEST:
					receiptMenu(mCurrentTrip, null, new File(data.getStringExtra(MyCameraActivity.IMG_FILE)));
				break;
				case ADD_PHOTO_CAMERA_REQUEST:
					File img = new File(data.getStringExtra(MyCameraActivity.IMG_FILE));
					final ReceiptRow updatedReceipt = getPersistenceManager().getDatabase().updateReceiptImg(mHighlightedReceipt, img);
					if (updatedReceipt != null) {
						getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
						Toast.makeText(getSherlockActivity(), "Receipt Image Successfully Added to " + mHighlightedReceipt.getName(), Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(getSherlockActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
						return;
					}
				break;
				default:
					if (BuildConfig.DEBUG) Log.e(TAG, "Unrecognized Request Code: " + requestCode);
					super.onActivityResult(requestCode, resultCode, data);
				break;
			}
    	}
    	else if (resultCode == PhotoModule.RESULT_SAVE_FAILED) {
    		Toast.makeText(getSherlockActivity(), getFlexString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
			return;
    	}
    	else {
			if (BuildConfig.DEBUG) Log.e(TAG, "Unrecgonized Result Code: " + resultCode);
			List<String> errors = ((GalleryApp)getSherlockActivity().getApplication()).getErrorList();
	    	final int size = errors.size();
	    	for (int i=0; i < size; i++) {
	    		getWorkerManager().getLogger().logError(errors.get(i));
	    	}
	    	errors.clear();
			super.onActivityResult(requestCode, resultCode, data);
    	}
    }
	
	final void updateActionBarTitle() {
    	getSherlockActivity().getSupportActionBar().setTitle(mCurrentTrip.getCurrencyFormattedPrice() + " - " + mCurrentTrip.getName());
	}
	
	public final void addPictureReceipt() {
		String dirPath;
		File dir = mCurrentTrip.getDirectory();
		if (dir.exists())
			dirPath = dir.getAbsolutePath();
		else
			dirPath = getPersistenceManager().getStorageManager().mkdir(dir.getName()).getAbsolutePath();
		if (getPersistenceManager().getPreferences().useNativeCamera()) {
			final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			mImageUri = Uri.fromFile(new File(dirPath, System.currentTimeMillis() + "x" + getPersistenceManager().getDatabase().getReceiptsSerial(mCurrentTrip).length + ".jpg"));
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
			startActivityForResult(intent, NATIVE_NEW_RECEIPT_CAMERA_REQUEST);
		}
		else {
			if (wb.android.google.camera.common.ApiHelper.NEW_SR_CAMERA_IS_SUPPORTED) {
				final Intent intent = new Intent(getSherlockActivity(), wb.android.google.camera.CameraActivity.class);
				mImageUri = Uri.fromFile(new File(dirPath, System.currentTimeMillis() + "x" + getPersistenceManager().getDatabase().getReceiptsSerial(mCurrentTrip).length + ".jpg"));
				intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
				startActivityForResult(intent, NEW_RECEIPT_CAMERA_REQUEST);
			}
			else {
				final Intent intent = new Intent(getSherlockActivity(), MyCameraActivity.class);
				String[] strings  = new String[] {dirPath, System.currentTimeMillis() + "x" + getPersistenceManager().getDatabase().getReceiptsSerial(mCurrentTrip).length + ".jpg"};
				intent.putExtra(MyCameraActivity.STRING_DATA, strings);
				startActivityForResult(intent, NEW_RECEIPT_CAMERA_REQUEST);
			}
		}
	}
		    
	public final void addTextReceipt() {
		receiptMenu(mCurrentTrip ,null, null);
	}
		    
	public final void receiptMenu(final TripRow trip, final ReceiptRow receipt, final File img) {
		final boolean newReceipt = (receipt == null);
		final View scrollView = getFlex().getView(R.layout.dialog_receiptmenu);
		final AutoCompleteTextView nameBox = (AutoCompleteTextView) getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_NAME);
		final EditText priceBox = (EditText) getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_PRICE);
		final EditText taxBox = (EditText) getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_TAX);
		final Spinner currencySpinner = (Spinner) getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_CURRENCY);
		final DateEditText dateBox = (DateEditText) getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_DATE);
		final EditText commentBox = (EditText) getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_COMMENT);
		final Spinner categoriesSpinner =  (Spinner) getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_CATEGORY);
		final CheckBox expensable = (CheckBox) getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_EXPENSABLE);
		final CheckBox fullpage = (CheckBox) getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_FULLPAGE);
		
		//Extras
		final LinearLayout extras = (LinearLayout) getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_EXTRAS);
		final EditText extra_edittext_box_1 = (EditText) extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_1));
		final EditText extra_edittext_box_2 = (EditText) extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_2));
		final EditText extra_edittext_box_3 = (EditText) extras.findViewWithTag(getFlexString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_3));
		
		if (getPersistenceManager().getPreferences().includeTaxField()) {
			priceBox.setHint(getFlexString(R.string.DIALOG_RECEIPTMENU_HINT_PRICE_SHORT));
			taxBox.setVisibility(View.VISIBLE);
		}
		
		//Show default dictionary with auto-complete
		TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.SENTENCES);
		nameBox.setKeyListener(input);
		
		final ArrayAdapter<CharSequence> currenices = new ArrayAdapter<CharSequence>(getSherlockActivity(), android.R.layout.simple_spinner_item, getPersistenceManager().getDatabase().getCurrenciesList());
		currenices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		currencySpinner.setAdapter(currenices);
		dateBox.setFocusableInTouchMode(false); dateBox.setOnClickListener(getDateManager().getDateEditTextListener());
		final ArrayAdapter<CharSequence> categories = new ArrayAdapter<CharSequence>(getSherlockActivity(), android.R.layout.simple_spinner_item, getPersistenceManager().getDatabase().getCategoriesList());
		categories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categoriesSpinner.setAdapter(categories);
		
		if (newReceipt) {
			if (getPersistenceManager().getPreferences().enableAutoCompleteSuggestions()) {
				if (mAutoCompleteAdapter == null) {
					mAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getSherlockActivity(), getPersistenceManager().getDatabase(), DatabaseHelper.TAG_RECEIPTS);
				}
				else {
					mAutoCompleteAdapter.reset();
				}
				nameBox.setAdapter(mAutoCompleteAdapter);
				mNameBox = nameBox;
				mCategoriesSpinner = categoriesSpinner;
				mPriceBox = priceBox;
			}
			Time now = new Time(); now.setToNow();
			if (mCachedDate == null) dateBox.date = new Date(now.toMillis(false));
			else dateBox.date = mCachedDate;
			dateBox.setText(DateFormat.getDateFormat(getSherlockActivity()).format(dateBox.date));
			expensable.setChecked(true);
			Preferences preferences = getPersistenceManager().getPreferences();
			if (preferences.matchCommentToCategory() && preferences.matchNameToCategory()) categoriesSpinner.setOnItemSelectedListener(getSpinnerSelectionListener(nameBox, commentBox, categories));
			else if (preferences.matchCommentToCategory()) categoriesSpinner.setOnItemSelectedListener(getSpinnerSelectionListener(null, commentBox, categories));
			else if (preferences.matchNameToCategory()) categoriesSpinner.setOnItemSelectedListener(getSpinnerSelectionListener(nameBox, null, categories));
			if (preferences.predictCategories()) { //Predict Breakfast, Lunch, Dinner by the hour
				if (now.hour >= 4 && now.hour < 11) { //Breakfast hours
					int idx = categories.getPosition("Breakfast");
					if (idx > 0)
						categoriesSpinner.setSelection(idx);
				}
				else if (now.hour >= 11 && now.hour < 16) { //Lunch hours
					int idx = categories.getPosition("Lunch");
					if (idx > 0)
						categoriesSpinner.setSelection(idx);
				}
				else if (now.hour >= 16 && now.hour < 23) { //Dinner hours
					int idx = categories.getPosition("Dinner");
					if (idx > 0)
						categoriesSpinner.setSelection(idx);
				}
			}
			int idx = currenices.getPosition(preferences.getDefaultCurreny());
			if (idx > 0) currencySpinner.setSelection(idx);
		}
		else {
			if (receipt.getName().length() != 0) { nameBox.setText(receipt.getName()); }
			if (receipt.getPrice().length() != 0) { priceBox.setText(receipt.getPrice()); }
			if (receipt.getDate() != null) { dateBox.setText(receipt.getFormattedDate(getSherlockActivity())); dateBox.date = receipt.getDate(); }
			if (receipt.getCategory().length() != 0) { categoriesSpinner.setSelection(categories.getPosition(receipt.getCategory())); }
			if (receipt.getComment().length() != 0) { commentBox.setText(receipt.getComment()); }
			if (receipt.getTax().length() != 0) { taxBox.setText(receipt.getTax()); }
			int idx = currenices.getPosition(receipt.getCurrencyCode());
			if (idx > 0) currencySpinner.setSelection(idx);
			expensable.setChecked(receipt.isExpensable());
			fullpage.setChecked(receipt.isFullPage());
			if (extra_edittext_box_1 != null && receipt.hasExtraEditText1()) extra_edittext_box_1.setText(receipt.getExtraEditText1());
			if (extra_edittext_box_2 != null && receipt.hasExtraEditText2()) extra_edittext_box_2.setText(receipt.getExtraEditText2());
			if (extra_edittext_box_3 != null && receipt.hasExtraEditText3()) extra_edittext_box_3.setText(receipt.getExtraEditText3());
		}
		nameBox.setSelection(nameBox.getText().length()); //Put the cursor at the end
		
		//Show DialogController
		final BetterDialogBuilder builder = new BetterDialogBuilder(getSherlockActivity());
		builder.setTitle((newReceipt)?getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW):getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_EDIT))
			 .setCancelable(true)
			 .setView(scrollView)
			 .setLongLivedPositiveButton((newReceipt)?getFlexString(R.string.DIALOG_RECEIPTMENU_POSITIVE_BUTTON_CREATE):getFlexString(R.string.DIALOG_RECEIPTMENU_POSITIVE_BUTTON_UPDATE), new LongLivedOnClickListener() {
				 @Override
				 public void onClick(DialogInterface dialog, int whichButton) {
					 mNameBox = null; // Un-set
					 mCategoriesSpinner = null; // Un-set
					 mPriceBox = null; //Un-set
					 final String name = nameBox.getText().toString();
					 String price = priceBox.getText().toString();
					 String tax = taxBox.getText().toString();
					 final String category = categoriesSpinner.getSelectedItem().toString();
					 final String currency = currencySpinner.getSelectedItem().toString();
					 final String comment = commentBox.getText().toString();
					 final String extra_edittext_1 = (extra_edittext_box_1 == null) ? null : extra_edittext_box_1.getText().toString();
					 final String extra_edittext_2 = (extra_edittext_box_1 == null) ? null : extra_edittext_box_2.getText().toString();
					 final String extra_edittext_3 = (extra_edittext_box_1 == null) ? null : extra_edittext_box_3.getText().toString();
					 if (name.length() == 0) {
						 Toast.makeText(getSherlockActivity(), getFlexString(R.string.DIALOG_RECEIPTMENU_TOAST_MISSING_NAME), Toast.LENGTH_SHORT).show();
						 return;
					 }
					 if (dateBox.date == null) {
						 Toast.makeText(getSherlockActivity(), getFlexString(R.string.CALENDAR_TAB_ERROR), Toast.LENGTH_SHORT).show();
						 return;
					 }
					 Calendar receiptCalendar = Calendar.getInstance(); receiptCalendar.setTime(dateBox.date); 
					 receiptCalendar.set(Calendar.HOUR_OF_DAY, 0); receiptCalendar.set(Calendar.MINUTE, 0); receiptCalendar.set(Calendar.SECOND, 0); receiptCalendar.set(Calendar.MILLISECOND, 0);
					 Calendar tripCalendar = Calendar.getInstance(); tripCalendar.setTime(trip.getStartDate());
					 tripCalendar.set(Calendar.HOUR_OF_DAY, 0); tripCalendar.set(Calendar.MINUTE, 0); tripCalendar.set(Calendar.SECOND, 0); tripCalendar.set(Calendar.MILLISECOND, 0);
					 if (receiptCalendar.compareTo(tripCalendar) < 0) {
						 Toast.makeText(getSherlockActivity(), getFlexString(R.string.DIALOG_RECEIPTMENU_TOAST_BAD_DATE), Toast.LENGTH_LONG).show();
					 }
					 tripCalendar.setTime(trip.getEndDate());
					 tripCalendar.set(Calendar.HOUR_OF_DAY, 0); tripCalendar.set(Calendar.MINUTE, 0); tripCalendar.set(Calendar.SECOND, 0); tripCalendar.set(Calendar.MILLISECOND, 0);
					 if (receiptCalendar.compareTo(tripCalendar) > 0) {
						 Toast.makeText(getSherlockActivity(), getFlexString(R.string.DIALOG_RECEIPTMENU_TOAST_BAD_DATE), Toast.LENGTH_LONG).show();
					 }
					 if (newReceipt) {//Insert
						 getPersistenceManager().getDatabase().insertReceiptParallel(trip, img, name, category, dateBox.date, comment, price, tax, expensable.isChecked(), currency, fullpage.isChecked(), extra_edittext_1, extra_edittext_2, extra_edittext_3);
						 getDateManager().setDateEditTextListenerDialogHolder(null);
						 dialog.cancel();
					 }
					 else { //Update
						 if (price == null || price.length() == 0)
							 price = "0";
						 getPersistenceManager().getDatabase().updateReceiptParallel(receipt, trip, name, category, (dateBox.date == null) ? receipt.getDate() : dateBox.date, comment, price, tax, expensable.isChecked(), currency, fullpage.isChecked(), extra_edittext_1, extra_edittext_2, extra_edittext_3);
						 getDateManager().setDateEditTextListenerDialogHolder(null);
						 dialog.cancel();
					 }
				 }
			 })  
			 .setNegativeButton(getFlexString(R.string.DIALOG_RECEIPTMENU_NEGATIVE_BUTTON), new DialogInterface.OnClickListener() {
				 public void onClick(DialogInterface dialog, int which) {
					 if (img != null && newReceipt)
						 getPersistenceManager().getStorageManager().delete(img); //Clean Up On Cancel
					 getDateManager().setDateEditTextListenerDialogHolder(null);
					 dialog.cancel();   
				 }
			 });
		final AlertDialog dialog = builder.show();
		getDateManager().setDateEditTextListenerDialogHolder(dialog);
		if (newReceipt) {
			nameBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus && getSherlockActivity().getResources().getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
						dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			});
		}
		categoriesSpinner.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					InputMethodManager imm = (InputMethodManager)getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(nameBox.getWindowToken(), 0);
				}
				return false;
			}
		});
		currencySpinner.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					InputMethodManager imm = (InputMethodManager)getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(nameBox.getWindowToken(), 0);
				}
				return false;
			}
		});
	}
	
	public final void showMileage() {
    	final String milesString = mCurrentTrip.getMilesAsString();
    	final BetterDialogBuilder builder = new BetterDialogBuilder(getSherlockActivity());
    	final View linearLayout = getFlex().getView(R.layout.dialog_mileage);
		final EditText milesBox = (EditText) getFlex().getSubView(linearLayout, R.id.DIALOG_MILES_DELTA);
		builder.setTitle("Total: " + milesString)
			   .setCancelable(true)
			   .setView(linearLayout)
			   .setPositiveButton("Add", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   if (!getPersistenceManager().getDatabase().addMiles(mCurrentTrip, milesString, milesBox.getText().toString()))
		        		   Toast.makeText(getSherlockActivity(), "Bad Input", Toast.LENGTH_SHORT).show();
		        	   else
		        		   mAdapter.notifyDataSetChanged();
		           }
			   })
			   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   
		           }
			   })
			   .show();
    }
    
    public final boolean editReceipt(final ReceiptRow receipt) {
    	mHighlightedReceipt = receipt;
    	final BetterDialogBuilder builder = new BetterDialogBuilder(getSherlockActivity());
		builder.setTitle(receipt.getName())
			   .setCancelable(true)
			   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		if (getSmartReceiptsActivity().calledFromActionSend()) {
			if (receipt.getImage() == null) {
				final String[] actionSendNoImgEditReceiptItems = getFlex().getStringArray(R.array.ACTION_SEND_NOIMG_EDIT_RECEIPT_ITEMS);
				builder.setItems(actionSendNoImgEditReceiptItems, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						final String selection = actionSendNoImgEditReceiptItems[item].toString();
						if (selection == actionSendNoImgEditReceiptItems[0]) { //Attach Image to Receipt
							String dirPath;
							File dir = mCurrentTrip.getDirectory();
							if (dir.exists())
								dirPath = dir.getAbsolutePath();
							else
								dirPath = getPersistenceManager().getStorageManager().mkdir(dir.getName()).getAbsolutePath();
							File imgFile = getWorkerManager().getImageGalleryWorker().transformNativeCameraBitmap(getSmartReceiptsActivity().actionSendUri(), null, Uri.fromFile(new File(dirPath, receipt.getId() + "x.jpg")));
							if (imgFile != null) {
								Log.e(TAG, imgFile.getPath());
								final ReceiptRow updatedReceipt = getPersistenceManager().getDatabase().updateReceiptImg(receipt, imgFile);
								if (updatedReceipt != null) {
									getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
									Toast.makeText(getSherlockActivity(), "Receipt Image Successfully Added to " + mHighlightedReceipt.getName(), Toast.LENGTH_SHORT).show();
									getSherlockActivity().setResult(Activity.RESULT_OK, new Intent(Intent.ACTION_SEND, Uri.fromFile(imgFile)));
									getSherlockActivity().finish();
								}
								else {
									Toast.makeText(getSherlockActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
									getPersistenceManager().getStorageManager().delete(imgFile); //Rollback
									getSherlockActivity().finish();
									return;
								}
							}
							else {
								Toast.makeText(getSherlockActivity(), getFlexString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
								getSherlockActivity().finish();
							}
						}
					}
				});
			}
			else {
				final String[] actionSendImgEditReceiptItems = getFlex().getStringArray(R.array.ACTION_SEND_IMG_EDIT_RECEIPT_ITEMS);
				builder.setItems(actionSendImgEditReceiptItems, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						final String selection = actionSendImgEditReceiptItems[item].toString();
						if (selection == actionSendImgEditReceiptItems[0]) //View Image
							ReceiptsFragment.this.showImage(receipt);
						else if (selection == actionSendImgEditReceiptItems[1]) { //Attach Image to Receipt
							String dirPath;
							File dir = mCurrentTrip.getDirectory();
							if (dir.exists())
								dirPath = dir.getAbsolutePath();
							else
								dirPath = getPersistenceManager().getStorageManager().mkdir(dir.getName()).getAbsolutePath();
							File imgFile = getWorkerManager().getImageGalleryWorker().transformNativeCameraBitmap(getSmartReceiptsActivity().actionSendUri(), null, Uri.fromFile(new File(dirPath, receipt.getId() + "x.jpg")));
							if (imgFile != null) {
								Log.e(TAG, imgFile.getPath());
								final ReceiptRow retakeReceipt = getPersistenceManager().getDatabase().updateReceiptImg(mHighlightedReceipt, imgFile);
								if (retakeReceipt != null) {
									getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
									Toast.makeText(getSherlockActivity(), "Receipt Image Successfully Replaced for " + mHighlightedReceipt.getName(), Toast.LENGTH_SHORT).show();
									getSherlockActivity().setResult(Activity.RESULT_OK, new Intent(Intent.ACTION_SEND, Uri.fromFile(imgFile)));
									getSherlockActivity().finish();
								}
								else {
									Toast.makeText(getSherlockActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
									//Add overwrite rollback here
									getSherlockActivity().finish();
								}
							}
							else {
								Toast.makeText(getSherlockActivity(), getFlexString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
								getSherlockActivity().finish();
							}
						}
					}
				});
			}
		}
		else {
			if (receipt.getImage() == null) {
				final String[] noImgEditReceiptItems = getFlex().getStringArray(R.array.NOIMG_EDIT_RECEIPT_ITEMS);
				builder.setItems(noImgEditReceiptItems, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
				    	final String selection = noImgEditReceiptItems[item].toString();
				    	if (selection == noImgEditReceiptItems[0]) //Edit Receipt
				    		ReceiptsFragment.this.receiptMenu(mCurrentTrip, receipt, null);
				    	else if (selection == noImgEditReceiptItems[1]) { //Take Photo
							String dirPath;
							File dir = mCurrentTrip.getDirectory();
							if (dir.exists())
								dirPath = dir.getAbsolutePath();
							else
								dirPath = getPersistenceManager().getStorageManager().mkdir(dir.getName()).getAbsolutePath();
				    		if (getPersistenceManager().getPreferences().useNativeCamera()) {
				            	final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				                mImageUri = Uri.fromFile(new File(dirPath, receipt.getId() + "x.jpg"));
				                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
				                startActivityForResult(intent, NATIVE_ADD_PHOTO_CAMERA_REQUEST);
				        	}
				        	else {
				        		if (wb.android.google.camera.common.ApiHelper.NEW_SR_CAMERA_IS_SUPPORTED) {
				    				final Intent intent = new Intent(getSherlockActivity(), wb.android.google.camera.CameraActivity.class);
				    				mImageUri = Uri.fromFile(new File(dirPath, receipt.getId() + "x.jpg"));
				    				intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
				    				startActivityForResult(intent, ADD_PHOTO_CAMERA_REQUEST);
				    			}
				    			else {
									final Intent intent = new Intent(getSherlockActivity(), MyCameraActivity.class);
									String[] strings  = new String[] {dirPath, receipt.getId() + "x.jpg"};
									intent.putExtra(MyCameraActivity.STRING_DATA, strings);
									startActivityForResult(intent, ADD_PHOTO_CAMERA_REQUEST);
				    			}
				        	}
				    	}
				    	else if (selection == noImgEditReceiptItems[2]) //Delete Receipt
				    		ReceiptsFragment.this.deleteReceipt(receipt);
				    	else if (selection == noImgEditReceiptItems[3]) //Move Up
				    		ReceiptsFragment.this.moveReceiptUp(receipt);
				    	else if (selection == noImgEditReceiptItems[4]) //Move Down
				    		ReceiptsFragment.this.moveReceiptDown(receipt);
				    	dialog.cancel();
				    }
				});
			}
			else {
				final String[] imgEditReceiptItems = getFlex().getStringArray(R.array.IMG_EDIT_RECEIPT_ITEMS);
				builder.setItems(imgEditReceiptItems, new DialogInterface.OnClickListener() {
					@Override
				    public void onClick(DialogInterface dialog, int item) {
				    	final String selection = imgEditReceiptItems[item].toString();
				    	if (selection == imgEditReceiptItems[0]) //Edit Receipt
				    		ReceiptsFragment.this.receiptMenu(mCurrentTrip, receipt, receipt.getImage());
				    	else if (selection == imgEditReceiptItems[1]) //View/Retake Image 
				    		ReceiptsFragment.this.showImage(receipt);
				    	else if (selection == imgEditReceiptItems[2]) //Delete Receipt
				    		ReceiptsFragment.this.deleteReceipt(receipt);
				    	else if (selection == imgEditReceiptItems[3]) //Move Up
				    		ReceiptsFragment.this.moveReceiptUp(receipt);
				    	else if (selection == imgEditReceiptItems[4]) //Move Down
				    		ReceiptsFragment.this.moveReceiptDown(receipt);
				    	dialog.cancel();
				    }
				});
			}
		}
		builder.show();
    	return true;
    }
    
    public void emailTrip() {
    	EmailAssistant assistant = new EmailAssistant(getSmartReceiptsActivity(), mCurrentTrip);
    	assistant.emailTrip();
    }
    
    private final void showImage(final ReceiptRow receipt) {
    	getNavigator().viewReceiptImage(receipt, mCurrentTrip);
    }
    
    public final void deleteReceipt(final ReceiptRow receipt) {
    	final BetterDialogBuilder builder = new BetterDialogBuilder(getSherlockActivity());
		builder.setTitle("Delete " + receipt.getName() + "?")
			   .setCancelable(true)
			   .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                getPersistenceManager().getDatabase().deleteReceiptParallel(receipt, mCurrentTrip);
		           }
		       })
		       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       })
		       .show();
    }
    
    
    final void moveReceiptUp(final ReceiptRow receipt) {
    	getPersistenceManager().getDatabase().moveReceiptUp(mCurrentTrip, receipt);
		getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
    }
    
    final void moveReceiptDown(final ReceiptRow receipt) {
    	getPersistenceManager().getDatabase().moveReceiptDown(mCurrentTrip, receipt);
		getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
    }
    
    public final String getMilesString() {
    	return mCurrentTrip.getMilesAsString();
    }

	@Override
	public void onReceiptRowsQuerySuccess(ReceiptRow[] receipts) {
		mAdapter.notifyDataSetChanged(receipts);
		updateActionBarTitle();
	}

	@Override
	public void onReceiptRowInsertSuccess(ReceiptRow receipt) {
		getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
	}

	@Override
	public void onReceiptRowInsertFailure(SQLException ex) {
		Toast.makeText(getSherlockActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onReceiptRowUpdateSuccess(ReceiptRow receipt) {
		getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
		ReceiptsFragment.this.updateActionBarTitle();
	}

	@Override
	public void onReceiptRowUpdateFailure() {
		Toast.makeText(getSherlockActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();		
	}
	
	@Override
	public void onReceiptRowAutoCompleteQueryResult(String name, String price, String category) {
		if (mNameBox != null && name != null) {
			mNameBox.setText(name);
			mNameBox.setSelection(name.length());
		}
		if (mPriceBox != null && price != null && mPriceBox.getText().length() == 0) {
			mPriceBox.setText(price);
		}
		if (mCategoriesSpinner != null && category != null) {
			mCategoriesSpinner.setSelection(getPersistenceManager().getDatabase().getCategoriesList().indexOf(category));
		}
	}

	@Override
	public void onReceiptDeleteSuccess(ReceiptRow receipt) {
		getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
    	if (receipt.getImage() != null) {
    		if (!getPersistenceManager().getStorageManager().delete(receipt.getImage()))
    			Toast.makeText(getSherlockActivity(), getFlexString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
    	}
    	if (!receipt.isPriceEmpty()) {
			ReceiptsFragment.this.updateActionBarTitle();
    	}
	}

	@Override
	public void onReceiptDeleteFailure() {
		Toast.makeText(getSherlockActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();		
	}
	
    public OnItemSelectedListener getSpinnerSelectionListener(TextView nameBox, TextView commentBox, ArrayAdapter<CharSequence> categories) {
    	return new SpinnerSelectionListener(nameBox, commentBox, categories);
    }
    
	private final class SpinnerSelectionListener implements OnItemSelectedListener {
		
		private final TextView sNameBox, sCommentBox;
		private final ArrayAdapter<CharSequence> sCategories;
		
		public SpinnerSelectionListener(TextView nameBox, TextView commentBox, ArrayAdapter<CharSequence> categories) {
			sNameBox = nameBox; 
			sCommentBox = commentBox; 
			sCategories = categories;
		}
		
		@Override 
		public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
			if (sNameBox != null) 
				sNameBox.setText(sCategories.getItem(position));
			if (sCommentBox != null) 
				sCommentBox.setText(sCategories.getItem(position)); 
		}
		
		@Override 
		public void onNothingSelected(AdapterView<?> arg0) {}	
	}
	
}

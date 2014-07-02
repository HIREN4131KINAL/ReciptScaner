package co.smartreceipts.android.fragments;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.util.List;

import wb.android.autocomplete.AutoCompleteAdapter;
import wb.android.dialog.BetterDialogBuilder;
import wb.android.dialog.LongLivedOnClickListener;
import wb.android.google.camera.PhotoModule;
import wb.android.google.camera.app.GalleryApp;
import wb.android.loaders.SharedPreferencesLoader;
import android.annotation.TargetApi;
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
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.Attachable;
import co.smartreceipts.android.activities.ReceiptImageActivity;
import co.smartreceipts.android.activities.ReceiptPDFActivity;
import co.smartreceipts.android.adapters.ReceiptCardAdapter;
import co.smartreceipts.android.adapters.TaxAutoCompleteAdapter;
import co.smartreceipts.android.date.DateEditText;
import co.smartreceipts.android.legacycamera.MyCameraActivity;
import co.smartreceipts.android.model.Attachment;
import co.smartreceipts.android.model.ReceiptRow;
import co.smartreceipts.android.model.TripRow;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.utils.Utils;
import co.smartreceipts.android.workers.EmailAssistant;
import co.smartreceipts.android.workers.ImageGalleryWorker;

public class ReceiptsListFragment extends ReceiptsFragment implements DatabaseHelper.ReceiptRowListener,
																	  LoaderCallbacks<SharedPreferences> {

	public static final String TAG = "ReceiptsListFragment";

	//Activity Request ints
    private static final int NEW_RECEIPT_CAMERA_REQUEST = 1;
    private static final int ADD_PHOTO_CAMERA_REQUEST = 2;
    private static final int NATIVE_NEW_RECEIPT_CAMERA_REQUEST = 3;
    private static final int NATIVE_ADD_PHOTO_CAMERA_REQUEST = 4;

    //Preferences
  	private static final String PREFERENCE_HIGHLIGHTED_RECEIPT_ID = "highlightedReceiptId";
  	private static final String PREFERENCE_IMAGE_URI = "imageUri";

  	private ReceiptCardAdapter mAdapter;
	private ReceiptRow mHighlightedReceipt;
	private Uri mImageUri;
	private AutoCompleteAdapter mReceiptsNameAutoCompleteAdapter, mReceiptsCommentAutoCompleteAdapter;
	private Date mCachedDate;
	private String mCachedCategory;
	private Time mNow;
	private ProgressBar mProgressDialog;
	private TextView mNoDataAlert;
	private Attachable mAttachable;
	private View mAdView;
	
	private boolean mShowDialogOnResume = false;
	private File mImageFile;

	// Cached views for autocomplete
	private AutoCompleteTextView mNameBox;
	private EditText mPriceBox;
	private Spinner mCategoriesSpinner;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof Attachable) {
			mAttachable = (Attachable) activity;
		}
		else {
			throw new ClassCastException("The ReceiptFragment's Activity must extend the Attachable interfaces");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new ReceiptCardAdapter(getActivity(), getPersistenceManager().getPreferences());
		getLoaderManager().initLoader(getResources().getInteger(R.integer.receipts_list_fragment_loader), null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onCreateView");
		}
		View rootView = inflater.inflate(getLayoutId(), container, false);
		mProgressDialog = (ProgressBar) rootView.findViewById(R.id.progress);
		mNoDataAlert = (TextView) rootView.findViewById(R.id.no_data);
		mAdView = getWorkerManager().getAdManager().onCreateAd(rootView);
		View.OnClickListener listener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final int id = v.getId();
				if (id == R.id.receipt_action_camera) {
					addPictureReceipt();
				}
				else if (id == R.id.receipt_action_text) {
					addTextReceipt();
				}
				else if (id == R.id.receipt_action_distance) {
					showMileage();
				}
				else if (id == R.id.receipt_action_send) {
					emailTrip();
				}
			}
		};
		rootView.findViewById(R.id.receipt_action_camera).setOnClickListener(listener);
		rootView.findViewById(R.id.receipt_action_text).setOnClickListener(listener);
		rootView.findViewById(R.id.receipt_action_distance).setOnClickListener(listener);
		rootView.findViewById(R.id.receipt_action_send).setOnClickListener(listener);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(mAdapter); //Set this here to ensure this has been laid out already
	}

	public int getLayoutId() {
		return R.layout.receipt_fragment_layout;
	}

	@Override
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void onPause() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onPause");
		}
		if (mReceiptsNameAutoCompleteAdapter != null) {
			mReceiptsNameAutoCompleteAdapter.onPause();
		}
		if (mReceiptsCommentAutoCompleteAdapter != null) {
			mReceiptsCommentAutoCompleteAdapter.onPause();
		}
		mCachedDate = null;
		mCachedCategory = null;
		if (mCurrentTrip != null) {
			// Save persistent data state
	    	SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCES, 0);
	    	SharedPreferences.Editor editor = preferences.edit();
	    	final int id = (mHighlightedReceipt == null) ? -1 : mHighlightedReceipt.getId();
	    	editor.putInt(PREFERENCE_HIGHLIGHTED_RECEIPT_ID, id);
	    	final String uriPath = (mImageUri == null) ? null : mImageUri.toString();
	    	editor.putString(PREFERENCE_IMAGE_URI, uriPath);
	    	if (Utils.ApiHelper.hasGingerbread()) {
				editor.apply();
			} else {
				editor.commit();
			}
    	}
		getWorkerManager().getAdManager().onAdPaused(mAdView);
		getPersistenceManager().getDatabase().unregisterReceiptRowListener();
		super.onPause();
	}

	@Override
	public void onResume() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onResume");
		}
		super.onResume();
		getWorkerManager().getAdManager().onAdResumed(mAdView);
		getPersistenceManager().getDatabase().registerReceiptRowListener(this);
		getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
		if (mShowDialogOnResume) {
			receiptMenu(mCurrentTrip, null, mImageFile);
			mShowDialogOnResume = false;
		}
	}

	// Restore persistent data (Called serially)
	@Override
	protected void restoreData() {
		super.restoreData();
		restoreDataHelper(getActivity().getSharedPreferences(PREFERENCES, 0));
	}

	private void restoreDataHelper(SharedPreferences preferences) {
		if (mHighlightedReceipt == null) { // Attempt to Restore
			final DatabaseHelper db = getPersistenceManager().getDatabase();
	    	final int receiptId = preferences.getInt(PREFERENCE_HIGHLIGHTED_RECEIPT_ID, -1);
	    	if (receiptId > 0) {
	    		mHighlightedReceipt = db.getReceiptByID(receiptId);
	    	}
		}
		if (mImageUri == null) { // Attempt to Restore
	    	final String uriPath = preferences.getString(PREFERENCE_IMAGE_URI, "");
	    	if (uriPath != null) {
	    		mImageUri = Uri.parse(uriPath);
	    	}
		}
	}

	@Override
	public void onDestroy() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onDestroy");
		}
		getWorkerManager().getAdManager().onAdDestroyed(mAdView);
		getPersistenceManager().getDatabase().unregisterReceiptRowListener();
		super.onDestroy();
	}

	@Override
	public Loader<SharedPreferences> onCreateLoader(int id, Bundle args) {
		return new SharedPreferencesLoader(getActivity(), PREFERENCES);
	}

	@Override
	public void onLoadFinished(Loader<SharedPreferences> loader, SharedPreferences prefs) {
		restoreDataHelper(prefs);
	}

	@Override
	public void onLoaderReset(Loader<SharedPreferences> loader) {
		// Unused
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    	if (BuildConfig.DEBUG) {
			Log.d(TAG, "Result Code: " + resultCode);
		}
    	if (BuildConfig.DEBUG) {
			Log.d(TAG, "Request Code: " + requestCode);
		}
    	
    	// Need to make this call here, since users with "Don't keep activities" will hit this call
    	// before any of onCreate/onStart/onResume is called. This should restore our current trip (what
    	// onResume() would normally do to prevent a variety of crashes that we might encounter
    	ensureValidCurrentTrip();
    	
    	if (resultCode == Activity.RESULT_OK) { //-1
    		restoreDataHelper(getActivity().getSharedPreferences(PREFERENCES, 0)); //Added here since onActivityResult is called before onResume
    		File imgFile = (mImageUri != null) ? new File(mImageUri.getPath()) : null;
    		if (requestCode == NATIVE_NEW_RECEIPT_CAMERA_REQUEST || requestCode == NATIVE_ADD_PHOTO_CAMERA_REQUEST) {
    			final ImageGalleryWorker worker = getWorkerManager().getImageGalleryWorker();
    			worker.deleteDuplicateGalleryImage(); //Some devices duplicate the gallery images
    			imgFile = worker.transformNativeCameraBitmap(mImageUri, data, null);
    		}
			if (imgFile == null) {
				Toast.makeText(getActivity(), getFlexString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
				return;
			}
    		switch (requestCode) {
				case NATIVE_NEW_RECEIPT_CAMERA_REQUEST:
				case NEW_RECEIPT_CAMERA_REQUEST:
					if (this.isResumed()) {
						receiptMenu(mCurrentTrip, null, imgFile);
					}
					else {
						mShowDialogOnResume = true;
						mImageFile = imgFile;
					}
				break;
				case NATIVE_ADD_PHOTO_CAMERA_REQUEST:
				case ADD_PHOTO_CAMERA_REQUEST:
					final ReceiptRow updatedReceipt = getPersistenceManager().getDatabase().updateReceiptFile(mHighlightedReceipt, imgFile);
					if (updatedReceipt != null) {
						getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
						Toast.makeText(getActivity(), "Receipt Image Successfully Added to " + mHighlightedReceipt.getName(), Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(getActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
						getPersistenceManager().getStorageManager().delete(imgFile); //Rollback
						return;
					}
				break;
				default:
					if (BuildConfig.DEBUG) {
						Log.e(TAG, "Unrecognized Request Code: " + requestCode);
					}
					super.onActivityResult(requestCode, resultCode, data);
				break;
			}
    	}
    	else if (resultCode == MyCameraActivity.PICTURE_SUCCESS) {  //51
    		restoreDataHelper(getActivity().getSharedPreferences(PREFERENCES, 0)); //Added here since onActivityResult is called before onResume
	    	switch (requestCode) {
				case NEW_RECEIPT_CAMERA_REQUEST:
					File imgFile = new File(data.getStringExtra(MyCameraActivity.IMG_FILE));
					if (this.isResumed()) {
						receiptMenu(mCurrentTrip, null, imgFile);
					}
					else {
						mShowDialogOnResume = true;
						mImageFile = imgFile;
					}
				break;
				case ADD_PHOTO_CAMERA_REQUEST:
					File img = new File(data.getStringExtra(MyCameraActivity.IMG_FILE));
					final ReceiptRow updatedReceipt = getPersistenceManager().getDatabase().updateReceiptFile(mHighlightedReceipt, img);
					if (updatedReceipt != null) {
						getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
						Toast.makeText(getActivity(), "Receipt Image Successfully Added to " + mHighlightedReceipt.getName(), Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(getActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
						return;
					}
				break;
				default:
					if (BuildConfig.DEBUG) {
						Log.e(TAG, "Unrecognized Request Code: " + requestCode);
					}
					super.onActivityResult(requestCode, resultCode, data);
				break;
			}
    	}
    	else if (resultCode == PhotoModule.RESULT_SAVE_FAILED) {
    		Toast.makeText(getActivity(), getFlexString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
			return;
    	}
    	else {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Unrecgonized Result Code: " + resultCode);
			}
			List<String> errors = ((GalleryApp)getActivity().getApplication()).getErrorList();
	    	final int size = errors.size();
	    	for (int i=0; i < size; i++) {
	    		getWorkerManager().getLogger().logError(errors.get(i));
	    	}
	    	errors.clear();
			super.onActivityResult(requestCode, resultCode, data);
    	}
    }

	public final void addPictureReceipt() {
		String dirPath;
		File dir = mCurrentTrip.getDirectory();
		if (dir.exists()) {
			dirPath = dir.getAbsolutePath();
		} else {
			dirPath = getPersistenceManager().getStorageManager().mkdir(dir.getName()).getAbsolutePath();
		}
		if (getPersistenceManager().getPreferences().useNativeCamera()) {
			final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			mImageUri = Uri.fromFile(new File(dirPath, System.currentTimeMillis() + "x" + getPersistenceManager().getDatabase().getReceiptsSerial(mCurrentTrip).length + ".jpg"));
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
			startActivityForResult(intent, NATIVE_NEW_RECEIPT_CAMERA_REQUEST);
		}
		else {
			if (wb.android.google.camera.common.ApiHelper.NEW_SR_CAMERA_IS_SUPPORTED) {
				final Intent intent = new Intent(getActivity(), wb.android.google.camera.CameraActivity.class);
				mImageUri = Uri.fromFile(new File(dirPath, System.currentTimeMillis() + "x" + getPersistenceManager().getDatabase().getReceiptsSerial(mCurrentTrip).length + ".jpg"));
				intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
				startActivityForResult(intent, NEW_RECEIPT_CAMERA_REQUEST);
			}
			else {
				final Intent intent = new Intent(getActivity(), MyCameraActivity.class);
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
		final View scrollView = getFlex().getView(getActivity(), R.layout.dialog_receiptmenu);
		final AutoCompleteTextView nameBox = (AutoCompleteTextView) getFlex().getSubView(getActivity(), scrollView, R.id.DIALOG_RECEIPTMENU_NAME);
		final EditText priceBox = (EditText) getFlex().getSubView(getActivity(), scrollView, R.id.DIALOG_RECEIPTMENU_PRICE);
		final AutoCompleteTextView taxBox = (AutoCompleteTextView) getFlex().getSubView(getActivity(), scrollView, R.id.DIALOG_RECEIPTMENU_TAX);
		final Spinner currencySpinner = (Spinner) getFlex().getSubView(getActivity(), scrollView, R.id.DIALOG_RECEIPTMENU_CURRENCY);
		final DateEditText dateBox = (DateEditText) getFlex().getSubView(getActivity(), scrollView, R.id.DIALOG_RECEIPTMENU_DATE);
		final AutoCompleteTextView commentBox = (AutoCompleteTextView) getFlex().getSubView(getActivity(), scrollView, R.id.DIALOG_RECEIPTMENU_COMMENT);
		final Spinner categoriesSpinner =  (Spinner) getFlex().getSubView(getActivity(), scrollView, R.id.DIALOG_RECEIPTMENU_CATEGORY);
		final CheckBox expensable = (CheckBox) getFlex().getSubView(getActivity(), scrollView, R.id.DIALOG_RECEIPTMENU_EXPENSABLE);
		final CheckBox fullpage = (CheckBox) getFlex().getSubView(getActivity(), scrollView, R.id.DIALOG_RECEIPTMENU_FULLPAGE);

		//Extras
		final LinearLayout extras = (LinearLayout) getFlex().getSubView(getActivity(), scrollView, R.id.DIALOG_RECEIPTMENU_EXTRAS);
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

		final ArrayAdapter<CharSequence> currenices = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, getPersistenceManager().getDatabase().getCurrenciesList());
		currenices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		currencySpinner.setAdapter(currenices);

		dateBox.setFocusableInTouchMode(false); dateBox.setOnClickListener(getDateManager().getDateEditTextListener());
		final ArrayAdapter<CharSequence> categories = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, getPersistenceManager().getDatabase().getCategoriesList());
		categories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categoriesSpinner.setAdapter(categories);

		if (newReceipt) {
			if (getPersistenceManager().getPreferences().enableAutoCompleteSuggestions()) {
				final DatabaseHelper db = getPersistenceManager().getDatabase();
				if (mReceiptsNameAutoCompleteAdapter == null) {
					mReceiptsNameAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getActivity(), DatabaseHelper.TAG_RECEIPTS_NAME, db, db);
				}
				else {
					mReceiptsNameAutoCompleteAdapter.reset();
				}
				if (mReceiptsCommentAutoCompleteAdapter == null) {
					mReceiptsCommentAutoCompleteAdapter = AutoCompleteAdapter.getInstance(getActivity(), DatabaseHelper.TAG_RECEIPTS_COMMENT, db);
				}
				else {
					mReceiptsCommentAutoCompleteAdapter.reset();
				}
				nameBox.setAdapter(mReceiptsNameAutoCompleteAdapter);
				commentBox.setAdapter(mReceiptsCommentAutoCompleteAdapter);
				mNameBox = nameBox;
				mCategoriesSpinner = categoriesSpinner;
				mPriceBox = priceBox;
			}
			if (getPersistenceManager().getPreferences().includeTaxField()) {
				taxBox.setAdapter(new TaxAutoCompleteAdapter(getActivity(), priceBox, taxBox, getPersistenceManager().getPreferences(), getPersistenceManager().getPreferences().getDefaultTaxPercentage()));
			}
			mNow = new Time();
			mNow.setToNow();
			if (mCachedDate == null) {
				if (getPersistenceManager().getPreferences().defaultToFirstReportDate()) {
					dateBox.date = mCurrentTrip.getStartDate();
				}
				else {
					dateBox.date = new Date(mNow.toMillis(false));
				}
			}
			else {
				dateBox.date = mCachedDate;
			}
			dateBox.setText(DateFormat.getDateFormat(getActivity()).format(dateBox.date));
			expensable.setChecked(true);
			Preferences preferences = getPersistenceManager().getPreferences();
			if (preferences.matchCommentToCategory() && preferences.matchNameToCategory()) {
				categoriesSpinner.setOnItemSelectedListener(getSpinnerSelectionListener(nameBox, commentBox, categories));
			} else if (preferences.matchCommentToCategory()) {
				categoriesSpinner.setOnItemSelectedListener(getSpinnerSelectionListener(null, commentBox, categories));
			} else if (preferences.matchNameToCategory()) {
				categoriesSpinner.setOnItemSelectedListener(getSpinnerSelectionListener(nameBox, null, categories));
			}
			if (preferences.predictCategories()) { //Predict Breakfast, Lunch, Dinner by the hour
				if (mCachedCategory == null) {
					if (mNow.hour >= 4 && mNow.hour < 11) { //Breakfast hours
						int idx = categories.getPosition(getString(R.string.category_breakfast));
						if (idx > 0) {
							categoriesSpinner.setSelection(idx);
						}
					}
					else if (mNow.hour >= 11 && mNow.hour < 16) { //Lunch hours
						int idx = categories.getPosition(getString(R.string.category_lunch));
						if (idx > 0) {
							categoriesSpinner.setSelection(idx);
						}
					}
					else if (mNow.hour >= 16 && mNow.hour < 23) { //Dinner hours
						int idx = categories.getPosition(getString(R.string.category_dinner));
						if (idx > 0) {
							categoriesSpinner.setSelection(idx);
						}
					}
				}
				else {
					int idx = categories.getPosition(mCachedCategory);
					if (idx > 0) {
						categoriesSpinner.setSelection(idx);
					}
				}
			}
			int idx = currenices.getPosition((mCurrentTrip != null) ? mCurrentTrip.getDefaultCurrencyCode() : preferences.getDefaultCurreny());
			if (idx > 0) {
				currencySpinner.setSelection(idx);
			}
			fullpage.setChecked(preferences.shouldDefaultToFullPage());
		}
		else {
			if (!TextUtils.isEmpty(receipt.getName())) { nameBox.setText(receipt.getName()); }
			if (!TextUtils.isEmpty(receipt.getPrice())) { priceBox.setText(receipt.getPrice()); }
			if (receipt.getDate() != null) { dateBox.setText(receipt.getFormattedDate(getActivity(), getPersistenceManager().getPreferences().getDateSeparator())); dateBox.date = receipt.getDate(); }
			if (!TextUtils.isEmpty(receipt.getCategory())) { categoriesSpinner.setSelection(categories.getPosition(receipt.getCategory())); }
			if (!TextUtils.isEmpty(receipt.getComment())) { commentBox.setText(receipt.getComment()); }
			if (!TextUtils.isEmpty(receipt.getTax())) { taxBox.setText(receipt.getTax()); }
			int idx = currenices.getPosition(receipt.getCurrencyCode());
			if (idx > 0) {
				currencySpinner.setSelection(idx);
			}
			expensable.setChecked(receipt.isExpensable());
			fullpage.setChecked(receipt.isFullPage());
			if (extra_edittext_box_1 != null && receipt.hasExtraEditText1()) {
				extra_edittext_box_1.setText(receipt.getExtraEditText1());
			}
			if (extra_edittext_box_2 != null && receipt.hasExtraEditText2()) {
				extra_edittext_box_2.setText(receipt.getExtraEditText2());
			}
			if (extra_edittext_box_3 != null && receipt.hasExtraEditText3()) {
				extra_edittext_box_3.setText(receipt.getExtraEditText3());
			}
		}
		nameBox.setSelection(nameBox.getText().length()); //Put the cursor at the end

		//Show DialogController
		final BetterDialogBuilder builder = new BetterDialogBuilder(getActivity());
		final String title;
		if (newReceipt) {
			if (getPersistenceManager().getPreferences().isShowReceiptID()) {
				title = String.format(getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW_ID), getPersistenceManager().getDatabase().getNextReceiptAutoIncremenetIdSerial());
			}
			else {
				title = getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW);
			}
		}
		else {
			if (getPersistenceManager().getPreferences().isShowReceiptID()) {
				title = String.format(getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_EDIT_ID), receipt.getId());
			}
			else {
				title = getFlexString(R.string.DIALOG_RECEIPTMENU_TITLE_EDIT);
			}
		}
		builder.setTitle(title)
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
						 Toast.makeText(getActivity(), getFlexString(R.string.DIALOG_RECEIPTMENU_TOAST_MISSING_NAME), Toast.LENGTH_SHORT).show();
						 return;
					 }
					 if (dateBox.date == null) {
						 Toast.makeText(getActivity(), getFlexString(R.string.CALENDAR_TAB_ERROR), Toast.LENGTH_SHORT).show();
						 return;
					 }
					 else if (!dateBox.date.equals(mNow)){
						 mCachedDate = (Date) dateBox.date.clone();
					 }
					 mCachedCategory = category;

					 if (!mCurrentTrip.isDateInsideTripBounds(dateBox.date)) {
						 Toast.makeText(getActivity(), getFlexString(R.string.DIALOG_RECEIPTMENU_TOAST_BAD_DATE), Toast.LENGTH_LONG).show();
					 }

					 if (newReceipt) {//Insert
						 getPersistenceManager().getDatabase().insertReceiptParallel(trip, img, name, category, dateBox.date, comment, price, tax, expensable.isChecked(), currency, fullpage.isChecked(), extra_edittext_1, extra_edittext_2, extra_edittext_3);
						 getDateManager().setDateEditTextListenerDialogHolder(null);
						 dialog.cancel();
					 }
					 else { //Update
						 if (TextUtils.isEmpty(price)) {
							price = "0";
						}
						 getPersistenceManager().getDatabase().updateReceiptParallel(receipt, trip, name, category, (dateBox.date == null) ? receipt.getDate() : dateBox.date, comment, price, tax, expensable.isChecked(), currency, fullpage.isChecked(), extra_edittext_1, extra_edittext_2, extra_edittext_3);
						 getDateManager().setDateEditTextListenerDialogHolder(null);
						 dialog.cancel();
					 }
				 }
			 })
			 .setNegativeButton(getFlexString(R.string.DIALOG_RECEIPTMENU_NEGATIVE_BUTTON), new DialogInterface.OnClickListener() {
				 @Override
				public void onClick(DialogInterface dialog, int which) {
					 if (img != null && newReceipt)
					 {
						getPersistenceManager().getStorageManager().delete(img); //Clean Up On Cancel
					}
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
					if (hasFocus && getActivity() != null) {
						if (getActivity().getResources().getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
							dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
						}
					}
				}
			});
		}
		categoriesSpinner.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (getActivity() != null) {
						InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(nameBox.getWindowToken(), 0);
					}
				}
				return false;
			}
		});
		currencySpinner.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (getActivity() != null) {
						InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(nameBox.getWindowToken(), 0);
					}
				}
				return false;
			}
		});
	}

	public final void showMileage() {
    	final String milesString = mCurrentTrip.getMilesAsString();
    	final BetterDialogBuilder builder = new BetterDialogBuilder(getActivity());
    	final View linearLayout = getFlex().getView(getActivity(), R.layout.dialog_mileage);
		final EditText milesBox = (EditText) getFlex().getSubView(getActivity(), linearLayout, R.id.DIALOG_MILES_DELTA);
		builder.setTitle(getString(R.string.total_item, milesString))
			   .setCancelable(true)
			   .setView(linearLayout)
			   .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
		           @Override
				public void onClick(DialogInterface dialog, int id) {
		        	   if (!getPersistenceManager().getDatabase().addMiles(mCurrentTrip, milesBox.getText().toString())) {
		        		   Toast.makeText(getActivity(), R.string.toast_error_invalid_input, Toast.LENGTH_SHORT).show();
		        	   }
		        	   else {
		        		   mAdapter.notifyDataSetChanged();
		        	   }
		           }
			   })
			   .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		           @Override
				public void onClick(DialogInterface dialog, int id) {

		           }
			   })
			   .show();
    }

    public final boolean editReceipt(final ReceiptRow receipt) {
    	mHighlightedReceipt = receipt;
    	final BetterDialogBuilder builder = new BetterDialogBuilder(getActivity());
		builder.setTitle(receipt.getName())
			   .setCancelable(true)
			   .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		           @Override
				public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		final Attachment attachment = mAttachable.getAttachment();
		if (attachment != null && attachment.isDirectlyAttachable()) {
			final String[] receiptActions;
			final int stringId = attachment.isPDF() ? R.string.pdf : R.string.image;
			final String viewFile = getString(R.string.action_send_view, getString(stringId));
			final String attachFile = getString(R.string.action_send_attach, getString(stringId));
			final String replaceFile = getString(R.string.action_send_replace, getString(receipt.hasPDF() ? R.string.pdf : R.string.image));
			if (receipt.hasFile()) {
				receiptActions = new String[] { viewFile, replaceFile };
			}
			else {
				receiptActions = new String[] { attachFile };
			}
			builder.setItems(receiptActions, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					final String selection = receiptActions[item].toString();
					if (selection == viewFile) { //Show File
						if (attachment.isPDF()) {
							ReceiptsListFragment.this.showPDF(receipt);
						}
						else {
							ReceiptsListFragment.this.showImage(receipt);
						}
					}
					else if (selection == attachFile) { //Attach File to Receipt
						if (attachment.isPDF()) {
							ReceiptsListFragment.this.attachPDFToReceipt(attachment, receipt, false);
						}
						else {
							ReceiptsListFragment.this.attachImageToReceipt(attachment, receipt, false);
						}
					}
					else if (selection == replaceFile) { //Replace File for Receipt
						if (attachment.isPDF()) {
							ReceiptsListFragment.this.attachPDFToReceipt(attachment, receipt, true);
						}
						else {
							ReceiptsListFragment.this.attachImageToReceipt(attachment, receipt, true);
						}
					}
				}
			});
		}
		else {
			final String receiptActionEdit = getString(R.string.receipt_dialog_action_edit);
		    final String receiptActionView = getString(R.string.receipt_dialog_action_view, getString(receipt.hasPDF() ? R.string.pdf : R.string.image));
		    final String receiptActionCamera = getString(R.string.receipt_dialog_action_camera);
		    final String receiptActionDelete = getString(R.string.receipt_dialog_action_delete);
		    final String receiptActionMoveCopy = getString(R.string.receipt_dialog_action_move_copy);
		    final String receiptActionSwapUp = getString(R.string.receipt_dialog_action_swap_up);
		    final String receiptActionSwapDown = getString(R.string.receipt_dialog_action_swap_down);
		    final String[] receiptActions;
		    if (!receipt.hasFile()) {
		    	receiptActions = new String[] { receiptActionEdit, receiptActionCamera, receiptActionDelete, receiptActionMoveCopy, receiptActionSwapUp, receiptActionSwapDown };
		    }
		    else {
		    	receiptActions = new String[] { receiptActionEdit, receiptActionView, receiptActionDelete, receiptActionMoveCopy, receiptActionSwapUp, receiptActionSwapDown };
		    }
			builder.setItems(receiptActions, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
			    	final String selection = receiptActions[item].toString();
			    	if (selection == receiptActionEdit) { //Edit Receipt
			    		ReceiptsListFragment.this.receiptMenu(mCurrentTrip, receipt, null);
			    	}
			    	else if (selection == receiptActionCamera) { //Take Photo
						File dir = mCurrentTrip.getDirectory();
						String dirPath = dir.exists() ? dir.getAbsolutePath() : getPersistenceManager().getStorageManager().mkdir(dir.getName()).getAbsolutePath();
			    		if (getPersistenceManager().getPreferences().useNativeCamera()) {
			            	final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			                mImageUri = Uri.fromFile(new File(dirPath, receipt.getId() + "x.jpg"));
			                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
			                startActivityForResult(intent, NATIVE_ADD_PHOTO_CAMERA_REQUEST);
			        	}
			        	else {
			        		if (wb.android.google.camera.common.ApiHelper.NEW_SR_CAMERA_IS_SUPPORTED) {
			    				final Intent intent = new Intent(getActivity(), wb.android.google.camera.CameraActivity.class);
			    				mImageUri = Uri.fromFile(new File(dirPath, receipt.getId() + "x.jpg"));
			    				intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
			    				startActivityForResult(intent, ADD_PHOTO_CAMERA_REQUEST);
			    			}
			    			else {
								final Intent intent = new Intent(getActivity(), MyCameraActivity.class);
								String[] strings  = new String[] {dirPath, receipt.getId() + "x.jpg"};
								intent.putExtra(MyCameraActivity.STRING_DATA, strings);
								startActivityForResult(intent, ADD_PHOTO_CAMERA_REQUEST);
			    			}
			        	}
			    	}
			    	else if (selection == receiptActionView) { //View Photo/PDF
			    		if (receipt.hasPDF()) {
			    			ReceiptsListFragment.this.showPDF(receipt);
			    		}
			    		else {
			    			ReceiptsListFragment.this.showImage(receipt);
			    		}
			    	}
			    	else if (selection == receiptActionDelete) { //Delete Receipt
			    		ReceiptsListFragment.this.deleteReceipt(receipt);
			    	}
			    	else if (selection == receiptActionMoveCopy) {//Move-Copy
			    		ReceiptsListFragment.this.moveOrCopy(receipt);
			    	}
			    	else if (selection == receiptActionSwapUp) { //Swap Up
			    		ReceiptsListFragment.this.moveReceiptUp(receipt);
			    	}
			    	else if (selection == receiptActionSwapDown) { //Swap Down
			    		ReceiptsListFragment.this.moveReceiptDown(receipt);
			    	}
			    	dialog.cancel();
			    }
			});
		}
		builder.show();
    	return true;
    }

    private void attachImageToReceipt(Attachment attachment, ReceiptRow receipt, boolean replace) {
    	File dir = mCurrentTrip.getDirectory();
		String dirPath = dir.exists() ? dir.getAbsolutePath() : getPersistenceManager().getStorageManager().mkdir(dir.getName()).getAbsolutePath();
		File file = getWorkerManager().getImageGalleryWorker().transformNativeCameraBitmap(attachment.getUri(), null, Uri.fromFile(new File(dirPath, receipt.getId() + "x.jpg")));
		if (file != null) {
			// TODO: Off UI Thread
			final ReceiptRow retakeReceipt = getPersistenceManager().getDatabase().updateReceiptFile(receipt, file);
			if (retakeReceipt != null) {
				getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
				int stringId = replace ? R.string.toast_receipt_image_replaced : R.string.toast_receipt_image_added;
				Toast.makeText(getActivity(), getString(stringId, receipt.getName()), Toast.LENGTH_SHORT).show();
				getActivity().finish(); // Finish activity since we're done with the send action
			}
			else {
				Toast.makeText(getActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
				getActivity().finish(); // Finish activity since we're done with the send action
				//TODO: Add overwrite rollback here
			}
		}
		else {
			Toast.makeText(getActivity(), getFlexString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
			getActivity().finish(); // Finish activity since we're done with the send action
		}
    }

    private void attachPDFToReceipt(Attachment attachment, ReceiptRow receipt, boolean replace) {
    	File dir = mCurrentTrip.getDirectory();
		String dirPath = dir.exists() ? dir.getAbsolutePath() : getPersistenceManager().getStorageManager().mkdir(dir.getName()).getAbsolutePath();
		File file = new File(dirPath, receipt.getId() + "x.pdf");
		InputStream is = null;
		try {
			// TODO: Off UI Thread
			is = attachment.openUri(getActivity().getContentResolver());
			getPersistenceManager().getStorageManager().copy(is, file, true);
			if (file != null) {
				final ReceiptRow retakeReceipt = getPersistenceManager().getDatabase().updateReceiptFile(receipt, file);
				if (retakeReceipt != null) {
					getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
					int stringId = replace ? R.string.toast_receipt_pdf_replaced : R.string.toast_receipt_pdf_added;
					Toast.makeText(getActivity(), getString(stringId, receipt.getName()), Toast.LENGTH_SHORT).show();
					getActivity().finish(); // Finish activity since we're done with the send action
				}
				else {
					Toast.makeText(getActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
					getActivity().finish(); // Finish activity since we're done with the send action
					//TODO: Add overwrite rollback here
				}
			}
		}
		catch (IOException e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, e.toString());
			}
			Toast.makeText(getActivity(), getString(R.string.toast_pdf_save_error), Toast.LENGTH_SHORT).show();
			getActivity().finish();
		}
		catch (SecurityException e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, e.toString());
			}
			Toast.makeText(getActivity(), getString(R.string.toast_kitkat_security_error), Toast.LENGTH_LONG).show();
			getActivity().finish();
		}
		finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
			}
			catch (IOException e) {
				Log.w(TAG, e.toString());
			}
		}
    }

    public void emailTrip() {
    	EmailAssistant.email(getSmartReceiptsApplication(), getActivity(), mCurrentTrip);
    }

    private final void showImage(ReceiptRow receipt) {
		final Intent intent = new Intent(getActivity(), ReceiptImageActivity.class);
    	intent.putExtra(ReceiptRow.PARCEL_KEY, receipt);
    	intent.putExtra(TripRow.PARCEL_KEY, mCurrentTrip);
    	startActivity(intent);
    }

    private final void showPDF(ReceiptRow receipt) {
    	final Intent intent = new Intent(getActivity(), ReceiptPDFActivity.class);
    	intent.setData(Uri.fromFile(receipt.getPDF()));
    	startActivity(intent);
    }

    public final void deleteReceipt(final ReceiptRow receipt) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.delete_item, receipt.getName()))
			   .setCancelable(true)
			   .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
		           @Override
				public void onClick(DialogInterface dialog, int id) {
		                getPersistenceManager().getDatabase().deleteReceiptParallel(receipt, mCurrentTrip);
		           }
		       })
		       .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		           @Override
				public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       })
		       .show();
    }

    public void moveOrCopy(final ReceiptRow receipt) {
    	final DatabaseHelper db = getPersistenceManager().getDatabase();
    	final BetterDialogBuilder builder = new BetterDialogBuilder(getActivity());
    	final LinearLayout outerLayout = new LinearLayout(getActivity());
		outerLayout.setOrientation(LinearLayout.VERTICAL);
		outerLayout.setGravity(Gravity.BOTTOM);
		outerLayout.setPadding(10, 0, 10, 10);
		final Spinner tripsSpinner = new Spinner(getActivity());
		List<CharSequence> trips = db.getTripNames(mCurrentTrip);
		final ArrayAdapter<CharSequence> tripNames = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, trips);
		tripNames.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tripsSpinner.setAdapter(tripNames);
		tripsSpinner.setPrompt(getString(R.string.report));
		outerLayout.addView(tripsSpinner, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		builder.setTitle(getString(R.string.move_copy_item, receipt.getName()))
			   .setView(outerLayout)
			   .setCancelable(true)
			   .setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
		           @Override
				public void onClick(DialogInterface dialog, int id) {
		        	   if (tripsSpinner.getSelectedItem() != null) {
			        	   db.moveReceiptParallel(receipt, mCurrentTrip, db.getTripByName(tripsSpinner.getSelectedItem().toString()));
			        	   dialog.cancel();
		        	   }
		        	   else {
		        		   ReceiptsListFragment.this.onReceiptMoveFailure();
		        	   }
		           }
		       })
		       .setNegativeButton(R.string.copy, new DialogInterface.OnClickListener() {
		           @Override
				public void onClick(DialogInterface dialog, int id) {
		        	   if (tripsSpinner.getSelectedItem() != null) {
			        	   db.copyReceiptParallel(receipt, db.getTripByName(tripsSpinner.getSelectedItem().toString()));
			        	   dialog.cancel();
			           }
		        	   else {
		        		   ReceiptsListFragment.this.onReceiptCopyFailure();
		        	   }
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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	editReceipt(mAdapter.getItem(position));
    }

	@Override
	public void onReceiptRowsQuerySuccess(ReceiptRow[] receipts) {
		mProgressDialog.setVisibility(View.GONE);
		getListView().setVisibility(View.VISIBLE);
		if (receipts == null || receipts.length == 0) {
			mNoDataAlert.setVisibility(View.VISIBLE);
		}
		else {
			mNoDataAlert.setVisibility(View.INVISIBLE);
		}
		getPersistenceManager().getDatabase().getTripsParallel();
		mAdapter.notifyDataSetChanged(receipts);
		updateActionBarTitle();
	}

	@Override
	public void onReceiptRowInsertSuccess(ReceiptRow receipt) {
		getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
	}

	@Override
	public void onReceiptRowInsertFailure(SQLException ex) {
		Toast.makeText(getActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onReceiptRowUpdateSuccess(ReceiptRow receipt) {
		getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
		ReceiptsListFragment.this.updateActionBarTitle();
	}

	@Override
	public void onReceiptRowUpdateFailure() {
		Toast.makeText(getActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
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
    	if (receipt.hasFile()) {
    		if (!getPersistenceManager().getStorageManager().delete(receipt.getFile())) {
				Toast.makeText(getActivity(), getFlexString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
			}
    	}
    	if (!receipt.isPriceEmpty()) {
    		ReceiptsListFragment.this.updateActionBarTitle();
    	}
	}

	@Override
	public void onReceiptDeleteFailure() {
		Toast.makeText(getActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
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
			if (sNameBox != null) {
				sNameBox.setText(sCategories.getItem(position));
			}
			if (sCommentBox != null) {
				sCommentBox.setText(sCategories.getItem(position));
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {}
	}

	@Override
	public void onReceiptCopySuccess(TripRow tripRow) {
		getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
		getPersistenceManager().getDatabase().getTripsParallel(); // Call this to update Trip Fragments
		Toast.makeText(getActivity(), getFlexString(R.string.toast_receipt_copy), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onReceiptCopyFailure() {
		Toast.makeText(getActivity(), getFlexString(R.string.COPY_ERROR), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onReceiptMoveSuccess(TripRow tripRow) {
		getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
		getPersistenceManager().getDatabase().getTripsParallel(); // Call this to update Trip Fragments
		Toast.makeText(getActivity(), getFlexString(R.string.toast_receipt_move), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onReceiptMoveFailure() {
		Toast.makeText(getActivity(), getFlexString(R.string.MOVE_ERROR), Toast.LENGTH_SHORT).show();
	}

}

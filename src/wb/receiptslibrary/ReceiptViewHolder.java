package wb.receiptslibrary;	

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

import wb.android.autocomplete.AutoCompleteAdapter;
import wb.android.dialog.BetterDialogBuilder;
import wb.android.dialog.DirectDialogOnClickListener;
import wb.android.dialog.DirectLongLivedOnClickListener;
import wb.android.google.camera.PhotoModule;
import wb.android.google.camera.app.GalleryApp;
import wb.receiptslibrary.EmailAttachmentWriter.EmailOptions;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;

public class ReceiptViewHolder extends DateViewHolder implements DatabaseHelper.ReceiptRowListener {

	private static final boolean D = SmartReceiptsActivity.D;
	private static final String TAG = "ReceiptViewHolder";
	
	//Activity Request ints
    private static final int NEW_RECEIPT_CAMERA_REQUEST = 1;
    private static final int ADD_PHOTO_CAMERA_REQUEST = 2;
    private static final int NATIVE_NEW_RECEIPT_CAMERA_REQUEST = 3;
    private static final int NATIVE_ADD_PHOTO_CAMERA_REQUEST = 4;
	
	private ReceiptAdapter receiptAdapter;
	private ReceiptRow highlightedReceipt;
	private final TripRow currentTrip;
	private Uri _imageUri;
	private HomeHolder parent;
	private AutoCompleteAdapter autoCompleteAdapter;
	private Date _dateCache;
	
	public ReceiptViewHolder(SmartReceiptsActivity activity, TripRow currentTrip) {
		super(activity);
		this.currentTrip = currentTrip;
	}
	
	public ReceiptViewHolder(SmartReceiptsActivity activity, Bundle bundle) {
		super(activity);
		_imageUri = (bundle.getString(ViewHolderFactory.State.IMAGE_URI) != null) ? Uri.parse(bundle.getString(ViewHolderFactory.State.IMAGE_URI)) : null;
		highlightedReceipt = activity.getDB().getReceiptByID(bundle.getInt(ViewHolderFactory.State.HIGH_RCPT));
		currentTrip = activity.getDB().getTripByName(bundle.getString(ViewHolderFactory.State.CURR_TRIP));
	}
	
	@Override
	public void onCreate() {
		this.updateActionBarTitle();
		activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		receiptAdapter = new ReceiptAdapter(this, new ReceiptRow[0]);
		getParent().setAdapter(receiptAdapter);
		
		activity.getDB().registerReceiptRowListener(this);
		activity.getDB().getReceiptsParallel(currentTrip);
		activity.SRLog("/ReceiptView");
	}
	
	@Override
	public void onFocus() {
		getParent().setMainLayout();
		getParent().setAdapter(receiptAdapter);
		this.updateActionBarTitle();
		activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public void onPause() {
		if (autoCompleteAdapter != null) {
			autoCompleteAdapter.onPause();
		}
		_dateCache = null;
	}
	
	public void onResume() {
		if (autoCompleteAdapter != null) {
			autoCompleteAdapter.onResume();
		}
	}
	
	@Override
	public void onDestroy() {
		activity.getDB().unregisterReceiptRowListener();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			getParent().onBackPressed();
			return true;
		}
		else
			return getParent().onParentOptionsItemSelected(item);
	}
	
	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    	if (D) Log.d(TAG, "Result Code: " + resultCode);
    	if (D) Log.d(TAG, "Request Code: " + requestCode);
    	if (resultCode == Activity.RESULT_OK) { //-1
    		File imgFile = new File(_imageUri.getPath());
    		if (requestCode == NATIVE_NEW_RECEIPT_CAMERA_REQUEST || requestCode == NATIVE_ADD_PHOTO_CAMERA_REQUEST) {
    			activity.deleteDuplicateGalleryImage(); //Some devices duplicate the gallery images
    			imgFile = activity.transformNativeCameraBitmap(_imageUri, data, null);
    		}
			if (imgFile == null) {
				Toast.makeText(activity, activity.getFlex().getString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
				return;
			}
    		switch (requestCode) {
				case NATIVE_NEW_RECEIPT_CAMERA_REQUEST:
				case NEW_RECEIPT_CAMERA_REQUEST:
					receiptMenu(currentTrip, null, imgFile);
				break;
				case NATIVE_ADD_PHOTO_CAMERA_REQUEST:
				case ADD_PHOTO_CAMERA_REQUEST:
					final ReceiptRow updatedReceipt = activity.getDB().updateReceiptImg(highlightedReceipt, imgFile);
					if (updatedReceipt != null) {
						activity.getDB().getReceiptsParallel(currentTrip);
						Toast.makeText(activity, "Receipt Image Successfully Added to " + highlightedReceipt.name, Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(activity, activity.getFlex().getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
						activity.getStorageManager().delete(imgFile); //Rollback
						return;
					}
				break;
				default:
					if (D) Log.e(TAG, "Unrecognized Request Code: " + requestCode);
					super.onActivityResult(requestCode, resultCode, data);
				break;
			}
    	}
    	else if (resultCode == MyCameraActivity.PICTURE_SUCCESS) {  //51
	    	switch (requestCode) {
				case NEW_RECEIPT_CAMERA_REQUEST:
					receiptMenu(currentTrip, null, new File(data.getStringExtra(MyCameraActivity.IMG_FILE)));
				break;
				case ADD_PHOTO_CAMERA_REQUEST:
					File img = new File(data.getStringExtra(MyCameraActivity.IMG_FILE));
					final ReceiptRow updatedReceipt = activity.getDB().updateReceiptImg(highlightedReceipt, img);
					if (updatedReceipt != null) {
						activity.getDB().getReceiptsParallel(currentTrip);
						Toast.makeText(activity, "Receipt Image Successfully Added to " + highlightedReceipt.name, Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(activity, activity.getFlex().getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
						return;
					}
				break;
				default:
					if (D) Log.e(TAG, "Unrecognized Request Code: " + requestCode);
					super.onActivityResult(requestCode, resultCode, data);
				break;
			}
    	}
    	else if (resultCode == PhotoModule.RESULT_SAVE_FAILED) {
    		Toast.makeText(activity, activity.getFlex().getString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
			return;
    	}
    	else {
			if (D) Log.e(TAG, "Unrecgonized Result Code: " + resultCode);
			List<String> errors = ((GalleryApp)activity.getApplication()).getErrorList();
	    	final int size = errors.size();
	    	for (int i=0; i < size; i++) {
	    		activity.SRErrorLog(errors.get(i));
	    	}
	    	errors.clear();
			super.onActivityResult(requestCode, resultCode, data);
    	}
    }
	
	final void updateActionBarTitle() {
		final String currency = SRUtils.CurrencyValue(currentTrip.price, currentTrip.currency);
    	activity.getSupportActionBar().setTitle(currency + " - " + currentTrip.dir.getName());
	}
	
	public final void addPictureReceipt() {
		String dirPath;
		File dir = currentTrip.dir;
		if (dir.exists())
			dirPath = dir.getAbsolutePath();
		else
			dirPath = activity.getStorageManager().mkdir(dir.getName()).getAbsolutePath();
		if (activity.getPreferences().useNativeCamera()) {
			final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			_imageUri = Uri.fromFile(new File(dirPath, System.currentTimeMillis() + "x" + activity.getDB().getReceiptsSerial(currentTrip).length + ".jpg"));
			intent.putExtra(MediaStore.EXTRA_OUTPUT, _imageUri);
			activity.startActivityForResult(intent, NATIVE_NEW_RECEIPT_CAMERA_REQUEST);
		}
		else {
			if (wb.android.google.camera.common.ApiHelper.NEW_SR_CAMERA_IS_SUPPORTED) {
				final Intent intent = new Intent(activity, wb.android.google.camera.CameraActivity.class);
				_imageUri = Uri.fromFile(new File(dirPath, System.currentTimeMillis() + "x" + activity.getDB().getReceiptsSerial(currentTrip).length + ".jpg"));
				intent.putExtra(MediaStore.EXTRA_OUTPUT, _imageUri);
				activity.startActivityForResult(intent, NEW_RECEIPT_CAMERA_REQUEST);
			}
			else {
				final Intent intent = new Intent(activity, MyCameraActivity.class);
				String[] strings  = new String[] {dirPath, System.currentTimeMillis() + "x" + activity.getDB().getReceiptsSerial(currentTrip).length + ".jpg"};
				intent.putExtra(MyCameraActivity.STRING_DATA, strings);
				activity.startActivityForResult(intent, NEW_RECEIPT_CAMERA_REQUEST);
			}
		}
	}
		    
	public final void addTextReceipt() {
		receiptMenu(currentTrip ,null, null);
	}
		    
	public final void receiptMenu(final TripRow trip, final ReceiptRow receipt, final File img) {
		final boolean newReceipt = (receipt == null);
		final View scrollView = activity.getFlex().getView(R.layout.dialog_receiptmenu);
		final AutoCompleteTextView nameBox = (AutoCompleteTextView) activity.getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_NAME);
		final EditText priceBox = (EditText) activity.getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_PRICE);
		final EditText taxBox = (EditText) activity.getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_TAX);
		final Spinner currencySpinner = (Spinner) activity.getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_CURRENCY);
		final DateEditText dateBox = (DateEditText) activity.getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_DATE);
		final EditText commentBox = (EditText) activity.getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_COMMENT);
		final Spinner categoriesSpinner =  (Spinner) activity.getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_CATEGORY);
		final CheckBox expensable = (CheckBox) activity.getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_EXPENSABLE);
		final CheckBox fullpage = (CheckBox) activity.getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_FULLPAGE);
		
		//Extras
		final LinearLayout extras = (LinearLayout) activity.getFlex().getSubView(scrollView, R.id.DIALOG_RECEIPTMENU_EXTRAS);
		final EditText extra_edittext_box_1 = (EditText) extras.findViewWithTag(activity.getFlex().getString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_1));
		final EditText extra_edittext_box_2 = (EditText) extras.findViewWithTag(activity.getFlex().getString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_2));
		final EditText extra_edittext_box_3 = (EditText) extras.findViewWithTag(activity.getFlex().getString(R.string.RECEIPTMENU_TAG_EXTRA_EDITTEXT_3));
		
		if (getActivity().getPreferences().includeTaxField()) {
			priceBox.setHint(getActivity().getFlex().getString(R.string.DIALOG_RECEIPTMENU_HINT_PRICE_SHORT));
			taxBox.setVisibility(View.VISIBLE);
		}
		
		//Show default dictionary with auto-complete
		TextKeyListener input = TextKeyListener.getInstance(true, TextKeyListener.Capitalize.SENTENCES);
		nameBox.setKeyListener(input);
		
		final ArrayAdapter<CharSequence> currenices = new ArrayAdapter<CharSequence>(activity, android.R.layout.simple_spinner_item, activity.getDB().getCurrenciesList());
		currenices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		currencySpinner.setAdapter(currenices);
		dateBox.setFocusableInTouchMode(false); dateBox.setOnClickListener(getDateEditTextListener());
		final ArrayAdapter<CharSequence> categories = new ArrayAdapter<CharSequence>(activity, android.R.layout.simple_spinner_item, activity.getDB().getCategoriesList());
		categories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categoriesSpinner.setAdapter(categories);
		
		if (newReceipt) {
			if (getActivity().getPreferences().enableAutoCompleteSuggestions()) {
				autoCompleteAdapter = AutoCompleteAdapter.getInstance(activity, activity.getDB(), DatabaseHelper.TAG_RECEIPTS);
				nameBox.setAdapter(autoCompleteAdapter);
			}
			Time now = new Time(); now.setToNow();
			if (_dateCache == null) dateBox.date = new Date(now.toMillis(false));
			else dateBox.date = _dateCache;
			dateBox.setText(DateFormat.getDateFormat(activity).format(dateBox.date));
			expensable.setChecked(true);
			Preferences preferences = activity.getPreferences();
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
			if (receipt.name.length() != 0) { nameBox.setText(receipt.name); }
			if (receipt.price.length() != 0) { priceBox.setText(receipt.price); }
			if (receipt.date != null) { dateBox.setText(DateFormat.getDateFormat(activity).format(receipt.date)); dateBox.date = receipt.date; }
			if (receipt.category.length() != 0) { categoriesSpinner.setSelection(categories.getPosition(receipt.category)); }
			if (receipt.comment.length() != 0) { commentBox.setText(receipt.comment); }
			if (receipt.tax.length() != 0) { taxBox.setText(receipt.tax); }
			int idx = currenices.getPosition(receipt.currency.getCurrencyCode());
			if (idx > 0) currencySpinner.setSelection(idx);
			expensable.setChecked(receipt.expensable);
			fullpage.setChecked(receipt.fullpage);
			if (extra_edittext_box_1 != null && receipt.extra_edittext_1 != null) extra_edittext_box_1.setText(receipt.extra_edittext_1);
			if (extra_edittext_box_2 != null && receipt.extra_edittext_2 != null) extra_edittext_box_2.setText(receipt.extra_edittext_2);
			if (extra_edittext_box_3 != null && receipt.extra_edittext_3 != null) extra_edittext_box_3.setText(receipt.extra_edittext_3);
		}
		nameBox.setSelection(nameBox.getText().length()); //Put the cursor at the end
		
		//Show DialogController
		final BetterDialogBuilder builder = new BetterDialogBuilder(activity);
		builder.setTitle((newReceipt)?activity.getFlex().getString(R.string.DIALOG_RECEIPTMENU_TITLE_NEW):activity.getFlex().getString(R.string.DIALOG_RECEIPTMENU_TITLE_EDIT))
			 .setCancelable(true)
			 .setView(scrollView)
			 .setLongLivedPositiveButton((newReceipt)?activity.getFlex().getString(R.string.DIALOG_RECEIPTMENU_POSITIVE_BUTTON_CREATE):activity.getFlex().getString(R.string.DIALOG_RECEIPTMENU_POSITIVE_BUTTON_UPDATE), new DirectLongLivedOnClickListener<SmartReceiptsActivity>(activity) {
				 @Override
				 public void onClick(DialogInterface dialog, int whichButton) {
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
						 Toast.makeText(activity, activity.getFlex().getString(R.string.DIALOG_RECEIPTMENU_TOAST_MISSING_NAME), Toast.LENGTH_SHORT).show();
						 return;
					 }
					 if (dateBox.date == null) {
						 Toast.makeText(activity, activity.getFlex().getString(R.string.CALENDAR_TAB_ERROR), Toast.LENGTH_SHORT).show();
						 return;
					 }
					 Calendar receiptCalendar = Calendar.getInstance(); receiptCalendar.setTime(dateBox.date); 
					 receiptCalendar.set(Calendar.HOUR_OF_DAY, 0); receiptCalendar.set(Calendar.MINUTE, 0); receiptCalendar.set(Calendar.SECOND, 0); receiptCalendar.set(Calendar.MILLISECOND, 0);
					 Calendar tripCalendar = Calendar.getInstance(); tripCalendar.setTime(trip.from);
					 tripCalendar.set(Calendar.HOUR_OF_DAY, 0); tripCalendar.set(Calendar.MINUTE, 0); tripCalendar.set(Calendar.SECOND, 0); tripCalendar.set(Calendar.MILLISECOND, 0);
					 if (receiptCalendar.compareTo(tripCalendar) < 0) {
						 Toast.makeText(activity, activity.getFlex().getString(R.string.DIALOG_RECEIPTMENU_TOAST_BAD_DATE), Toast.LENGTH_LONG).show();
					 }
					 tripCalendar.setTime(trip.to);
					 tripCalendar.set(Calendar.HOUR_OF_DAY, 0); tripCalendar.set(Calendar.MINUTE, 0); tripCalendar.set(Calendar.SECOND, 0); tripCalendar.set(Calendar.MILLISECOND, 0);
					 if (receiptCalendar.compareTo(tripCalendar) > 0) {
						 Toast.makeText(activity, activity.getFlex().getString(R.string.DIALOG_RECEIPTMENU_TOAST_BAD_DATE), Toast.LENGTH_LONG).show();
					 }
					 if (newReceipt) {//Insert
						 activity.getDB().insertReceiptParallel(trip, img, name, category, dateBox.date, comment, price, tax, expensable.isChecked(), currency, fullpage.isChecked(), extra_edittext_1, extra_edittext_2, extra_edittext_3);
						 setDateEditTextListenerDialogHolder(null);
						 dialog.cancel();
					 }
					 else { //Update
						 if (price == null || price.length() == 0)
							 price = "0";
						 activity.getDB().updateReceiptParallel(receipt, trip, name, category, (dateBox.date == null) ? receipt.date : dateBox.date, comment, price, tax, expensable.isChecked(), currency, fullpage.isChecked(), extra_edittext_1, extra_edittext_2, extra_edittext_3);
						 setDateEditTextListenerDialogHolder(null);
						 dialog.cancel();
					 }
				 }
			 })  
			 .setNegativeButton(activity.getFlex().getString(R.string.DIALOG_RECEIPTMENU_NEGATIVE_BUTTON), new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
				 public void onClick(DialogInterface dialog, int which) {
					 if (img != null && newReceipt)
						 activity.getStorageManager().delete(img); //Clean Up On Cancel
					 setDateEditTextListenerDialogHolder(null);
					 dialog.cancel();   
				 }
			 });
		final AlertDialog dialog = builder.show();
		setDateEditTextListenerDialogHolder(dialog);
		if (newReceipt) {
			nameBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus && activity.getResources().getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
						dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			});
		}
		categoriesSpinner.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(nameBox.getWindowToken(), 0);
				}
				return false;
			}
		});
		currencySpinner.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(nameBox.getWindowToken(), 0);
				}
				return false;
			}
		});
	}
	
	public final void showMileage() {
    	final String milesString = currentTrip.getMilesString();
    	final BetterDialogBuilder builder = new BetterDialogBuilder(activity);
    	final View linearLayout = activity.getFlex().getView(R.layout.dialog_mileage);
		final EditText milesBox = (EditText) activity.getFlex().getSubView(linearLayout, R.id.DIALOG_MILES_DELTA);
		builder.setTitle("Total: " + milesString)
			   .setCancelable(true)
			   .setView(linearLayout)
			   .setPositiveButton("Add", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
		           public void onClick(DialogInterface dialog, int id) {
		        	   if (!activity.getDB().addMiles(currentTrip, milesString, milesBox.getText().toString()))
		        		   Toast.makeText(activity, "Bad Input", Toast.LENGTH_SHORT).show();
		        	   else
		        		   receiptAdapter.notifyDataSetChanged();
		           }
			   })
			   .setNegativeButton("Cancel", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
		           public void onClick(DialogInterface dialog, int id) {
		        	   
		           }
			   })
			   .show();
    }
    
    public final boolean editReceipt(final ReceiptRow receipt) {
    	highlightedReceipt = receipt;
    	final BetterDialogBuilder builder = new BetterDialogBuilder(activity);
		builder.setTitle(receipt.name)
			   .setCancelable(true)
			   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       });
		if (activity.calledFromActionSend()) {
			if (receipt.img == null) {
				final String[] actionSendNoImgEditReceiptItems = activity.getFlex().getStringArray(R.array.ACTION_SEND_NOIMG_EDIT_RECEIPT_ITEMS);
				builder.setItems(actionSendNoImgEditReceiptItems, new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						final String selection = actionSendNoImgEditReceiptItems[item].toString();
						if (selection == actionSendNoImgEditReceiptItems[0]) { //Attach Image to Receipt
							String dirPath;
							File dir = currentTrip.dir;
							if (dir.exists())
								dirPath = dir.getAbsolutePath();
							else
								dirPath = activity.getStorageManager().mkdir(dir.getName()).getAbsolutePath();
							File imgFile = activity.transformNativeCameraBitmap(activity.actionSendUri(), null, Uri.fromFile(new File(dirPath, receipt.id + "x.jpg")));
							if (imgFile != null) {
								Log.e(TAG, imgFile.getPath());
								final ReceiptRow updatedReceipt = activity.getDB().updateReceiptImg(receipt, imgFile);
								if (updatedReceipt != null) {
									activity.getDB().getReceiptsParallel(currentTrip);
									Toast.makeText(activity, "Receipt Image Successfully Added to " + highlightedReceipt.name, Toast.LENGTH_SHORT).show();
									activity.setResult(Activity.RESULT_OK, new Intent(Intent.ACTION_SEND, Uri.fromFile(imgFile)));
									activity.finish();
								}
								else {
									Toast.makeText(activity, activity.getFlex().getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
									activity.getStorageManager().delete(imgFile); //Rollback
									activity.finish();
									return;
								}
							}
							else {
								Toast.makeText(activity, activity.getFlex().getString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
								activity.finish();
							}
						}
					}
				});
			}
			else {
				final String[] actionSendImgEditReceiptItems = activity.getFlex().getStringArray(R.array.ACTION_SEND_IMG_EDIT_RECEIPT_ITEMS);
				builder.setItems(actionSendImgEditReceiptItems, new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						final String selection = actionSendImgEditReceiptItems[item].toString();
						if (selection == actionSendImgEditReceiptItems[0]) //View Image
							ReceiptViewHolder.this.showImage(receipt);
						else if (selection == actionSendImgEditReceiptItems[1]) { //Attach Image to Receipt
							String dirPath;
							File dir = currentTrip.dir;
							if (dir.exists())
								dirPath = dir.getAbsolutePath();
							else
								dirPath = activity.getStorageManager().mkdir(dir.getName()).getAbsolutePath();
							File imgFile = activity.transformNativeCameraBitmap(activity.actionSendUri(), null, Uri.fromFile(new File(dirPath, receipt.id + "x.jpg")));
							if (imgFile != null) {
								Log.e(TAG, imgFile.getPath());
								final ReceiptRow retakeReceipt = activity.getDB().updateReceiptImg(highlightedReceipt, imgFile);
								if (retakeReceipt != null) {
									activity.getDB().getReceiptsParallel(currentTrip);
									Toast.makeText(activity, "Receipt Image Successfully Replaced for " + highlightedReceipt.name, Toast.LENGTH_SHORT).show();
									activity.setResult(Activity.RESULT_OK, new Intent(Intent.ACTION_SEND, Uri.fromFile(imgFile)));
									activity.finish();
								}
								else {
									Toast.makeText(activity, activity.getFlex().getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
									//Add overwrite rollback here
									activity.finish();
								}
							}
							else {
								Toast.makeText(activity, activity.getFlex().getString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
								activity.finish();
							}
						}
					}
				});
			}
		}
		else {
			if (receipt.img == null) {
				final String[] noImgEditReceiptItems = activity.getFlex().getStringArray(R.array.NOIMG_EDIT_RECEIPT_ITEMS);
				builder.setItems(noImgEditReceiptItems, new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
					@Override
					public void onClick(DialogInterface dialog, int item) {
				    	final String selection = noImgEditReceiptItems[item].toString();
				    	if (selection == noImgEditReceiptItems[0]) //Edit Receipt
				    		ReceiptViewHolder.this.receiptMenu(currentTrip, receipt, null);
				    	else if (selection == noImgEditReceiptItems[1]) { //Take Photo
							String dirPath;
							File dir = currentTrip.dir;
							if (dir.exists())
								dirPath = dir.getAbsolutePath();
							else
								dirPath = activity.getStorageManager().mkdir(dir.getName()).getAbsolutePath();
				    		if (activity.getPreferences().useNativeCamera()) {
				            	final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				                _imageUri = Uri.fromFile(new File(dirPath, receipt.id + "x.jpg"));
				                intent.putExtra(MediaStore.EXTRA_OUTPUT, _imageUri);
				                activity.startActivityForResult(intent, NATIVE_ADD_PHOTO_CAMERA_REQUEST);
				        	}
				        	else {
				        		if (wb.android.google.camera.common.ApiHelper.NEW_SR_CAMERA_IS_SUPPORTED) {
				    				final Intent intent = new Intent(activity, wb.android.google.camera.CameraActivity.class);
				    				_imageUri = Uri.fromFile(new File(dirPath, receipt.id + "x.jpg"));
				    				intent.putExtra(MediaStore.EXTRA_OUTPUT, _imageUri);
				    				activity.startActivityForResult(intent, ADD_PHOTO_CAMERA_REQUEST);
				    			}
				    			else {
									final Intent intent = new Intent(activity, MyCameraActivity.class);
									String[] strings  = new String[] {dirPath, receipt.id + "x.jpg"};
									intent.putExtra(MyCameraActivity.STRING_DATA, strings);
									activity.startActivityForResult(intent, ADD_PHOTO_CAMERA_REQUEST);
				    			}
				        	}
				    	}
				    	else if (selection == noImgEditReceiptItems[2]) //Delete Receipt
				    		ReceiptViewHolder.this.deleteReceipt(receipt);
				    	else if (selection == noImgEditReceiptItems[3]) //Move Up
				    		ReceiptViewHolder.this.moveReceiptUp(receipt);
				    	else if (selection == noImgEditReceiptItems[4]) //Move Down
				    		ReceiptViewHolder.this.moveReceiptDown(receipt);
				    	dialog.cancel();
				    }
				});
			}
			else {
				final String[] imgEditReceiptItems = activity.getFlex().getStringArray(R.array.IMG_EDIT_RECEIPT_ITEMS);
				builder.setItems(imgEditReceiptItems, new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
					@Override
				    public void onClick(DialogInterface dialog, int item) {
				    	final String selection = imgEditReceiptItems[item].toString();
				    	if (selection == imgEditReceiptItems[0]) //Edit Receipt
				    		ReceiptViewHolder.this.receiptMenu(currentTrip, receipt, receipt.img);
				    	else if (selection == imgEditReceiptItems[1]) //View/Retake Image 
				    		ReceiptViewHolder.this.showImage(receipt);
				    	else if (selection == imgEditReceiptItems[2]) //Delete Receipt
				    		ReceiptViewHolder.this.deleteReceipt(receipt);
				    	else if (selection == imgEditReceiptItems[3]) //Move Up
				    		ReceiptViewHolder.this.moveReceiptUp(receipt);
				    	else if (selection == imgEditReceiptItems[4]) //Move Down
				    		ReceiptViewHolder.this.moveReceiptDown(receipt);
				    	dialog.cancel();
				    }
				});
			}
		}
		builder.show();
    	return true;
    }
    
    private final void showImage(final ReceiptRow receipt) {
    	activity.navigateToShowReceiptImage(currentTrip, receipt);
    }
    
    public final void deleteReceipt(final ReceiptRow receipt) {
    	final BetterDialogBuilder builder = new BetterDialogBuilder(activity);
		builder.setTitle("Delete " + receipt.name + "?")
			   .setCancelable(true)
			   .setPositiveButton("Delete", new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
		           public void onClick(DialogInterface dialog, int id) {
		                activity.getDB().deleteReceiptParallel(receipt, currentTrip);
		           }
		       })
		       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       })
		       .show();
    }
    
    public final void emailTrip() {
		if (!activity.getStorageManager().isExternal()) {
    		Toast.makeText(activity, activity.getFlex().getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
    		return;
    	}
    	View scrollView = activity.getFlex().getView(R.layout.dialog_email);
    	final CheckBox pdfFull = (CheckBox) activity.getFlex().getSubView(scrollView, R.id.DIALOG_EMAIL_CHECKBOX_PDF_FULL);
    	final CheckBox pdfImages = (CheckBox) activity.getFlex().getSubView(scrollView, R.id.DIALOG_EMAIL_CHECKBOX_PDF_IMAGES);
    	final CheckBox csv = (CheckBox) activity.getFlex().getSubView(scrollView, R.id.DIALOG_EMAIL_CHECKBOX_CSV);
    	final CheckBox zipStampedImages = (CheckBox) activity.getFlex().getSubView(scrollView, R.id.DIALOG_EMAIL_CHECKBOX_ZIP_IMAGES_STAMPED);
    	final BetterDialogBuilder builder = new BetterDialogBuilder(activity);
    	String msg = activity.getFlex().getString(R.string.DIALOG_EMAIL_MESSAGE);
    	if (msg.length() > 0) 
    		builder.setMessage(msg);
			builder.setTitle(activity.getFlex().getString(R.string.DIALOG_EMAIL_TITLE))
			   .setCancelable(true)
			   .setView(scrollView)
			   .setPositiveButton(activity.getFlex().getString(R.string.DIALOG_EMAIL_POSITIVE_BUTTON), new DirectDialogOnClickListener<SmartReceiptsActivity>(activity) {
				   @Override
		           public void onClick(DialogInterface dialog, int id) {
					   if (!pdfFull.isChecked() && !pdfImages.isChecked() && !csv.isChecked() && !zipStampedImages.isChecked()) {
						   Toast.makeText(activity, activity.getFlex().getString(R.string.DIALOG_EMAIL_TOAST_NO_SELECTION), Toast.LENGTH_SHORT).show();
						   dialog.cancel();
						   return;
					   }
					   if (activity.getDB().getReceiptsSerial(currentTrip).length == 0) {
						   Toast.makeText(activity, activity.getFlex().getString(R.string.DIALOG_EMAIL_TOAST_NO_RECEIPTS), Toast.LENGTH_SHORT).show();
						   dialog.cancel();
						   return;
					   }
		        	   ProgressDialog progress = ProgressDialog.show(activity, "", "Building Reports...", true, false);
		        	   EnumSet<EmailOptions> options = EnumSet.noneOf(EmailOptions.class);
		        	   if (pdfFull.isChecked()) options.add(EmailOptions.PDF_FULL);
		        	   if (pdfImages.isChecked()) options.add(EmailOptions.PDF_IMAGES_ONLY);
		        	   if (csv.isChecked()) options.add(EmailOptions.CSV);
		        	   if (zipStampedImages.isChecked()) options.add(EmailOptions.ZIP_IMAGES_STAMPED);
		        	   EmailAttachmentWriter attachmentWriter = new EmailAttachmentWriter(ReceiptViewHolder.this, activity.getStorageManager(), activity.getDB(), progress, options);
		        	   attachmentWriter.execute(currentTrip);
		           }
		       })
		       .setNegativeButton(activity.getFlex().getString(R.string.DIALOG_EMAIL_NEGATIVE_BUTTON), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       })
		       .show();
	}
    
    public final void postCreateAttachments(File[] files) {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);  
		emailIntent.setType("application/octet-stream");
		ArrayList<Uri> uris = new ArrayList<Uri>();
		if (files[EmailOptions.PDF_FULL.getIndex()] != null) uris.add(Uri.fromFile(files[EmailOptions.PDF_FULL.getIndex()]));
		if (files[EmailOptions.PDF_IMAGES_ONLY.getIndex()] != null) uris.add(Uri.fromFile(files[EmailOptions.PDF_IMAGES_ONLY.getIndex()]));
		if (files[EmailOptions.CSV.getIndex()] != null) uris.add(Uri.fromFile(files[EmailOptions.CSV.getIndex()]));
		if (files[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()] != null) uris.add(Uri.fromFile(files[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()]));
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{getActivity().getPreferences().getDefaultEmailReceipient()});
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, getActivity().getFlex().getString(R.string.EMAIL_DATA_SUBJECT).replace("%REPORT_NAME%", currentTrip.dir.getName()).replace("%USER_ID%", getActivity().getPreferences().getUserID()));
		/*Works for Google Drive. Breaks the rest
		ArrayList<String> extra_text = new ArrayList<String>(); //Need this part to fix a Bundle casting bug
		if (uris.size() == 1) extra_text.add(uris.size() + " report attached");
		if (uris.size() > 1) extra_text.add(uris.size() + " reports attached");
		emailIntent.putStringArrayListExtra(Intent.EXTRA_TEXT, extra_text);
		*/
		
		if (uris.size() == 1) emailIntent.putExtra(Intent.EXTRA_TEXT, uris.size() + " report attached");
		if (uris.size() > 1) emailIntent.putExtra(Intent.EXTRA_TEXT, uris.size() + " reports attached");
		emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
    	activity.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }    
    
    final void moveReceiptUp(final ReceiptRow receipt) {
    	activity.getDB().moveReceiptUp(currentTrip, receipt);
		activity.getDB().getReceiptsParallel(currentTrip);
    }
    
    final void moveReceiptDown(final ReceiptRow receipt) {
    	activity.getDB().moveReceiptDown(currentTrip, receipt);
		activity.getDB().getReceiptsParallel(currentTrip);
    }
    
    public final String getMilesString() {
    	return currentTrip.getMilesString();
    }
    
    public void setParent(HomeHolder parent) {
    	this.parent = parent;
    }
    
    public HomeHolder getParent() {
    	return this.parent;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	if (_imageUri != null) outState.putString(ViewHolderFactory.State.IMAGE_URI, _imageUri.toString());
    	if (highlightedReceipt != null) outState.putInt(ViewHolderFactory.State.HIGH_RCPT, highlightedReceipt.id);
    	if (currentTrip != null) outState.putString(ViewHolderFactory.State.CURR_TRIP, currentTrip.name);
    }

	@Override
	public void onReceiptRowsQuerySuccess(ReceiptRow[] receipts) {
		receiptAdapter.notifyDataSetChanged(receipts);
	}

	@Override
	public void onReceiptRowInsertSuccess(ReceiptRow receipt) {
		activity.getDB().getReceiptsParallel(currentTrip);
		ReceiptViewHolder.this.updateActionBarTitle();
	}

	@Override
	public void onReceiptRowInsertFailure(SQLException ex) {
		Toast.makeText(activity, activity.getFlex().getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onReceiptRowUpdateSuccess(ReceiptRow receipt) {
		activity.getDB().getReceiptsParallel(currentTrip);
		ReceiptViewHolder.this.updateActionBarTitle();
	}

	@Override
	public void onReceiptRowUpdateFailure() {
		Toast.makeText(activity, activity.getFlex().getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();		
	}

	@Override
	public void onReceiptDeleteSuccess(ReceiptRow receipt) {
		activity.getDB().getReceiptsParallel(currentTrip);
    	if (receipt.img != null) {
    		if (!activity.getStorageManager().delete(receipt.img))
    			Toast.makeText(activity, activity.getFlex().getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
    	}
    	if (receipt.price != null && receipt.price.length() != 0) {
			ReceiptViewHolder.this.updateActionBarTitle();
    	}
	}

	@Override
	public void onReceiptDeleteFailure() {
		Toast.makeText(activity, activity.getFlex().getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();		
	}

}
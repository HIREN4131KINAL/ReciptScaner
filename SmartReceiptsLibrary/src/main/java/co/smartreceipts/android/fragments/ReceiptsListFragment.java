package co.smartreceipts.android.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.Attachable;
import co.smartreceipts.android.activities.DefaultFragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.adapters.ReceiptCardAdapter;
import co.smartreceipts.android.legacycamera.MyCameraActivity;
import co.smartreceipts.android.model.Attachment;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import wb.android.dialog.BetterDialogBuilder;
import wb.android.google.camera.PhotoModule;
import wb.android.google.camera.app.GalleryApp;

public class ReceiptsListFragment extends ReceiptsFragment implements DatabaseHelper.ReceiptRowListener {

    public static final String TAG = "ReceiptsListFragment";

    // Activity Request ints
    private static final int NEW_RECEIPT_CAMERA_REQUEST = 1;
    private static final int ADD_PHOTO_CAMERA_REQUEST = 2;
    private static final int NATIVE_NEW_RECEIPT_CAMERA_REQUEST = 3;
    private static final int NATIVE_ADD_PHOTO_CAMERA_REQUEST = 4;
    private static final int IMPORT_GALLERY_IMAGE = 5;

    // Permissions Request Ints
    private static final int PERMISSION_CAMERA_REQUEST = 21;
    private static final int PERMISSION_STORAGE_REQUEST = 22;

    // Outstate
    private static final String OUT_IMAGE_URI = "out_image_uri";

    private ReceiptCardAdapter mAdapter;
    private Receipt mHighlightedReceipt;
    private Uri mImageUri;
    private ProgressBar mProgressDialog;
    private TextView mNoDataAlert;
    private Attachable mAttachable;

    private FloatingActionMenu mFloatingActionMenu;
    private View mFloatingActionMenuActiveMaskView;

    private NavigationHandler mNavigationHandler;
    private boolean mShowDialogOnResume = false;
    private File mImageFile;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Attachable) {
            mAttachable = (Attachable) activity;
        } else {
            throw new ClassCastException("The ReceiptFragment's Activity must extend the Attachable interfaces");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        mAdapter = new ReceiptCardAdapter(getActivity(), getPersistenceManager().getPreferences());
        mNavigationHandler = new NavigationHandler(getActivity(), new DefaultFragmentProvider());
        if (savedInstanceState != null) {
            mImageUri = savedInstanceState.getParcelable(OUT_IMAGE_URI);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        final View rootView = inflater.inflate(getLayoutId(), container, false);
        mProgressDialog = (ProgressBar) rootView.findViewById(R.id.progress);
        mNoDataAlert = (TextView) rootView.findViewById(R.id.no_data);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int id = v.getId();
                if (id == R.id.receipt_action_camera) {
                    getWorkerManager().getLogger().logEvent(ReceiptsListFragment.this, "Add_Picture_Receipt");
                    addPictureReceipt();
                } else if (id == R.id.receipt_action_text) {
                    getWorkerManager().getLogger().logEvent(ReceiptsListFragment.this, "Add_Text_Receipt");
                    addTextReceipt();
                } else if (id == R.id.receipt_action_import) {
                    getWorkerManager().getLogger().logEvent(ReceiptsListFragment.this, "Import from gallery");
                    importReceipt();
                }
            }
        };
        rootView.findViewById(R.id.receipt_action_camera).setOnClickListener(listener);
        rootView.findViewById(R.id.receipt_action_text).setOnClickListener(listener);
        rootView.findViewById(R.id.receipt_action_import).setOnClickListener(listener);
        rootView.findViewById(R.id.receipt_action_text).setVisibility(getConfigurationManager().isTextReceiptsOptionAvailable() ? View.VISIBLE : View.GONE);
        mFloatingActionMenu = (FloatingActionMenu) rootView.findViewById(R.id.fab_menu);
        mFloatingActionMenuActiveMaskView = rootView.findViewById(R.id.fab_active_mask);
        mFloatingActionMenuActiveMaskView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intentional stub to block click events when this view is active
            }
        });
        mFloatingActionMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean isOpen) {
                // TODO: Animate this change with the buttons appearing for cleaner effect
                final Context context = mFloatingActionMenuActiveMaskView.getContext();
                if (isOpen) {
                    mFloatingActionMenuActiveMaskView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.out_from_bottom_right));
                    mFloatingActionMenuActiveMaskView.setVisibility(View.VISIBLE);
                } else {
                    mFloatingActionMenuActiveMaskView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.in_to_bottom_right));
                    mFloatingActionMenuActiveMaskView.setVisibility(View.GONE);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        setListAdapter(mAdapter); // Set this here to ensure this has been laid out already
    }

    public int getLayoutId() {
        return R.layout.receipt_fragment_layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        getPersistenceManager().getDatabase().registerReceiptRowListener(this);
        getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
        if (mShowDialogOnResume) {
            mNavigationHandler.navigateToCreateNewReceiptFragment(mCurrentTrip, mImageFile);
            // receiptMenu(mCurrentTrip, null, mImageFile);
            mShowDialogOnResume = false;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // Refresh as soon as we're visible
            getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        mFloatingActionMenu.close(false);
        getPersistenceManager().getDatabase().unregisterReceiptRowListener(this);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(OUT_IMAGE_URI, mImageUri);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        getPersistenceManager().getDatabase().unregisterReceiptRowListener(this);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.d(TAG, "onActivityResult");
        Log.d(TAG, "Result Code: " + resultCode);
        Log.d(TAG, "Request Code: " + requestCode);

        // Need to make this call here, since users with "Don't keep activities" will hit this call
        // before any of onCreate/onStart/onResume is called. This should restore our current trip (what
        // onResume() would normally do to prevent a variety of crashes that we might encounter
        if (mCurrentTrip == null) {
            mCurrentTrip = getArguments().getParcelable(Trip.PARCEL_KEY);
        }

        // Let's cache the image uri and then null it out to not impact future requests
        final Uri cachedImageUriForRequest = mImageUri;
        mImageUri = null;

        if (resultCode == Activity.RESULT_OK) { // -1
            File imgFile = (cachedImageUriForRequest != null) ? new File(cachedImageUriForRequest.getPath()) : null;
            if (requestCode == NATIVE_NEW_RECEIPT_CAMERA_REQUEST || requestCode == NATIVE_ADD_PHOTO_CAMERA_REQUEST) {
                imgFile = getWorkerManager().getImageGalleryWorker().transformNativeCameraBitmap(cachedImageUriForRequest, data, null);
            } else if (requestCode == IMPORT_GALLERY_IMAGE) {
                imgFile = getWorkerManager().getImageGalleryWorker().transformNativeCameraBitmap(cachedImageUriForRequest, data, Uri.fromFile(new File(mCurrentTrip.getDirectory(), System.currentTimeMillis() + ".jpg")));
            }
            if (imgFile == null) {
                Toast.makeText(getActivity(), getFlexString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
                return;
            }
            switch (requestCode) {
                case IMPORT_GALLERY_IMAGE:
                case NATIVE_NEW_RECEIPT_CAMERA_REQUEST:
                case NEW_RECEIPT_CAMERA_REQUEST:
                    if (this.isResumed()) {
                        mNavigationHandler.navigateToCreateNewReceiptFragment(mCurrentTrip, imgFile);
                        // receiptMenu(mCurrentTrip, null, imgFile);
                    } else {
                        mShowDialogOnResume = true;
                        mImageFile = imgFile;
                    }
                    break;
                case NATIVE_ADD_PHOTO_CAMERA_REQUEST:
                case ADD_PHOTO_CAMERA_REQUEST:
                    final Receipt updatedReceipt = getPersistenceManager().getDatabase().updateReceiptFile(mHighlightedReceipt, imgFile);
                    if (updatedReceipt != null) {
                        getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
                        Toast.makeText(getActivity(), "Receipt Image Successfully Added to " + mHighlightedReceipt.getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
                        getPersistenceManager().getStorageManager().delete(imgFile); // Rollback
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
        } else if (resultCode == MyCameraActivity.PICTURE_SUCCESS) { // 51
            switch (requestCode) {
                case NEW_RECEIPT_CAMERA_REQUEST:
                    File imgFile = new File(data.getStringExtra(MyCameraActivity.IMG_FILE));
                    if (this.isResumed()) {
                        mNavigationHandler.navigateToCreateNewReceiptFragment(mCurrentTrip, imgFile);
                        // receiptMenu(mCurrentTrip, null, imgFile);
                    } else {
                        mShowDialogOnResume = true;
                        mImageFile = imgFile;
                    }
                    break;
                case ADD_PHOTO_CAMERA_REQUEST:
                    File img = new File(data.getStringExtra(MyCameraActivity.IMG_FILE));
                    final Receipt updatedReceipt = getPersistenceManager().getDatabase().updateReceiptFile(mHighlightedReceipt, img);
                    if (updatedReceipt != null) {
                        getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
                        Toast.makeText(getActivity(), "Receipt Image Successfully Added to " + mHighlightedReceipt.getName(), Toast.LENGTH_SHORT).show();
                    } else {
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
        } else if (resultCode == PhotoModule.RESULT_SAVE_FAILED) {
            Toast.makeText(getActivity(), getFlexString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Unrecgonized Result Code: " + resultCode);
            List<String> errors = ((GalleryApp) getActivity().getApplication()).getErrorList();
            final int size = errors.size();
            for (int i = 0; i < size; i++) {
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
        final boolean hasCameraPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        final boolean hasWritePermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (getPersistenceManager().getPreferences().useNativeCamera() || !hasCameraPermission || !hasWritePermission) {
            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mImageUri = Uri.fromFile(new File(dirPath, System.currentTimeMillis() + "x" + getPersistenceManager().getDatabase().getReceiptsSerial(mCurrentTrip).size() + ".jpg"));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            startActivityForResult(intent, NATIVE_NEW_RECEIPT_CAMERA_REQUEST);
        } else {
            if (wb.android.google.camera.common.ApiHelper.NEW_SR_CAMERA_IS_SUPPORTED) {
                final Intent intent = new Intent(getActivity(), wb.android.google.camera.CameraActivity.class);
                mImageUri = Uri.fromFile(new File(dirPath, System.currentTimeMillis() + "x" + getPersistenceManager().getDatabase().getReceiptsSerial(mCurrentTrip).size() + ".jpg"));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                startActivityForResult(intent, NEW_RECEIPT_CAMERA_REQUEST);
            } else {
                final Intent intent = new Intent(getActivity(), MyCameraActivity.class);
                String[] strings = new String[]{dirPath, System.currentTimeMillis() + "x" + getPersistenceManager().getDatabase().getReceiptsSerial(mCurrentTrip).size() + ".jpg"};
                intent.putExtra(MyCameraActivity.STRING_DATA, strings);
                startActivityForResult(intent, NEW_RECEIPT_CAMERA_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA_REQUEST) {
            Log.i(TAG, "Received response for Camera permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "CAMERA permission has now been granted.");
            } else {
                Log.i(TAG, "CAMERA permission was NOT granted.");
                getPersistenceManager().getPreferences().setUseNativeCamera(true);
            }
            // Retry add now with either native camera or now granted way
            addPictureReceipt();
        } else if (requestCode == PERMISSION_STORAGE_REQUEST) {
            Log.i(TAG, "Received response for storage permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "STORAGE permission has now been granted.");
            } else {
                Log.i(TAG, "STORAGE permission was NOT granted.");
                getPersistenceManager().getPreferences().setUseNativeCamera(true);
            }
            // Retry add now with either native camera or now granted way
            addPictureReceipt();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public final void addTextReceipt() {
        mNavigationHandler.navigateToCreateNewReceiptFragment(mCurrentTrip, null);
    }

    private void importReceipt() {
        final Intent intent;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        }
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, IMPORT_GALLERY_IMAGE);
    }

    public final boolean showReceiptMenu(final Receipt receipt) {
        mHighlightedReceipt = receipt;
        final BetterDialogBuilder builder = new BetterDialogBuilder(getActivity());
        builder.setTitle(receipt.getName()).setCancelable(true).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        final Attachment attachment = mAttachable.getAttachment();
        if (attachment != null && attachment.isDirectlyAttachable()) {
            final String[] receiptActions;
            final int attachmentStringId = attachment.isPDF() ? R.string.pdf : R.string.image;
            final int receiptStringId = receipt.hasPDF() ? R.string.pdf : R.string.image;
            final String attachFile = getString(R.string.action_send_attach, getString(attachmentStringId));
            final String viewFile = getString(R.string.action_send_view, getString(receiptStringId));
            final String replaceFile = getString(R.string.action_send_replace, getString(receipt.hasPDF() ? R.string.pdf : R.string.image));
            if (receipt.hasFile()) {
                receiptActions = new String[]{viewFile, replaceFile};
            } else {
                receiptActions = new String[]{attachFile};
            }
            builder.setItems(receiptActions, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    final String selection = receiptActions[item];
                    if (selection != null) {
                        if (selection.equals(viewFile)) { // Show File
                            if (attachment.isPDF()) {
                                ReceiptsListFragment.this.showPDF(receipt);
                            } else {
                                ReceiptsListFragment.this.showImage(receipt);
                            }
                        } else if (selection.equals(attachFile)) { // Attach File to Receipt
                            if (attachment.isPDF()) {
                                ReceiptsListFragment.this.attachPDFToReceipt(attachment, receipt, false);
                            } else {
                                ReceiptsListFragment.this.attachImageToReceipt(attachment, receipt, false);
                            }
                        } else if (selection.equals(replaceFile)) { // Replace File for Receipt
                            if (attachment.isPDF()) {
                                ReceiptsListFragment.this.attachPDFToReceipt(attachment, receipt, true);
                            } else {
                                ReceiptsListFragment.this.attachImageToReceipt(attachment, receipt, true);
                            }
                        }
                    }
                }
            });
        } else {
            final String receiptActionEdit = getString(R.string.receipt_dialog_action_edit);
            final String receiptActionView = getString(R.string.receipt_dialog_action_view, getString(receipt.hasPDF() ? R.string.pdf : R.string.image));
            final String receiptActionCamera = getString(R.string.receipt_dialog_action_camera);
            final String receiptActionDelete = getString(R.string.receipt_dialog_action_delete);
            final String receiptActionMoveCopy = getString(R.string.receipt_dialog_action_move_copy);
            final String receiptActionSwapUp = getString(R.string.receipt_dialog_action_swap_up);
            final String receiptActionSwapDown = getString(R.string.receipt_dialog_action_swap_down);
            final String[] receiptActions;
            if (!receipt.hasFile()) {
                receiptActions = new String[]{receiptActionEdit, receiptActionCamera, receiptActionDelete, receiptActionMoveCopy, receiptActionSwapUp, receiptActionSwapDown};
            } else {
                receiptActions = new String[]{receiptActionEdit, receiptActionView, receiptActionDelete, receiptActionMoveCopy, receiptActionSwapUp, receiptActionSwapDown};
            }
            builder.setItems(receiptActions, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    final String selection = receiptActions[item];
                    if (selection != null) {
                        if (selection.equals(receiptActionEdit)) { // Edit Receipt
                            getWorkerManager().getLogger().logEvent(ReceiptsListFragment.this, "Edit_Receipt");
                            // ReceiptsListFragment.this.receiptMenu(mCurrentTrip, receipt, null);
                            mNavigationHandler.navigateToEditReceiptFragment(mCurrentTrip, receipt);
                        } else if (selection.equals(receiptActionCamera)) { // Take Photo
                            getWorkerManager().getLogger().logEvent(ReceiptsListFragment.this, "Take_Photo_For_Existing_Receipt");
                            File dir = mCurrentTrip.getDirectory();
                            String dirPath = dir.exists() ? dir.getAbsolutePath() : getPersistenceManager().getStorageManager().mkdir(dir.getName()).getAbsolutePath();
                            if (getPersistenceManager().getPreferences().useNativeCamera()) {
                                final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                mImageUri = Uri.fromFile(new File(dirPath, receipt.getId() + "x.jpg"));
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                                startActivityForResult(intent, NATIVE_ADD_PHOTO_CAMERA_REQUEST);
                            } else {
                                if (wb.android.google.camera.common.ApiHelper.NEW_SR_CAMERA_IS_SUPPORTED) {
                                    final Intent intent = new Intent(getActivity(), wb.android.google.camera.CameraActivity.class);
                                    mImageUri = Uri.fromFile(new File(dirPath, receipt.getId() + "x.jpg"));
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                                    startActivityForResult(intent, ADD_PHOTO_CAMERA_REQUEST);
                                } else {
                                    final Intent intent = new Intent(getActivity(), MyCameraActivity.class);
                                    String[] strings = new String[]{dirPath, receipt.getId() + "x.jpg"};
                                    intent.putExtra(MyCameraActivity.STRING_DATA, strings);
                                    startActivityForResult(intent, ADD_PHOTO_CAMERA_REQUEST);
                                }
                            }
                        } else if (selection.equals(receiptActionView)) { // View Photo/PDF
                            if (receipt.hasPDF()) {
                                getWorkerManager().getLogger().logEvent(ReceiptsListFragment.this, "View_PDF");
                                ReceiptsListFragment.this.showPDF(receipt);
                            } else {
                                getWorkerManager().getLogger().logEvent(ReceiptsListFragment.this, "View_Image");
                                ReceiptsListFragment.this.showImage(receipt);
                            }
                        } else if (selection.equals(receiptActionDelete)) { // Delete Receipt
                            getWorkerManager().getLogger().logEvent(ReceiptsListFragment.this, "Delete_Receipt");
                            ReceiptsListFragment.this.deleteReceipt(receipt);
                        } else if (selection.equals(receiptActionMoveCopy)) {// Move-Copy
                            getWorkerManager().getLogger().logEvent(ReceiptsListFragment.this, "Move_Copy_Receipt");
                            ReceiptsListFragment.this.moveOrCopy(receipt);
                        } else if (selection.equals(receiptActionSwapUp)) { // Swap Up
                            getWorkerManager().getLogger().logEvent(ReceiptsListFragment.this, "Swap_Up");
                            ReceiptsListFragment.this.moveReceiptUp(receipt);
                        } else if (selection.equals(receiptActionSwapDown)) { // Swap Down
                            getWorkerManager().getLogger().logEvent(ReceiptsListFragment.this, "Swap_Down");
                            ReceiptsListFragment.this.moveReceiptDown(receipt);
                        }
                    }
                    dialog.cancel();
                }
            });
        }
        builder.show();
        return true;
    }

    private void attachImageToReceipt(Attachment attachment, Receipt receipt, boolean replace) {
        File dir = mCurrentTrip.getDirectory();
        String dirPath = dir.exists() ? dir.getAbsolutePath() : getPersistenceManager().getStorageManager().mkdir(dir.getName()).getAbsolutePath();
        File file = getWorkerManager().getImageGalleryWorker().transformNativeCameraBitmap(attachment.getUri(), null, Uri.fromFile(new File(dirPath, receipt.getId() + "x.jpg")));
        if (file != null) {
            // TODO: Off UI Thread
            final Receipt retakeReceipt = getPersistenceManager().getDatabase().updateReceiptFile(receipt, file);
            if (retakeReceipt != null) {
                getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
                int stringId = replace ? R.string.toast_receipt_image_replaced : R.string.toast_receipt_image_added;
                Toast.makeText(getActivity(), getString(stringId, receipt.getName()), Toast.LENGTH_SHORT).show();
                getActivity().finish(); // Finish activity since we're done with the send action
            } else {
                Toast.makeText(getActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
                getActivity().finish(); // Finish activity since we're done with the send action
                // TODO: Add overwrite rollback here
            }
        } else {
            Toast.makeText(getActivity(), getFlexString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
            getActivity().finish(); // Finish activity since we're done with the send action
        }
    }

    private void attachPDFToReceipt(Attachment attachment, Receipt receipt, boolean replace) {
        File dir = mCurrentTrip.getDirectory();
        String dirPath = dir.exists() ? dir.getAbsolutePath() : getPersistenceManager().getStorageManager().mkdir(dir.getName()).getAbsolutePath();
        File file = new File(dirPath, receipt.getId() + "x.pdf");
        InputStream is = null;
        try {
            // TODO: Off UI Thread
            is = attachment.openUri(getActivity().getContentResolver());
            getPersistenceManager().getStorageManager().copy(is, file, true);
            if (file != null) {
                final Receipt retakeReceipt = getPersistenceManager().getDatabase().updateReceiptFile(receipt, file);
                if (retakeReceipt != null) {
                    getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
                    int stringId = replace ? R.string.toast_receipt_pdf_replaced : R.string.toast_receipt_pdf_added;
                    Toast.makeText(getActivity(), getString(stringId, receipt.getName()), Toast.LENGTH_SHORT).show();
                    getActivity().finish(); // Finish activity since we're done with the send action
                } else {
                    Toast.makeText(getActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
                    getActivity().finish(); // Finish activity since we're done with the send action
                    // TODO: Add overwrite rollback here
                }
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, e.toString());
            }
            Toast.makeText(getActivity(), getString(R.string.toast_pdf_save_error), Toast.LENGTH_SHORT).show();
            getActivity().finish();
        } catch (SecurityException e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, e.toString());
            }
            Toast.makeText(getActivity(), getString(R.string.toast_kitkat_security_error), Toast.LENGTH_LONG).show();
            getActivity().finish();
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (IOException e) {
                Log.w(TAG, e.toString());
            }
        }
    }

    private void showImage(@NonNull Receipt receipt) {
        mNavigationHandler.navigateToViewReceiptImage(receipt);
    }

    private void showPDF(@NonNull Receipt receipt) {
        mNavigationHandler.navigateToViewReceiptPdf(receipt);
    }

    public final void deleteReceipt(final Receipt receipt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.delete_item, receipt.getName())).setCancelable(true).setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                getPersistenceManager().getDatabase().deleteReceiptParallel(receipt, mCurrentTrip);
            }
        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        }).show();
    }

    public void moveOrCopy(final Receipt receipt) {
        final DatabaseHelper db = getPersistenceManager().getDatabase();
        final BetterDialogBuilder builder = new BetterDialogBuilder(getActivity());
        final View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.move_copy_dialog, null);
        final Spinner tripsSpinner = (Spinner) dialogView.findViewById(R.id.move_copy_spinner);
        List<CharSequence> trips = db.getTripNames(mCurrentTrip);
        final ArrayAdapter<CharSequence> tripNames = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, trips);
        tripNames.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tripsSpinner.setAdapter(tripNames);
        tripsSpinner.setPrompt(getString(R.string.report));
        builder.setTitle(getString(R.string.move_copy_item, receipt.getName())).setView(dialogView).setCancelable(true).setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (tripsSpinner.getSelectedItem() != null) {
                    db.moveReceiptParallel(receipt, mCurrentTrip, db.getTripByName(tripsSpinner.getSelectedItem().toString()));
                    dialog.cancel();
                } else {
                    ReceiptsListFragment.this.onReceiptMoveFailure();
                }
            }
        }).setNegativeButton(R.string.copy, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (tripsSpinner.getSelectedItem() != null) {
                    db.copyReceiptParallel(receipt, db.getTripByName(tripsSpinner.getSelectedItem().toString()));
                    dialog.cancel();
                } else {
                    ReceiptsListFragment.this.onReceiptCopyFailure();
                }
            }
        }).show();
    }

    final void moveReceiptUp(final Receipt receipt) {
        getPersistenceManager().getDatabase().moveReceiptUp(mCurrentTrip, receipt);
        getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
    }

    final void moveReceiptDown(final Receipt receipt) {
        getPersistenceManager().getDatabase().moveReceiptDown(mCurrentTrip, receipt);
        getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        showReceiptMenu(mAdapter.getItem(position));
    }

    @Override
    public void onReceiptRowsQuerySuccess(List<Receipt> receipts) {
        if (isAdded()) {
            mProgressDialog.setVisibility(View.GONE);
            getListView().setVisibility(View.VISIBLE);
            if (receipts == null || receipts.size() == 0) {
                mNoDataAlert.setVisibility(View.VISIBLE);
            } else {
                mNoDataAlert.setVisibility(View.INVISIBLE);
            }
            getPersistenceManager().getDatabase().getTripsParallel();
            mAdapter.notifyDataSetChanged(receipts);
            updateActionBarTitle(getUserVisibleHint());
        }
    }

    @Override
    public void onReceiptRowInsertSuccess(Receipt receipt) {
        getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
    }

    @Override
    public void onReceiptRowInsertFailure(SQLException ex) {
        if (isAdded()) {
            Toast.makeText(getActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReceiptRowUpdateSuccess(Receipt receipt) {
        getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
        if (isAdded()) {
            ReceiptsListFragment.this.updateActionBarTitle(getUserVisibleHint());
        }
    }

    @Override
    public void onReceiptRowUpdateFailure() {
        if (isAdded()) {
            Toast.makeText(getActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReceiptDeleteSuccess(Receipt receipt) {
        getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
        if (isAdded()) {
            if (receipt.hasFile()) {
                if (!getPersistenceManager().getStorageManager().delete(receipt.getFile())) {
                    Toast.makeText(getActivity(), getFlexString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
                }
            }
            ReceiptsListFragment.this.updateActionBarTitle(getUserVisibleHint());
        }
    }

    @Override
    public void onReceiptDeleteFailure() {
        if (isAdded()) {
            Toast.makeText(getActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReceiptCopySuccess(Trip trip) {
        getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
        getPersistenceManager().getDatabase().getTripsParallel(); // Call this to update Trip Fragments
        if (isAdded()) {
            Toast.makeText(getActivity(), getFlexString(R.string.toast_receipt_copy), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReceiptCopyFailure() {
        if (isAdded()) {
            Toast.makeText(getActivity(), getFlexString(R.string.COPY_ERROR), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReceiptMoveSuccess(Trip trip) {
        getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
        getPersistenceManager().getDatabase().getTripsParallel(); // Call this to update Trip Fragments
        if (isAdded()) {
            Toast.makeText(getActivity(), getFlexString(R.string.toast_receipt_move), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onReceiptMoveFailure() {
        if (isAdded()) {
            Toast.makeText(getActivity(), getFlexString(R.string.MOVE_ERROR), Toast.LENGTH_SHORT).show();
        }
    }

}

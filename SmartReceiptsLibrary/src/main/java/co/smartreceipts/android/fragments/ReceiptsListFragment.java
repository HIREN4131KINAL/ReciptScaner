package co.smartreceipts.android.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.imports.ActivityFileResultImporter;
import co.smartreceipts.android.imports.CameraInteractionController;
import co.smartreceipts.android.imports.FileImportListener;
import co.smartreceipts.android.imports.RequestCodes;
import co.smartreceipts.android.model.Attachment;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.ReceiptTableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import wb.android.dialog.BetterDialogBuilder;

public class ReceiptsListFragment extends ReceiptsFragment implements ReceiptTableEventsListener {

    public static final String TAG = "ReceiptsListFragment";

    // Permissions Request Ints
    private static final int PERMISSION_CAMERA_REQUEST = 21;
    private static final int PERMISSION_STORAGE_REQUEST = 22;

    // Outstate
    private static final String OUT_IMAGE_URI = "out_image_uri";

    private ReceiptTableController mReceiptTableController;
    private ReceiptCardAdapter mAdapter;
    private Receipt mHighlightedReceipt;
    private Uri mImageUri;
    private ProgressBar mProgressDialog;
    private TextView mNoDataAlert;
    private Attachable mAttachable;

    private FloatingActionMenu mFloatingActionMenu;
    private View mFloatingActionMenuActiveMaskView;

    private NavigationHandler mNavigationHandler;


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
        mReceiptTableController = getSmartReceiptsApplication().getTableControllerManager().getReceiptTableController();
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
                    getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.AddPictureReceipt);
                    addPictureReceipt();
                } else if (id == R.id.receipt_action_text) {
                    getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.AddTextReceipt);
                    addTextReceipt();
                } else if (id == R.id.receipt_action_import) {
                    getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.ImportPictureReceipt);
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
        mReceiptTableController.subscribe(this);
        mReceiptTableController.get(mCurrentTrip);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() != null && isVisibleToUser) {
            // Refresh as soon as we're visible
            mReceiptTableController.get(mCurrentTrip);
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        mFloatingActionMenu.close(false);
        mReceiptTableController.unsubscribe(this);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(OUT_IMAGE_URI, mImageUri);
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

        // Null out the last request
        final Uri cachedImageSaveLocation = mImageUri;
        mImageUri = null;

        final ActivityFileResultImporter importer = new ActivityFileResultImporter(getActivity(), mCurrentTrip, getPersistenceManager());
        importer.onActivityResult(requestCode, resultCode, data, cachedImageSaveLocation, new FileImportListener() {
            @Override
            public void onImportSuccess(@NonNull File file, int requestCode, int resultCode) {
                switch (requestCode) {
                    case RequestCodes.IMPORT_GALLERY_IMAGE:
                    case RequestCodes.IMPORT_GALLERY_PDF:
                    case RequestCodes.NATIVE_NEW_RECEIPT_CAMERA_REQUEST:
                    case RequestCodes.NEW_RECEIPT_CAMERA_REQUEST:
                        if (isResumed()) {
                            mNavigationHandler.navigateToCreateNewReceiptFragment(mCurrentTrip, file);
                        } else {
                            // TODO: How do we handle this? Via replay?
                        }
                        break;
                    case RequestCodes.NATIVE_ADD_PHOTO_CAMERA_REQUEST:
                    case RequestCodes.ADD_PHOTO_CAMERA_REQUEST:
                        final Receipt updatedReceipt = new ReceiptBuilderFactory(mHighlightedReceipt).setImage(file).build();
                        mReceiptTableController.update(mHighlightedReceipt, updatedReceipt, new DatabaseOperationMetadata());
                        break;
                }
            }

            @Override
            public void onImportFailed(@Nullable Throwable e, int requestCode, int resultCode) {
                Toast.makeText(getActivity(), getFlexString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public final void addPictureReceipt() {
        mImageUri = new CameraInteractionController(this, getPersistenceManager()).takePhoto(mCurrentTrip);
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
        final ImportPhotoPdfDialogFragment fragment = new ImportPhotoPdfDialogFragment();
        fragment.show(getFragmentManager(), ImportPhotoPdfDialogFragment.TAG);
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
                            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.ReceiptMenuEdit);
                            // ReceiptsListFragment.this.receiptMenu(mCurrentTrip, receipt, null);
                            mNavigationHandler.navigateToEditReceiptFragment(mCurrentTrip, receipt);
                        } else if (selection.equals(receiptActionCamera)) { // Take Photo
                            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.ReceiptMenuRetakePhoto);
                            mImageUri = new CameraInteractionController(ReceiptsListFragment.this, getPersistenceManager()).addPhoto(receipt);
                        } else if (selection.equals(receiptActionView)) { // View Photo/PDF
                            if (receipt.hasPDF()) {
                                getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.ReceiptMenuViewImage);
                                ReceiptsListFragment.this.showPDF(receipt);
                            } else {
                                getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.ReceiptMenuViewPdf);
                                ReceiptsListFragment.this.showImage(receipt);
                            }
                        } else if (selection.equals(receiptActionDelete)) { // Delete Receipt
                            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.ReceiptMenuDelete);
                            ReceiptsListFragment.this.deleteReceipt(receipt);
                        } else if (selection.equals(receiptActionMoveCopy)) {// Move-Copy
                            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.ReceiptMenuMoveCopy);
                            ReceiptMoveCopyDialogFragment.newInstance(receipt).show(getFragmentManager(), ReceiptMoveCopyDialogFragment.TAG);
                        } else if (selection.equals(receiptActionSwapUp)) { // Swap Up
                            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.ReceiptMenuSwapUp);
                            mReceiptTableController.swapUp(receipt);
                        } else if (selection.equals(receiptActionSwapDown)) { // Swap Down
                            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.ReceiptMenuSwapDown);
                            mReceiptTableController.swapDown(receipt);
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
        final File dir = mCurrentTrip.getDirectory();
        // TODO: Off UI Thread
        File file = getWorkerManager().getImageGalleryWorker().transformNativeCameraBitmap(attachment.getUri(), null, Uri.fromFile(new File(dir, receipt.getId() + "x.jpg")));
        if (file != null) {
            final Receipt retakeReceipt = new ReceiptBuilderFactory(receipt).setFile(file).build();
            mReceiptTableController.update(receipt, retakeReceipt, new DatabaseOperationMetadata());
        } else {
            Toast.makeText(getActivity(), getFlexString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
            getActivity().finish(); // Finish activity since we're done with the send action
        }
    }

    private void attachPDFToReceipt(Attachment attachment, Receipt receipt, boolean replace) {
        final File dir = mCurrentTrip.getDirectory();
        File file = new File(dir, receipt.getId() + "x.pdf");
        InputStream is = null;
        try {
            is = attachment.openUri(getActivity().getContentResolver());
            // TODO: Off UI Thread
            getPersistenceManager().getStorageManager().copy(is, file, true);
            final Receipt retakeReceipt = new ReceiptBuilderFactory(receipt).setFile(file).build();
            mReceiptTableController.update(receipt, retakeReceipt, new DatabaseOperationMetadata());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(getActivity(), getString(R.string.toast_pdf_save_error), Toast.LENGTH_SHORT).show();
            getActivity().finish();
        } catch (SecurityException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(getActivity(), getString(R.string.toast_kitkat_security_error), Toast.LENGTH_LONG).show();
            getActivity().finish();
        } finally {
            try {
                if (is != null) {
                    is.close();
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
                mReceiptTableController.delete(receipt, new DatabaseOperationMetadata());
            }
        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        }).show();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        showReceiptMenu(mAdapter.getItem(position));
    }

    @Override
    public void onGetSuccess(@NonNull List<Receipt> receipts, @NonNull Trip trip) {
        if (isAdded()) {
            mProgressDialog.setVisibility(View.GONE);
            getListView().setVisibility(View.VISIBLE);
            if (receipts.isEmpty()) {
                mNoDataAlert.setVisibility(View.VISIBLE);
            } else {
                mNoDataAlert.setVisibility(View.INVISIBLE);
            }
            mAdapter.notifyDataSetChanged(receipts);
            updateActionBarTitle(getUserVisibleHint());
        }
    }

    @Override
    public void onGetFailure(@Nullable Throwable e, @NonNull Trip trip) {
        Toast.makeText(getActivity(), R.string.database_get_error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGetSuccess(@NonNull List<Receipt> list) {
        // TODO: Respond?
    }

    @Override
    public void onGetFailure(@Nullable Throwable e) {
        Toast.makeText(getActivity(), R.string.database_get_error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInsertSuccess(@NonNull Receipt receipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isResumed()) {
            mReceiptTableController.get(mCurrentTrip);
        }
    }

    @Override
    public void onInsertFailure(@NonNull Receipt receipt, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded()) {
            Toast.makeText(getActivity(), getFlexString(R.string.database_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpdateSuccess(@NonNull Receipt oldReceipt, @NonNull Receipt newReceipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded()) {
            if (newReceipt.getFile() != null && !newReceipt.getFile().equals(oldReceipt.getFile())) {
                int stringId = oldReceipt.getFile() != null ? R.string.toast_receipt_image_replaced : R.string.toast_receipt_image_added;
                Toast.makeText(getActivity(), getString(stringId, newReceipt.getName()), Toast.LENGTH_SHORT).show();
            }

            mReceiptTableController.get(mCurrentTrip);
            ReceiptsListFragment.this.updateActionBarTitle(getUserVisibleHint());
        }
    }

    @Override
    public void onUpdateFailure(@NonNull Receipt oldReceipt, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded()) {
            Toast.makeText(getActivity(), getFlexString(R.string.database_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteSuccess(@NonNull Receipt receipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded()) {
            mReceiptTableController.get(mCurrentTrip);
            ReceiptsListFragment.this.updateActionBarTitle(getUserVisibleHint());
        }
    }

    @Override
    public void onDeleteFailure(@NonNull Receipt receipt, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isAdded()) {
            Toast.makeText(getActivity(), getFlexString(R.string.database_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCopySuccess(@NonNull Receipt oldReceipt, @NonNull Receipt newReceipt) {
        if (isAdded()) {
            mReceiptTableController.get(mCurrentTrip);
            Toast.makeText(getActivity(), getFlexString(R.string.toast_receipt_copy), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCopyFailure(@NonNull Receipt oldReceipt, @Nullable Throwable e) {
        if (isAdded()) {
            Toast.makeText(getActivity(), getFlexString(R.string.COPY_ERROR), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSwapSuccess() {
        mReceiptTableController.get(mCurrentTrip);
    }

    @Override
    public void onSwapFailure(@Nullable Throwable e) {
        // TODO: Respond?
    }

    @Override
    public void onMoveSuccess(@NonNull Receipt oldReceipt, @NonNull Receipt newReceipt) {
        if (isAdded()) {
            mReceiptTableController.get(mCurrentTrip);
            Toast.makeText(getActivity(), getFlexString(R.string.toast_receipt_move), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMoveFailure(@NonNull Receipt oldReceipt, @Nullable Throwable e) {
        if (isAdded()) {
            Toast.makeText(getActivity(), getFlexString(R.string.MOVE_ERROR), Toast.LENGTH_SHORT).show();
        }
    }

}

package co.smartreceipts.android.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.FragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.imports.ActivityFileResultImporter;
import co.smartreceipts.android.imports.ActivityFileResultImporterResponse;
import co.smartreceipts.android.imports.CameraInteractionController;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.utils.log.Logger;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import wb.android.google.camera.Util;
import wb.android.storage.StorageManager;
import wb.android.ui.PinchToZoomImageView;

public class ReceiptImageFragment extends WBFragment {

    // Save state
    private static final String KEY_OUT_RECEIPT = "key_out_receipt";
    private static final String KEY_OUT_URI = "key_out_uri";

    private PinchToZoomImageView mImageView;
    private LinearLayout mFooter;
    private ProgressBar mProgress;
    private Toolbar mToolbar;

    private Receipt mReceipt;
    private ActivityFileResultImporter mActivityFileResultImporter;
    private NavigationHandler mNavigationHandler;
    private ImageUpdatedListener mImageUpdatedListener;
    private CompositeSubscription mCompositeSubscription;
    private boolean mIsRotateOngoing;
    private Uri mImageUri;

    public static ReceiptImageFragment newInstance(@NonNull Receipt currentReceipt) {
        ReceiptImageFragment fragment = new ReceiptImageFragment();
        Bundle args = new Bundle();
        args.putParcelable(Receipt.PARCEL_KEY, currentReceipt);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mReceipt = getArguments().getParcelable(Receipt.PARCEL_KEY);
        } else {
            mReceipt = savedInstanceState.getParcelable(KEY_OUT_RECEIPT);
            mImageUri = savedInstanceState.getParcelable(KEY_OUT_URI);
        }
        mIsRotateOngoing = false;
        mActivityFileResultImporter = new ActivityFileResultImporter(getActivity(), getFragmentManager(), mReceipt.getTrip(), getPersistenceManager(), getSmartReceiptsApplication().getAnalyticsManager());
        mNavigationHandler = new NavigationHandler(getActivity(), new FragmentProvider());
        mImageUpdatedListener = new ImageUpdatedListener();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.receipt_image_view, container, false);
        mImageView = (PinchToZoomImageView) rootView.findViewById(R.id.receiptimagefragment_imageview);
        mFooter = (LinearLayout) rootView.findViewById(R.id.footer);
        mProgress = (ProgressBar) rootView.findViewById(R.id.progress);

        final LinearLayout rotateCCW = (LinearLayout) rootView.findViewById(R.id.rotate_ccw);
        final LinearLayout retakePhoto = (LinearLayout) rootView.findViewById(R.id.retake_photo);
        final LinearLayout rotateCW = (LinearLayout) rootView.findViewById(R.id.rotate_cw);

        rotateCCW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.ReceiptImageViewRotateCcw);
                rotate(-90);
            }
        });
        retakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.ReceiptImageViewRetakePhoto);
                mImageUri = new CameraInteractionController(ReceiptImageFragment.this).retakePhoto(mReceipt);
            }
        });
        rotateCW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmartReceiptsApplication().getAnalyticsManager().record(Events.Receipts.ReceiptImageViewRotateCw);
                rotate(90);
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadImage();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Logger.debug(this, "Result Code: " + resultCode);
        if (mReceipt == null) {
            mReceipt = getArguments().getParcelable(Receipt.PARCEL_KEY);
        }

        // Show the progress bar
        mProgress.setVisibility(View.VISIBLE);

        // Null out the last request
        final Uri cachedImageSaveLocation = mImageUri;
        mImageUri = null;

        mActivityFileResultImporter.onActivityResult(requestCode, resultCode, data, cachedImageSaveLocation);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCompositeSubscription = new CompositeSubscription();
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mReceipt.getName());
        }
        getSmartReceiptsApplication().getTableControllerManager().getReceiptTableController().subscribe(mImageUpdatedListener);
        mCompositeSubscription.add(mActivityFileResultImporter.getResultStream()
                .subscribe(new Action1<ActivityFileResultImporterResponse>() {
                    @Override
                    public void call(ActivityFileResultImporterResponse response) {
                        final Receipt retakeReceipt = new ReceiptBuilderFactory(mReceipt).setFile(response.getFile()).build();
                        getSmartReceiptsApplication().getTableControllerManager().getReceiptTableController().update(mReceipt, retakeReceipt, new DatabaseOperationMetadata());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Toast.makeText(getActivity(), getFlexString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
                        mProgress.setVisibility(View.GONE);
                        mActivityFileResultImporter.dispose();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mProgress.setVisibility(View.GONE);
                        mActivityFileResultImporter.dispose();
                    }
                }));
    }

    @Override
    public void onPause() {
        mCompositeSubscription.unsubscribe();
        mCompositeSubscription = null;
        getSmartReceiptsApplication().getTableControllerManager().getReceiptTableController().unsubscribe(mImageUpdatedListener);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");
        outState.putParcelable(KEY_OUT_RECEIPT, mReceipt);
        outState.putParcelable(KEY_OUT_URI, mImageUri);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationHandler.navigateToReportInfoFragment(mReceipt.getTrip());
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void loadImage() {
        Picasso.with(getContext()).load(mReceipt.getImage()).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).fit().centerInside().into(mImageView, new Callback() {
            @Override
            public void onSuccess() {
                mProgress.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);
                mFooter.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError() {
                mProgress.setVisibility(View.GONE);
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), getFlexString(R.string.IMG_OPEN_ERROR), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void rotate(int orientation) {
        if (mIsRotateOngoing) {
            return;
        }
        mIsRotateOngoing = true;
        mProgress.setVisibility(View.VISIBLE);
        (new ImageRotater(orientation, mReceipt.getImage())).execute();
    }

    private void onRotateComplete(boolean success) {
        if (!success) {
            Toast.makeText(getActivity(), "Image Rotate Failed", Toast.LENGTH_SHORT).show();
        }
        mIsRotateOngoing = false;
        mProgress.setVisibility(View.GONE);
    }

    private class ImageUpdatedListener extends StubTableEventsListener<Receipt> {

        @Override
        public void onUpdateSuccess(@NonNull Receipt oldReceipt, @NonNull Receipt newReceipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
            if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
                if (oldReceipt.equals(mReceipt)) {
                    mReceipt = newReceipt;
                    loadImage();
                }
            }
        }

        @Override
        public void onUpdateFailure(@NonNull Receipt oldReceipt, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
            if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
                mProgress.setVisibility(View.GONE);
                Toast.makeText(getActivity(), getFlexString(R.string.database_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ImageRotater extends AsyncTask<Void, Void, Bitmap> {

        private final int mOrientation;
        private final File mImg;

        public ImageRotater(int orientation, File img) {
            mOrientation = orientation;
            mImg = img;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                StorageManager storage = getPersistenceManager().getStorageManager();
                File root = mImg.getParentFile();
                String filename = mImg.getName();
                Bitmap bitmap = storage.getBitmap(root, filename);
                bitmap = Util.rotate(bitmap, mOrientation);
                storage.writeBitmap(root, bitmap, filename, CompressFormat.JPEG, 85);
                return bitmap;
            } catch (Exception e) {
                Logger.error(this, e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result == null) {
                onRotateComplete(false);
            } else {
                mImageView.setImageBitmap(result);
                onRotateComplete(true);
            }
        }
    }

}
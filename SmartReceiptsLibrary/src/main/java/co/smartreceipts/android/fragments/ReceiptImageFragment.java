package co.smartreceipts.android.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;

import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.DefaultFragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.legacycamera.MyCameraActivity;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.workers.ImageGalleryWorker;
import wb.android.google.camera.Util;
import wb.android.storage.StorageManager;
import wb.android.ui.PinchToZoomImageView;

public class ReceiptImageFragment extends WBFragment {

    public static final String TAG = "ReceiptImageFragment";

    // Activity Request ints
    private static final int RETAKE_PHOTO_CAMERA_REQUEST = 1;
    private static final int NATIVE_RETAKE_PHOTO_CAMERA_REQUEST = 2;

    // Settings
    private static final int FADE_IN_TIME = 75;

    private Receipt mReceipt;
    private String mReceiptPath;

    private PinchToZoomImageView mImageView;
    private LinearLayout mFooter;
    private ProgressBar mProgress;
    private Toolbar mToolbar;

    private NavigationHandler mNavigationHandler;
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
        mReceipt = getArguments().getParcelable(Receipt.PARCEL_KEY);
        mReceiptPath = mReceipt.getTrip().getDirectoryPath();
        mIsRotateOngoing = false;
        mNavigationHandler = new NavigationHandler(getActivity(), new DefaultFragmentProvider());
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(getLayoutId(), container, false);
        mImageView = (PinchToZoomImageView) rootView.findViewById(R.id.receiptimagefragment_imageview);
        mFooter = (LinearLayout) rootView.findViewById(R.id.footer);
        mProgress = (ProgressBar) rootView.findViewById(R.id.progress);

        final LinearLayout rotateCCW = (LinearLayout) rootView.findViewById(R.id.rotate_ccw);
        final LinearLayout retakePhoto = (LinearLayout) rootView.findViewById(R.id.retake_photo);
        final LinearLayout rotateCW = (LinearLayout) rootView.findViewById(R.id.rotate_cw);

        rotateCCW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWorkerManager().getLogger().logEvent(ReceiptImageFragment.this, "Rotate_CCW");
                rotate(-90);
            }
        });
        retakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWorkerManager().getLogger().logEvent(ReceiptImageFragment.this, "Retake_Photo");
                final boolean hasCameraPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
                final boolean hasWritePermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
                if (getPersistenceManager().getPreferences().useNativeCamera() || !hasCameraPermission || !hasWritePermission) {
                    final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mImageUri = Uri.fromFile(new File(mReceiptPath, mReceipt.getImage().getName()));
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                    startActivityForResult(intent, NATIVE_RETAKE_PHOTO_CAMERA_REQUEST);
                } else {
                    if (wb.android.google.camera.common.ApiHelper.NEW_SR_CAMERA_IS_SUPPORTED) {
                        final Intent intent = new Intent(getActivity(), wb.android.google.camera.CameraActivity.class);
                        mImageUri = Uri.fromFile(new File(mReceiptPath, mReceipt.getImage().getName()));
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                        startActivityForResult(intent, RETAKE_PHOTO_CAMERA_REQUEST);
                    } else {
                        final Intent intent = new Intent(getActivity(), MyCameraActivity.class);
                        String[] strings = new String[]{mReceiptPath, mReceipt.getImage().getName()};
                        intent.putExtra(MyCameraActivity.STRING_DATA, strings);
                        startActivityForResult(intent, RETAKE_PHOTO_CAMERA_REQUEST);
                    }
                }
            }
        });
        rotateCW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWorkerManager().getLogger().logEvent(ReceiptImageFragment.this, "Rotate_CW");
                rotate(90);
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new ImageLoader().execute(new File(mReceiptPath, mReceipt.getImage().getName()).getAbsolutePath());
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
    }

    public int getLayoutId() {
        return R.layout.receipt_image_view;
    }


    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.d(TAG, "Result Code: " + resultCode);
        if (mReceipt == null) {
            mReceipt = getArguments().getParcelable(Receipt.PARCEL_KEY);
            mReceiptPath = mReceipt.getTrip().getDirectoryPath();
        }

        if (resultCode == Activity.RESULT_OK) { // -1
            final ImageGalleryWorker worker = getWorkerManager().getImageGalleryWorker();
            File imgFile = worker.transformNativeCameraBitmap(mImageUri, data, null);
            if (imgFile == null) {
                Toast.makeText(getActivity(), getFlexString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
                return;
            }
            switch (requestCode) {
                case NATIVE_RETAKE_PHOTO_CAMERA_REQUEST:
                case RETAKE_PHOTO_CAMERA_REQUEST:
                    final Receipt retakeReceipt = getPersistenceManager().getDatabase().updateReceiptFile(mReceipt, imgFile);
                    if (retakeReceipt != null) {
                        mImageView.setImageBitmap(BitmapFactory.decodeFile(mReceipt.getImage().getAbsolutePath()));
                    } else {
                        Toast.makeText(getActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
                        // Add overwrite rollback here
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
                case RETAKE_PHOTO_CAMERA_REQUEST:
                    File retakeImg = new File(data.getStringExtra(MyCameraActivity.IMG_FILE));
                    final Receipt retakeReceipt = getPersistenceManager().getDatabase().updateReceiptFile(mReceipt, retakeImg);
                    if (retakeReceipt != null) {
                        mImageView.setImageBitmap(BitmapFactory.decodeFile(mReceipt.getImage().getAbsolutePath()));
                    } else {
                        Toast.makeText(getActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
                        // Add overwrite rollback here
                        return;
                    }
                    break;
                default:
                    Log.e(TAG, "Unrecognized Request Code: " + requestCode);
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Unrecgonized Result Code: " + resultCode);
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mReceipt.getName());
        }
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

    private class ImageLoader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... args) {
            if (args != null && args.length > 0 && !TextUtils.isEmpty(args[0])) {
                return BitmapFactory.decodeFile(args[0]);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (isAdded()) {
                mProgress.setVisibility(View.GONE);
                if (result != null) {
                    mImageView.setVisibility(View.VISIBLE);
                    mFooter.setVisibility(View.VISIBLE);
                    final TransitionDrawable td = new TransitionDrawable(new Drawable[]{new ColorDrawable(android.R.color.transparent), new BitmapDrawable(getResources(), result)});
                    mImageView.setImageDrawable(td);
                    td.startTransition(FADE_IN_TIME);
                } else {
                    Toast.makeText(getActivity(), getFlexString(R.string.IMG_OPEN_ERROR), Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "User has already left the activity. Ignoring result");
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
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.toString());
                }
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
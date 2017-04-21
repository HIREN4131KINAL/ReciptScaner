package co.smartreceipts.android.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.AnimRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import java.io.File;
import java.lang.ref.WeakReference;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.settings.widget.PreferenceHeaderReportOutputFragment;
import co.smartreceipts.android.settings.widget.SettingsActivity;
import co.smartreceipts.android.utils.IntentUtils;
import co.smartreceipts.android.utils.log.Logger;

import static android.preference.PreferenceActivity.EXTRA_SHOW_FRAGMENT;

public class NavigationHandler {

    private static final int DO_NOT_ANIM = 0;
    private static final int MISSING_RES_ID = -1;

    private final FragmentManager mFragmentManager;
    private final FragmentProvider mFragmentProvider;
    private final WeakReference<FragmentActivity> mFragmentActivityWeakReference;
    private final boolean mIsDualPane;

    @Inject
    public NavigationHandler(Fragment fragment) {
        this(fragment.getActivity());
    }

    public NavigationHandler(@NonNull FragmentActivity activity) {
        this(activity, new FragmentProvider());
    }

    public NavigationHandler(@NonNull FragmentActivity activity, @NonNull FragmentProvider fragmentProvider) {
        this(activity, activity.getSupportFragmentManager(), fragmentProvider, activity.getResources().getBoolean(R.bool.isTablet));
    }

    public NavigationHandler(@NonNull FragmentActivity activity, @NonNull FragmentManager fragmentManager, @NonNull FragmentProvider fragmentProvider) {
        this(activity, fragmentManager, fragmentProvider, activity.getResources().getBoolean(R.bool.isTablet));
    }

    public NavigationHandler(@NonNull FragmentActivity activity, @NonNull FragmentManager fragmentManager, @NonNull FragmentProvider fragmentProvider, boolean isDualPane) {
        mFragmentActivityWeakReference = new WeakReference<>(Preconditions.checkNotNull(activity));
        mFragmentManager = Preconditions.checkNotNull(fragmentManager);
        mFragmentProvider = Preconditions.checkNotNull(fragmentProvider);
        mIsDualPane = Preconditions.checkNotNull(isDualPane);
    }

    public void navigateToHomeTripsFragment() {
        replaceFragment(mFragmentProvider.newTripFragmentInstance(true), R.id.content_list);
    }

    public void navigateUpToTripsFragment() {
        replaceFragment(mFragmentProvider.newTripFragmentInstance(false), R.id.content_list);
    }

    public void navigateToReportInfoFragment(@NonNull Trip trip) {
        if (mIsDualPane) {
            replaceFragment(mFragmentProvider.newReportInfoFragment(trip), R.id.content_details);
        } else {
            replaceFragment(mFragmentProvider.newReportInfoFragment(trip), R.id.content_list);
        }
    }

    public void navigateToCreateNewReceiptFragment(@NonNull Trip trip, @Nullable File file, @Nullable OcrResponse ocrResponse) {
        if (mIsDualPane) {
            replaceFragmentWithAnimation(mFragmentProvider.newCreateReceiptFragment(trip, file, ocrResponse), R.id.content_details, R.anim.enter_from_bottom, DO_NOT_ANIM);
        } else {
            replaceFragmentWithAnimation(mFragmentProvider.newCreateReceiptFragment(trip, file, ocrResponse), R.id.content_list, R.anim.enter_from_bottom, DO_NOT_ANIM);
        }
    }

    public void navigateToEditReceiptFragment(@NonNull Trip trip, @NonNull Receipt receiptToEdit) {
        if (mIsDualPane) {
            replaceFragment(mFragmentProvider.newEditReceiptFragment(trip, receiptToEdit), R.id.content_details);
        } else {
            replaceFragment(mFragmentProvider.newEditReceiptFragment(trip, receiptToEdit), R.id.content_list);
        }
    }

    public void navigateToCreateTripFragment() {
        if (mIsDualPane) {
            replaceFragmentWithAnimation(mFragmentProvider.newCreateTripFragment(), R.id.content_details, R.anim.enter_from_bottom, DO_NOT_ANIM);
        } else {
            replaceFragmentWithAnimation(mFragmentProvider.newCreateTripFragment(), R.id.content_list, R.anim.enter_from_bottom, DO_NOT_ANIM);
        }
    }

    public void navigateToEditTripFragment(@NonNull Trip tripToEdit) {
        if (mIsDualPane) {
            replaceFragment(mFragmentProvider.newEditTripFragment(tripToEdit), R.id.content_details);
        } else {
            replaceFragment(mFragmentProvider.newEditTripFragment(tripToEdit), R.id.content_list);
        }
    }



    public void navigateToOcrConfigurationFragment() {
        if (mIsDualPane) {
            replaceFragment(mFragmentProvider.newOcrConfigurationFragment(), R.id.content_details);
        } else {
            replaceFragment(mFragmentProvider.newOcrConfigurationFragment(), R.id.content_list);
        }
    }

    public void navigateToViewReceiptImage(@NonNull Receipt receipt) {
        if (mIsDualPane) {
            replaceFragment(mFragmentProvider.newReceiptImageFragment(receipt), R.id.content_details);
        } else {
            replaceFragment(mFragmentProvider.newReceiptImageFragment(receipt), R.id.content_list);
        }
    }

    public void navigateToViewReceiptPdf(@NonNull Receipt receipt) {
        final FragmentActivity activity = mFragmentActivityWeakReference.get();
        if (activity != null && receipt.getFile() != null) {
            try {
                final Intent intent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Logger.debug(this, "Creating a PDF view intent with a content scheme");
                    intent = IntentUtils.getViewIntent(activity, receipt.getFile(), "application/pdf");
                } else {
                    Logger.debug(this, "Creating a PDF view intent with a file scheme");
                    intent = IntentUtils.getLegacyViewIntent(activity, receipt.getFile(), "application/pdf");
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                activity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(activity, R.string.error_no_pdf_activity_viewer, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void navigateToBackupMenu() {
        if (mIsDualPane) {
            replaceFragment(mFragmentProvider.newBackupsFragment(), R.id.content_details);
        } else {
            replaceFragment(mFragmentProvider.newBackupsFragment(), R.id.content_list);
        }
    }

    public void navigateToLoginScreen() {
        if (mIsDualPane) {
            replaceFragment(mFragmentProvider.newLoginFragment(), R.id.content_details);
        } else {
            replaceFragment(mFragmentProvider.newLoginFragment(), R.id.content_list);
        }
    }

    public void navigateToSettings() {
        final FragmentActivity activity = mFragmentActivityWeakReference.get();
        if (activity != null) {
            final Intent intent = new Intent(activity, SettingsActivity.class);
            activity.startActivity(intent);
        }
    }

    public void navigateToSettingsScrollToReportSection() {
        final FragmentActivity activity = mFragmentActivityWeakReference.get();
        if (activity != null) {
            final Intent intent = new Intent(activity, SettingsActivity.class);
            if (mIsDualPane) {
                intent.putExtra(EXTRA_SHOW_FRAGMENT, PreferenceHeaderReportOutputFragment.class.getName());
            } else {
                intent.putExtra(SettingsActivity.EXTRA_GO_TO_CATEGORY, R.string.pref_output_header_key);
            }

            activity.startActivity(intent);
        }
    }

    public boolean navigateBack() {
        try {
            return mFragmentManager.popBackStackImmediate();
        } catch (final IllegalStateException e) {
            // This exception is always thrown if saveInstanceState was already been called.
            return false;
        }
    }

    public void showDialog(@NonNull DialogFragment dialogFragment) {
        final String tag = dialogFragment.getClass().getName();
        try {
            dialogFragment.show(mFragmentManager, tag);
        } catch (IllegalStateException e) {
            // This exception is always thrown if saveInstanceState was already been called.
        }
    }

    public boolean isDualPane() {
        return mIsDualPane;
    }

    public boolean shouldFinishOnBackNaviagtion() {
        return mFragmentManager.getBackStackEntryCount() == 1;
    }

    private void replaceFragment(@NonNull Fragment fragment, @IdRes int layoutResId) {
        replaceFragmentWithAnimation(fragment, layoutResId, MISSING_RES_ID, MISSING_RES_ID);
    }

    private void replaceFragmentWithAnimation(@NonNull Fragment fragment, @IdRes int layoutResId, @AnimRes int enterAnimId, @AnimRes int exitAnimId) {
        final String tag = fragment.getClass().getName();
        boolean wasFragmentPopped;
        try {
            wasFragmentPopped = mFragmentManager.popBackStackImmediate(tag, 0);
        } catch (final IllegalStateException e) {
            // This exception is always thrown if saveInstanceState was already been called.
            wasFragmentPopped = false;
        }
        if (!wasFragmentPopped) {
            final FragmentTransaction transaction = mFragmentManager.beginTransaction();
            if (enterAnimId >= 0 && exitAnimId >= 0) {
                transaction.setCustomAnimations(enterAnimId, exitAnimId);
            }
            transaction.replace(layoutResId, fragment, tag).addToBackStack(tag).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }
    }
}

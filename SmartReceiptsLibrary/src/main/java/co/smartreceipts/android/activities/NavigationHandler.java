package co.smartreceipts.android.activities;

import android.content.Context;
import android.support.annotation.AnimRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.io.File;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;

public class NavigationHandler {

    private static final int DO_NOT_ANIM = 0;
    private static final int MISSING_RES_ID = -1;

    private final FragmentManager mFragmentManager;
    private final FragmentProvider mFragmentProvider;
    private final boolean mIsDualPane;

    public NavigationHandler(@NonNull FragmentActivity activity, @NonNull FragmentProvider fragmentProvider) {
        this(activity.getSupportFragmentManager(), fragmentProvider, activity.getResources().getBoolean(R.bool.isTablet));
    }

    public NavigationHandler(@NonNull Context context, @NonNull FragmentManager fragmentManager, @NonNull FragmentProvider fragmentProvider) {
        this(fragmentManager, fragmentProvider, context.getResources().getBoolean(R.bool.isTablet));
    }

    public NavigationHandler(@NonNull FragmentManager fragmentManager, @NonNull FragmentProvider fragmentProvider, boolean isDualPane) {
        mFragmentManager = fragmentManager;
        mFragmentProvider = fragmentProvider;
        mIsDualPane = isDualPane;
    }

    public void navigateToTripsFragment() {
        replaceFragment(mFragmentProvider.newTripFragmentInstance(), R.id.content_list);
    }

    public void navigateToReportInfoFragment(@NonNull Trip trip) {
        if (mIsDualPane) {
            replaceFragment(mFragmentProvider.newReportInfoFragment(trip), R.id.content_details);
        } else {
            replaceFragment(mFragmentProvider.newReportInfoFragment(trip), R.id.content_list);
        }
    }

    public void navigateToCreateNewReceiptFragment(@NonNull Trip trip, @Nullable File file) {
        if (mIsDualPane) {
            replaceFragmentWithAnimation(mFragmentProvider.newCreateReceiptFragment(trip, file), R.id.content_details, R.anim.enter_from_bottom, DO_NOT_ANIM);
        } else {
            replaceFragmentWithAnimation(mFragmentProvider.newCreateReceiptFragment(trip, file), R.id.content_list, R.anim.enter_from_bottom, DO_NOT_ANIM);
        }
    }

    public void navigateToEditReceiptFragment(@NonNull Trip trip, @NonNull Receipt receiptToEdit) {
        if (mIsDualPane) {
            replaceFragment(mFragmentProvider.newEditReceiptFragment(trip, receiptToEdit), R.id.content_details);
        } else {
            replaceFragment(mFragmentProvider.newEditReceiptFragment(trip, receiptToEdit), R.id.content_list);
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
        if (mIsDualPane) {
            replaceFragment(mFragmentProvider.newReceiptPdfFragment(receipt), R.id.content_details);
        } else {
            replaceFragment(mFragmentProvider.newReceiptPdfFragment(receipt), R.id.content_list);
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
        final String backstackTag = fragment.getClass().getName();
        boolean wasFragmentPopped;
        try {
            wasFragmentPopped = mFragmentManager.popBackStackImmediate(backstackTag, 0);
        } catch (final IllegalStateException e) {
            // This exception is always thrown if saveInstanceState was already been called.
            wasFragmentPopped = false;
        }
        if (!wasFragmentPopped) {
            final FragmentTransaction transaction = mFragmentManager.beginTransaction();
            if (enterAnimId >= 0 && exitAnimId >= 0) {
                transaction.setCustomAnimations(enterAnimId, exitAnimId);
            }
            transaction.replace(layoutResId, fragment).addToBackStack(backstackTag).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }
    }
}

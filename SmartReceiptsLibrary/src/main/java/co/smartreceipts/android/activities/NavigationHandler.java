package co.smartreceipts.android.activities;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
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
        // TODO: Determine what to do with tablets
        replaceFragment(mFragmentProvider.newCreateReceiptFragment(trip, file), R.id.content_list);
    }

    public void navigateToEditReceiptFragment(@NonNull Trip trip, @NonNull Receipt receiptToEdit) {
        // TODO: Determine what to do with tablets
        replaceFragment(mFragmentProvider.newEditReceiptFragment(trip, receiptToEdit), R.id.content_list);
    }

    public void navigateToViewReceiptImage(@NonNull Trip trip, @NonNull Receipt receipt) {
        // TODO: Determine what to do with tablets
        replaceFragment(mFragmentProvider.newReceiptImageFragment(trip, receipt), R.id.content_list);
    }

    public boolean isDualPane() {
        return mIsDualPane;
    }

    public boolean shouldFinishOnBackNaviagtion() {
        return mFragmentManager.getBackStackEntryCount() == 1;
    }

    private void replaceFragment(@NonNull Fragment fragment, @IdRes int layoutResId) {
        final String backstackTag = fragment.getClass().getName();
        final boolean wasFragmentPopped = mFragmentManager.popBackStackImmediate(backstackTag, 0);
        if (!wasFragmentPopped) {
            mFragmentManager.beginTransaction().replace(layoutResId, fragment).addToBackStack(backstackTag).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        }
    }
}

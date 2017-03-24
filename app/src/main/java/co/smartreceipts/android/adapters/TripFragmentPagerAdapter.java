package co.smartreceipts.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import co.smartreceipts.android.R;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.fragments.DistanceFragment;
import co.smartreceipts.android.fragments.GenerateReportFragment;
import co.smartreceipts.android.fragments.ReceiptsListFragment;

public class TripFragmentPagerAdapter extends FragmentPagerAdapter {

    private static final int FRAGMENT_COUNT = 3;

    private final Context mContext;
    private final ConfigurationManager mConfigurationManager;

    public TripFragmentPagerAdapter(@NonNull Context context, @NonNull FragmentManager fragmentManager, @NonNull ConfigurationManager configurationManager) {
        super(fragmentManager);
        mContext = context.getApplicationContext();
        mConfigurationManager = configurationManager;
    }

    @Override
    public int getCount() {
        if (mConfigurationManager.isDistanceTrackingOptionAvailable()) {
            return FRAGMENT_COUNT;
        } else {
            return FRAGMENT_COUNT - 1;
        }
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return ReceiptsListFragment.newListInstance();
        } else if (position == 1) {
            if (mConfigurationManager.isDistanceTrackingOptionAvailable()) {
                return DistanceFragment.newInstance();
            } else {
                return GenerateReportFragment.newInstance();
            }
        } else if (position == 2) {
            return GenerateReportFragment.newInstance();
        } else {
            throw new IllegalArgumentException("Unexpected Fragment Position");
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return mContext.getString(R.string.report_info_receipts);
        } else if (position == 1) {
            if (mConfigurationManager.isDistanceTrackingOptionAvailable()) {
                return mContext.getString(R.string.report_info_distance);
            } else {
                return mContext.getString(R.string.report_info_reports);
            }
        } else if (position == 2) {
            return mContext.getString(R.string.report_info_reports);
        } else {
            throw new IllegalArgumentException("Unexpected Fragment Position");
        }
    }

}

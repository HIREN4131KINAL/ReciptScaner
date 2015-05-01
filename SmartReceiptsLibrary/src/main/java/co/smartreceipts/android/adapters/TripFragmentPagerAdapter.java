package co.smartreceipts.android.adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import co.smartreceipts.android.fragments.DistanceFragment;
import co.smartreceipts.android.fragments.GenerateReportFragment;
import co.smartreceipts.android.fragments.ReceiptsListFragment;
import co.smartreceipts.android.model.Trip;

public class TripFragmentPagerAdapter extends FragmentPagerAdapter {

    private static final int FRAGMENT_COUNT = 3;

    private final Trip mTrip;

    public TripFragmentPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Trip trip) {
        super(fragmentManager);
        mTrip = trip;
    }

    @Override
    public int getCount() {
        return FRAGMENT_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return ReceiptsListFragment.newListInstance(mTrip);
        } else if (position == 1) {
            return DistanceFragment.newInstance(mTrip);
        } else if (position == 2) {
            return GenerateReportFragment.newInstance(mTrip);
        } else {
            throw new IllegalArgumentException("Unexpected Fragment Position");
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // TODO: Use strings.xml
        if (position == 0) {
            return "Receipts";
        } else if (position == 1) {
            return "Distance";
        } else if (position == 2) {
            return "Reports";
        } else {
            throw new IllegalArgumentException("Unexpected Fragment Position");
        }
    }


}

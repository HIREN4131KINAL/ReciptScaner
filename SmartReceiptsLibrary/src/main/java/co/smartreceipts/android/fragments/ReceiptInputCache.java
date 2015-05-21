package co.smartreceipts.android.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.sql.Date;

public class ReceiptInputCache {

    private final HeadlessFragment mHeadlessFragment;

    public ReceiptInputCache(@NonNull FragmentManager fragmentManager) {
        HeadlessFragment headlessFragment = (HeadlessFragment) fragmentManager.findFragmentByTag(HeadlessFragment.TAG);
        if (headlessFragment == null) {
            headlessFragment = new HeadlessFragment();
            fragmentManager.beginTransaction().add(headlessFragment, HeadlessFragment.TAG).commit();
        }
        mHeadlessFragment = headlessFragment;
    }

    public Date getCachedDate() {
        return mHeadlessFragment.mCachedDate;
    }

    public void setCachedDate(@Nullable Date cachedDate) {
        mHeadlessFragment.mCachedDate = cachedDate;
    }

    public String getCachedCategory() {
        return mHeadlessFragment.mCachedCategory;
    }

    public void setCachedCategory(@Nullable String cachedCategory) {
        mHeadlessFragment.mCachedCategory = cachedCategory;
    }

    public String getCachedCurrency() {
        return mHeadlessFragment.mCachedCurrency;
    }

    public void setCachedCurrency(@Nullable String cachedCurrency) {
        mHeadlessFragment.mCachedCurrency = cachedCurrency;
    }

    public static final class HeadlessFragment extends Fragment {

        private static final String TAG = HeadlessFragment.class.getName();

        private Date mCachedDate;
        private String mCachedCategory, mCachedCurrency;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}

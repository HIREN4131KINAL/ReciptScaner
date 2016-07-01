package co.smartreceipts.android.persistence.database.tables.controllers.impl;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import rx.subscriptions.CompositeSubscription;

public class RxSubscriptionCache {

    private final RxSubscriptionCacheHeadlessFragment mHeadlessFragment;

    public RxSubscriptionCache(@NonNull FragmentManager fragmentManager) {
        RxSubscriptionCacheHeadlessFragment headlessFragment = (RxSubscriptionCacheHeadlessFragment) fragmentManager.findFragmentByTag(RxSubscriptionCacheHeadlessFragment.TAG);
        if (headlessFragment == null || headlessFragment.compositeSubscription == null) {
            headlessFragment = new RxSubscriptionCacheHeadlessFragment();
            fragmentManager.beginTransaction().add(headlessFragment, RxSubscriptionCacheHeadlessFragment.TAG).commit();
            headlessFragment.compositeSubscription = new CompositeSubscription();
        }
        mHeadlessFragment = headlessFragment;
    }

    @NonNull
    public final CompositeSubscription getCompositeSubscription() {
        return mHeadlessFragment.compositeSubscription;
    }

    public static final class RxSubscriptionCacheHeadlessFragment extends Fragment {

        private static final String TAG = RxSubscriptionCacheHeadlessFragment.class.getName();

        private CompositeSubscription compositeSubscription;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}

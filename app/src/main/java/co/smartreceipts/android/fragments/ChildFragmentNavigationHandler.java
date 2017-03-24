package co.smartreceipts.android.fragments;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.google.common.base.Preconditions;

public class ChildFragmentNavigationHandler {

    private final FragmentManager fragmentManager;

    public ChildFragmentNavigationHandler(@NonNull Fragment fragment) {
        this.fragmentManager = Preconditions.checkNotNull(fragment.getChildFragmentManager());
    }

    public void addChild(@NonNull Fragment childFragment, @IdRes int toViewRes) {
        Preconditions.checkNotNull(childFragment);
        this.fragmentManager.beginTransaction().replace(toViewRes, childFragment).commit();
    }
}

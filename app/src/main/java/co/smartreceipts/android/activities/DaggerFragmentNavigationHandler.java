package co.smartreceipts.android.activities;

import android.support.v4.app.Fragment;

import javax.inject.Inject;

public class DaggerFragmentNavigationHandler<T extends Fragment> extends NavigationHandler {

    @Inject
    public DaggerFragmentNavigationHandler(T t) {
        super(t);
    }
}

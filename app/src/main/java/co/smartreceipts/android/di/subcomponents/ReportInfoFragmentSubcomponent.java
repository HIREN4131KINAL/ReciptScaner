package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.fragments.ReportInfoFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface ReportInfoFragmentSubcomponent extends AndroidInjector<ReportInfoFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<ReportInfoFragment> {

    }
}

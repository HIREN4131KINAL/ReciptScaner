package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.FragmentScope;
import co.smartreceipts.android.fragments.GenerateReportFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface GenerateReportFragmentSubcomponent extends AndroidInjector<GenerateReportFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<GenerateReportFragment>{

    }
}

package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.rating.RatingDialogFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface RatingDialogFragmentSubcomponent extends AndroidInjector<RatingDialogFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<RatingDialogFragment> {

    }
}

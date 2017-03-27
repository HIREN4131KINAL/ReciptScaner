package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.rating.FeedbackDialogFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface FeedbackDialogFragmentSubcomponent extends AndroidInjector<FeedbackDialogFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<FeedbackDialogFragment> {

    }
}

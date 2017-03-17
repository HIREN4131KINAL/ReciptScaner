package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.FragmentScope;
import co.smartreceipts.android.fragments.InformAboutPdfImageAttachmentDialogFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface InformAboutPdfImageAttachmentDialogFragmentSubcomponent
        extends AndroidInjector<InformAboutPdfImageAttachmentDialogFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<InformAboutPdfImageAttachmentDialogFragment> {

    }
}

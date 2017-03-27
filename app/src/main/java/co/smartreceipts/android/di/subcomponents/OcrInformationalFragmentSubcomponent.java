package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.ocr.info.OcrInformationalFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface OcrInformationalFragmentSubcomponent extends AndroidInjector<OcrInformationalFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<OcrInformationalFragment> {

    }
}

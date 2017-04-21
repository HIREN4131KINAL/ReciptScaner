package co.smartreceipts.android.ocr.widget.di;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.ocr.widget.configuration.OcrConfigurationFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent(modules = { OcrConfigurationModule.class })
public interface OcrConfigurationFragmentSubcomponent extends AndroidInjector<OcrConfigurationFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<OcrConfigurationFragment> {

    }
}

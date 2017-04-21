package co.smartreceipts.android.ocr.widget.di;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.ocr.widget.tooltip.OcrInformationalTooltipFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface OcrInformationalTooltipFragmentSubcomponent extends AndroidInjector<OcrInformationalTooltipFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<OcrInformationalTooltipFragment> {

    }
}

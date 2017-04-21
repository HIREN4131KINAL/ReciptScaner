package co.smartreceipts.android.ocr.widget.di;

import co.smartreceipts.android.ocr.widget.configuration.OcrConfigurationFragment;
import co.smartreceipts.android.ocr.widget.configuration.OcrConfigurationView;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class OcrConfigurationModule {

    @Binds
    abstract OcrConfigurationView provideOcrConfigurationView(OcrConfigurationFragment fragment);

}

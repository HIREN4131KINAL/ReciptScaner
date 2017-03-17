package co.smartreceipts.android.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import wb.android.flex.Flex;
import wb.android.flex.Flexable;

@Module
public class PresentationModule {

    @Provides
    @Singleton
    public static Flex provideFlex(Context context) {
        return new Flex(context, new Flexable() {
            @Override
            public int getFleXML() {
                return Flexable.UNDEFINED;
            }
        });
    }
}

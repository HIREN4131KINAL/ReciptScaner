package co.smartreceipts.android;

import android.content.Context;

import com.bugsense.trace.BugSenseHandler;

import javax.inject.Inject;

public class ExtraInitializerFreeImpl implements ExtraInitializer{

    @Inject
    Context context;

    @Inject
    public ExtraInitializerFreeImpl() {
    }

    @Override
    public void init() {
        BugSenseHandler.initAndStartSession(context, "01de172a");
    }
}

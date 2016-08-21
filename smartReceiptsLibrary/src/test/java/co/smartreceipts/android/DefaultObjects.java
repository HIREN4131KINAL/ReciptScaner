package co.smartreceipts.android;

import java.io.File;
import java.sql.Date;
import java.util.Collections;
import java.util.TimeZone;

import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.impl.DefaultTripImpl;
import co.smartreceipts.android.sync.SyncProvider;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import co.smartreceipts.android.sync.model.impl.Identifier;
import co.smartreceipts.android.sync.model.impl.IdentifierMap;
import co.smartreceipts.android.sync.model.impl.MarkedForDeletionMap;

public class DefaultObjects {

    private DefaultObjects() { }

    public static Trip newDefaultTrip() {
        return new DefaultTripImpl(new File(new File("").getAbsolutePath()),
                                  new Date(System.currentTimeMillis()),
                                  TimeZone.getDefault(),
                                  new Date(System.currentTimeMillis()),
                                  TimeZone.getDefault(),
                                  WBCurrency.getDefault(),
                                  "comment",
                                  "costCenter",
                                  null,
                                  Source.Undefined);
    }

    public static SyncState newDefaultSyncState() {
        return new DefaultSyncState(new IdentifierMap(Collections.singletonMap(SyncProvider.GoogleDrive, new Identifier("abc"))),
                                    new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, true)),
                                    new Date(System.currentTimeMillis()));
    }
}

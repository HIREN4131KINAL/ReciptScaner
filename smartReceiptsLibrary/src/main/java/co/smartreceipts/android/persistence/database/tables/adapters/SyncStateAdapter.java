package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.sql.Date;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.persistence.database.tables.AbstractSqlTable;
import co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import co.smartreceipts.android.sync.model.impl.IdentifierMap;
import co.smartreceipts.android.sync.model.impl.MarkedForDeletionMap;

public final class SyncStateAdapter {

    private final Gson mGson = new Gson();

    @NonNull
    public SyncState read(@NonNull Cursor cursor) {
        final int identifierIndex = cursor.getColumnIndex(AbstractSqlTable.COLUMN_SYNC_ID);
        final int markedForDeletionIndex = cursor.getColumnIndex(AbstractSqlTable.COLUMN_MARKED_FOR_DELETION);
        final int lastLocalModificationTimeIndex = cursor.getColumnIndex(AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME);

        final String identifierMapJson = cursor.getString(identifierIndex);
        final String markedForDeletionJson = cursor.getString(markedForDeletionIndex);
        final long lastLocalModificationTimeLong = cursor.getLong(lastLocalModificationTimeIndex);

        final IdentifierMap identifierMap = mGson.fromJson(identifierMapJson, IdentifierMap.class);
        final MarkedForDeletionMap markedForDeletionMap = mGson.fromJson(markedForDeletionJson, MarkedForDeletionMap.class);
        final Date lastLocalModificationTime = new Date(lastLocalModificationTimeLong);
        return new DefaultSyncState(identifierMap, markedForDeletionMap, lastLocalModificationTime);
    }

    @NonNull
    public ContentValues write(@NonNull SyncState syncState) {
        final ContentValues values = new ContentValues();
        values.put(AbstractSqlTable.COLUMN_SYNC_ID, mGson.toJson(syncState.getIdentifierMap()));
        values.put(AbstractSqlTable.COLUMN_MARKED_FOR_DELETION, mGson.toJson(syncState.getMarkedForDeletionMap()));
        values.put(AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME, syncState.getLastLocalModificationTime().getTime());
        return values;
    }

}

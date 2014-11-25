package co.smartreceipts.android.sync.id;

import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Operates as a unique identifier for synchronization data
 */
public interface Identifier extends Parcelable {

    /**
     * @return - the {@link java.lang.String} representation of this id
     */
    @NonNull
    String getId();
}

package co.smartreceipts.android.sync.user;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import co.smartreceipts.android.sync.id.Identifier;

/**
 * Defines a number of core methods that all {@link co.smartreceipts.android.sync.user.SyncUser} objects should
 * define.
 *
 * @author williambaumann
 */
public interface SyncUser extends Parcelable {

    /**
     * @return - the {@link co.smartreceipts.android.sync.id.Identifier} for this user.
     */
    @NonNull
    Identifier getId();

    /**
     * @return - the {@link java.lang.String} representation of the username for this user.
     */
    @NonNull
    String getUsername();

    /**
     * @return - the {@link java.lang.String} representation of the email address for this user or
     * {@code null} if none was provided.
     */
    String getEmail();

}

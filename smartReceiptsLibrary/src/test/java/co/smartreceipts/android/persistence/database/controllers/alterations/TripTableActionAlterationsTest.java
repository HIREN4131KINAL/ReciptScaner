package co.smartreceipts.android.persistence.database.controllers.alterations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.tables.Table;
import rx.Observable;
import wb.android.storage.StorageManager;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class TripTableActionAlterationsTest {

    TripTableActionAlterations mTripTableActionAlterations;

    @Mock
    Table<Trip, String> mTripsTable;

    @Mock
    DatabaseHelper mDatabaseHelper;

    @Mock
    StorageManager mStorageManager;

    @Mock
    Trip mTrip1;

    @Mock
    Trip mTrip2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mTripTableActionAlterations = new TripTableActionAlterations(mTripsTable, mDatabaseHelper, mStorageManager);
    }

    @Test
    public void postGet() throws Exception {
        mTripTableActionAlterations.postGet(Arrays.asList(mTrip1, mTrip2));
        verify(mDatabaseHelper).getTripPriceAndDailyPrice(mTrip1);
        verify(mDatabaseHelper).getTripPriceAndDailyPrice(mTrip2);
    }

    @Test
    public void postInsertForNullTrip() throws Exception {
        mTripTableActionAlterations.postInsert(null); // No exceptions
        verify(mTripsTable, never()).delete(mTrip1);
    }

    @Test
    public void postInsertForValidlTrip() throws Exception {
        final String name = "name";
        when(mTrip1.getName()).thenReturn(name);
        when(mStorageManager.mkdir(name)).thenReturn(new File(name));
        mTripTableActionAlterations.postInsert(mTrip1); // No exceptions
        verify(mTripsTable, never()).delete(mTrip1);
    }

    @Test (expected = IOException.class)
    public void postInsertForValidlTripButIOFails() throws Exception {
        final String name = "name";
        when(mTrip1.getName()).thenReturn(name);
        when(mStorageManager.mkdir(name)).thenReturn(null);
        when(mTripsTable.delete(mTrip1)).thenReturn(Observable.just(true));
        mTripTableActionAlterations.postInsert(mTrip1);
        verify(mTripsTable).delete(mTrip1);
    }

    @Test
    public void postUpdateForNullTrip() throws Exception {
        mTripTableActionAlterations.postUpdate(mTrip1, null);
    }

    @Test
    public void postUpdateForValidlTrip() throws Exception {
        final String name1 = "name1";
        final String name2 = "name2";
        when(mTrip1.getName()).thenReturn(name1);
        when(mTrip2.getName()).thenReturn(name2);
        when(mTrip1.getDirectory()).thenReturn(new File(name1));
        when(mStorageManager.rename(new File(name1), name2)).thenReturn(new File(name2));
        mTripTableActionAlterations.postUpdate(mTrip1, mTrip2);
        verify(mTripsTable, never()).update(mTrip2, mTrip1);
    }

    @Test (expected = IOException.class)
    public void postUpdateForValidlTripButIOFails() throws Exception {
        final String name1 = "name1";
        final String name2 = "name2";
        when(mTrip1.getName()).thenReturn(name1);
        when(mTrip2.getName()).thenReturn(name2);
        when(mTrip1.getDirectory()).thenReturn(new File(name1));
        when(mStorageManager.rename(new File(name1), name2)).thenReturn(new File(name1));
        when(mTripsTable.update(mTrip2, mTrip1)).thenReturn(Observable.just(mTrip1));
        mTripTableActionAlterations.postUpdate(mTrip1, mTrip2);
        verify(mTripsTable).update(mTrip2, mTrip1);
    }

    @Test
    public void postDeleteFailed() throws Exception {
        mTripTableActionAlterations.postDelete(false, mTrip1);
    }

    @Test
    public void postDeleteSuccess() throws Exception {
        final File dir = new File("name");
        when(mTrip1.getDirectory()).thenReturn(dir);
        mTripTableActionAlterations.postDelete(true, mTrip1);
        verify(mStorageManager).deleteRecursively(dir);
    }

}
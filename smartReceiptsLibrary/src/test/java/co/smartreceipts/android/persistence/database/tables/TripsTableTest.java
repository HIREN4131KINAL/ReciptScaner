package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.verification.VerificationMode;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.TripBuilderFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import wb.android.storage.StorageManager;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class TripsTableTest {

    private static final String NAME_1 = "Trip1";
    private static final String NAME_2 = "Trip2";
    private static final String NAME_3 = "Trip3";

    // Use the to verify that sort ordering is on the End Date (i.e. End3 > End1 > End2)
    private static final long START_DATE_2 = 1409703721000L;
    private static final long START_DATE_1 = 1409703722000L;
    private static final long END_DATE_2   = 1409703793000L;
    private static final long END_DATE_1   = 1409703794000L;
    private static final long START_DATE_3 = 1409703891000L;
    private static final long END_DATE_3   = 1409703893000L;

    private static final String START_TIMEZONE = TimeZone.getAvailableIDs()[0];
    private static final String END_TIMEZONE = TimeZone.getAvailableIDs()[1];
    private static final String COMMENT = "Comment";
    private static final String COST_CENTER = "Cost Center";
    private static final String CURRENCY_CODE = "USD";
    private static final String USER_PREFERENCES_CURRENCY_CODE = "EUR";

    // Class under test
    TripsTable mTripsTable;

    @Mock
    SQLiteDatabase mSQLiteDatabase;

    @Mock
    TableDefaultsCustomizer mTableDefaultsCustomizer;

    @Mock
    PersistenceManager mPersistenceManager;
    
    @Mock
    StorageManager mStorageManager;
    
    @Mock
    Preferences mPreferences;

    @Captor
    ArgumentCaptor<String> mSqlCaptor;
    
    SQLiteOpenHelper mSQLiteOpenHelper;

    Trip mTrip1;

    Trip mTrip2;

    TripBuilderFactory mBuilder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        mSQLiteOpenHelper = new TestSQLiteOpenHelper(RuntimeEnvironment.application);
        
        when(mPersistenceManager.getStorageManager()).thenReturn(mStorageManager);
        when(mPersistenceManager.getPreferences()).thenReturn(mPreferences);
        when(mStorageManager.getFile(NAME_1)).thenReturn(new File(NAME_1));
        when(mStorageManager.getFile(NAME_2)).thenReturn(new File(NAME_2));
        when(mStorageManager.getFile(NAME_3)).thenReturn(new File(NAME_3));
        when(mStorageManager.mkdir(NAME_1)).thenReturn(new File(NAME_1));
        when(mStorageManager.mkdir(NAME_2)).thenReturn(new File(NAME_2));
        when(mStorageManager.mkdir(NAME_3)).thenReturn(new File(NAME_3));
        when(mPreferences.getDefaultCurreny()).thenReturn(USER_PREFERENCES_CURRENCY_CODE);
        
        mTripsTable = new TripsTable(mSQLiteOpenHelper, mPersistenceManager);

        // Now create the table and insert some defaults
        mTripsTable.onCreate(mSQLiteOpenHelper.getWritableDatabase(), mTableDefaultsCustomizer);
        mBuilder = new TripBuilderFactory();
        mBuilder.setStartTimeZone(START_TIMEZONE).setEndTimeZone(END_TIMEZONE).setComment(COMMENT).setCostCenter(COST_CENTER).setDefaultCurrency(CURRENCY_CODE, mPreferences.getDefaultCurreny());
        mTrip1 = mTripsTable.insert(mBuilder.setStartDate(START_DATE_1).setEndDate(END_DATE_1).setDirectory(mStorageManager.getFile(NAME_1)).build(), new DatabaseOperationMetadata()).toBlocking().first();
        mTrip2 = mTripsTable.insert(mBuilder.setStartDate(START_DATE_2).setEndDate(END_DATE_2).setDirectory(mStorageManager.getFile(NAME_2)).build(), new DatabaseOperationMetadata()).toBlocking().first();
    }

    @After
    public void tearDown() {
        mSQLiteOpenHelper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS " + mTripsTable.getTableName());
    }

    @Test
    public void getTableName() {
        assertEquals("trips", mTripsTable.getTableName());
    }

    @Test
    public void onCreate() {
        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mTripsTable.onCreate(mSQLiteDatabase, customizer);
        verify(mSQLiteDatabase).execSQL(mSqlCaptor.capture());
        verifyZeroInteractions(customizer);

        assertTrue(mSqlCaptor.getValue().contains("CREATE TABLE trips")); // Table name
        assertTrue(mSqlCaptor.getValue().contains("name TEXT PRIMARY KEY"));
        assertTrue(mSqlCaptor.getValue().contains("from_date DATE"));
        assertTrue(mSqlCaptor.getValue().contains("to_date DATE"));
        assertTrue(mSqlCaptor.getValue().contains("from_timezone TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("to_timezone TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("trips_comment TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("trips_cost_center TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("trips_default_currency TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("trips_filters TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("trip_processing_status TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("drive_sync_id TEXT"));
        assertTrue(mSqlCaptor.getValue().contains("drive_is_synced BOOLEAN DEFAULT 0"));
        assertTrue(mSqlCaptor.getValue().contains("drive_marked_for_deletion BOOLEAN DEFAULT 0"));
        assertTrue(mSqlCaptor.getValue().contains("last_local_modification_time DATE"));
    }

    @Test
    public void onUpgradeFromV8() {
        final int oldVersion = 8;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mTripsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV8Upgrade(times(1));
        verifyV10Upgrade(times(1));
        verifyV11Upgrade(times(1));
        verifyV12Upgrade(times(1));
        verifyV14Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV10() {
        final int oldVersion = 10;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mTripsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV8Upgrade(never());
        verifyV10Upgrade(times(1));
        verifyV11Upgrade(times(1));
        verifyV12Upgrade(times(1));
        verifyV14Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV11() {
        final int oldVersion = 11;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mTripsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV8Upgrade(never());
        verifyV10Upgrade(never());
        verifyV11Upgrade(times(1));
        verifyV12Upgrade(times(1));
        verifyV14Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV12() {
        final int oldVersion = 12;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mTripsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV8Upgrade(never());
        verifyV10Upgrade(never());
        verifyV11Upgrade(never());
        verifyV12Upgrade(times(1));
        verifyV14Upgrade(times(1));
    }

    @Test
    public void onUpgradeFromV14() {
        final int oldVersion = 14;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mTripsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verifyZeroInteractions(customizer);
        verifyV8Upgrade(never());
        verifyV10Upgrade(never());
        verifyV11Upgrade(never());
        verifyV12Upgrade(never());
        verifyV14Upgrade(times(1));
    }

    private void verifyV8Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE trips ADD from_timezone TEXT");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE trips ADD to_timezone TEXT");
    }

    private void verifyV10Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE trips ADD trips_comment TEXT");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE trips ADD trips_default_currency TEXT");
    }

    private void verifyV11Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE trips ADD trips_filters TEXT");
    }

    private void verifyV12Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE trips ADD trips_cost_center TEXT");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE trips ADD trip_processing_status TEXT");
    }

    private void verifyV14Upgrade(@NonNull VerificationMode verificationMode) {
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + mTripsTable.getTableName() + " ADD drive_sync_id TEXT");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + mTripsTable.getTableName() + " ADD drive_is_synced BOOLEAN DEFAULT 0");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + mTripsTable.getTableName() + " ADD drive_marked_for_deletion BOOLEAN DEFAULT 0");
        verify(mSQLiteDatabase, verificationMode).execSQL("ALTER TABLE " + mTripsTable.getTableName() + " ADD last_local_modification_time DATE");
    }

    @Test
    public void onUpgradeAlreadyOccurred() {
        final int oldVersion = DatabaseHelper.DATABASE_VERSION;
        final int newVersion = DatabaseHelper.DATABASE_VERSION;

        final TableDefaultsCustomizer customizer = mock(TableDefaultsCustomizer.class);
        mTripsTable.onUpgrade(mSQLiteDatabase, oldVersion, newVersion, customizer);
        verify(mSQLiteDatabase, never()).execSQL(mSqlCaptor.capture());
        verifyZeroInteractions(customizer);
    }

    @Test
    public void get() {
        final List<Trip> trips = mTripsTable.get().toBlocking().first();
        assertEquals(trips, Arrays.asList(mTrip1, mTrip2));
    }

    @Test
    public void insert() {
        final Trip trip = mTripsTable.insert(mBuilder.setStartDate(START_DATE_3).setEndDate(END_DATE_3).setDirectory(mStorageManager.getFile(NAME_3)).build(), new DatabaseOperationMetadata()).toBlocking().first();
        assertNotNull(trip);

        final List<Trip> trips = mTripsTable.get().toBlocking().first();
        // Also confirm the new one is first b/c of date ordering
        assertEquals(trips, Arrays.asList(trip, mTrip1, mTrip2));
    }

    @Test
    public void findByPrimaryKey() {
        final Trip trip = mTripsTable.findByPrimaryKey(NAME_1).toBlocking().first();
        assertNotNull(trip);
        assertEquals(mTrip1, trip);
    }

    @Test
    public void findByPrimaryMissingKey() {
        final Trip trip = mTripsTable.findByPrimaryKey("").toBlocking().first();
        assertNull(trip);
    }

    @Test
    public void update() {
        final Trip updatedTrip = mTripsTable.update(mTrip1, mBuilder.setDirectory(mStorageManager.getFile(NAME_3)).build(), new DatabaseOperationMetadata()).toBlocking().first();
        assertNotNull(updatedTrip);
        assertFalse(mTrip1.equals(updatedTrip));

        final List<Trip> trips = mTripsTable.get().toBlocking().first();
        assertEquals(trips, Arrays.asList(updatedTrip, mTrip2));
    }

    @Test
    public void delete() {
        assertTrue(mTripsTable.delete(mTrip1, new DatabaseOperationMetadata()).toBlocking().first());

        final List<Trip> trips = mTripsTable.get().toBlocking().first();
        assertEquals(trips, Collections.singletonList(mTrip2));
    }

}
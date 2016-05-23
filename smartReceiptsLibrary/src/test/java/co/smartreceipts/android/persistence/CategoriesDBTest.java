package co.smartreceipts.android.persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class CategoriesDBTest {

    private SmartReceiptsApplication mApp;
    private DatabaseHelper mDB;

    @Before
    public void setup() {
        mApp = (SmartReceiptsApplication) RuntimeEnvironment.application;
        mDB = mApp.getPersistenceManager().getDatabase();
    }

    @After
    public void tearDown() {
        mDB.close();
        mDB = null;
        mApp = null;
    }

    @Test
    public void getCategories() {
        ArrayList<CharSequence> categories = mDB.getCategoriesList();
        assertNotNull(categories);
        assertTrue(categories.size() > 0);
        assertTrue(categories.contains(mApp.getString(R.string.category_airfare)));
        assertTrue(categories.contains(mApp.getString(R.string.category_books_periodicals)));
        assertTrue(categories.contains(mApp.getString(R.string.category_breakfast)));
        assertTrue(categories.contains(mApp.getString(R.string.category_car_rental)));
        assertTrue(categories.contains(mApp.getString(R.string.category_cell_phone)));
        assertTrue(categories.contains(mApp.getString(R.string.category_dinner)));
        assertTrue(categories.contains(mApp.getString(R.string.category_dues_subscriptions)));
        assertTrue(categories.contains(mApp.getString(R.string.category_entertainment)));
        assertTrue(categories.contains(mApp.getString(R.string.category_gasoline)));
        assertTrue(categories.contains(mApp.getString(R.string.category_gift)));
        assertTrue(categories.contains(mApp.getString(R.string.category_hotel)));
        assertTrue(categories.contains(mApp.getString(R.string.category_laundry)));
        assertTrue(categories.contains(mApp.getString(R.string.category_lunch)));
        assertTrue(categories.contains(mApp.getString(R.string.category_meals_justified)));
        assertTrue(categories.contains(mApp.getString(R.string.category_null)));
        assertTrue(categories.contains(mApp.getString(R.string.category_other)));
        assertTrue(categories.contains(mApp.getString(R.string.category_parking_tolls)));
        assertTrue(categories.contains(mApp.getString(R.string.category_postage_shipping)));
        assertTrue(categories.contains(mApp.getString(R.string.category_stationery_stations)));
        assertTrue(categories.contains(mApp.getString(R.string.category_taxi_bus)));
        assertTrue(categories.contains(mApp.getString(R.string.category_telephone_fax)));
        assertTrue(categories.contains(mApp.getString(R.string.category_tip)));
        assertTrue(categories.contains(mApp.getString(R.string.category_train)));
        assertTrue(categories.contains(mApp.getString(R.string.category_training_fees)));

        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_airfare)), mApp.getString(R.string.category_airfare_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_books_periodicals)), mApp.getString(R.string.category_books_periodicals_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_breakfast)), mApp.getString(R.string.category_breakfast_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_car_rental)), mApp.getString(R.string.category_car_rental_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_cell_phone)), mApp.getString(R.string.category_cell_phone_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_dinner)), mApp.getString(R.string.category_dinner_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_dues_subscriptions)), mApp.getString(R.string.category_dues_subscriptions_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_entertainment)), mApp.getString(R.string.category_entertainment_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_gasoline)), mApp.getString(R.string.category_gasoline_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_gift)), mApp.getString(R.string.category_gift_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_hotel)), mApp.getString(R.string.category_hotel_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_laundry)), mApp.getString(R.string.category_laundry_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_lunch)), mApp.getString(R.string.category_lunch_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_meals_justified)), mApp.getString(R.string.category_meals_justified_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_null)), mApp.getString(R.string.category_null_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_other)), mApp.getString(R.string.category_other_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_parking_tolls)), mApp.getString(R.string.category_parking_tolls_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_postage_shipping)), mApp.getString(R.string.category_postage_shipping_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_stationery_stations)), mApp.getString(R.string.category_stationery_stations_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_taxi_bus)), mApp.getString(R.string.category_taxi_bus_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_telephone_fax)), mApp.getString(R.string.category_telephone_fax_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_tip)), mApp.getString(R.string.category_tip_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_train)), mApp.getString(R.string.category_train_code));
        assertEquals(mDB.getCategoryCode(mApp.getString(R.string.category_training_fees)), mApp.getString(R.string.category_training_fees_code));
    }

    @Test
    public void insert() {
        final ArrayList<CharSequence> oldCategories = mDB.getCategoriesList();
        int oldSize = oldCategories.size();
        assertTrue(mDB.insertCategory("New", "New"));
        final ArrayList<CharSequence> newCategories = mDB.getCategoriesList();
        assertEquals(oldSize + 1, newCategories.size());
        assertTrue(newCategories.contains("New"));
        assertEquals(mDB.getCategoryCode("New"), "New");
        assertEquals(oldCategories, newCategories); // Since the DB updates these lists
    }

    @Test
    public void insertNoCache() {
        final ArrayList<CharSequence> oldCategories = mDB.getCategoriesList();
        int oldSize = oldCategories.size();
        assertTrue(mDB.insertCategoryNoCache("New", "New"));
        final ArrayList<CharSequence> newCategories = mDB.getCategoriesList();
        assertEquals(oldSize, newCategories.size());
        assertFalse(newCategories.contains("New"));
        assertNull(mDB.getCategoryCode("New"));
        assertEquals(oldCategories, newCategories); // Since the DB updates these lists
    }

    @Test
    public void update() {
        final String lunch = mApp.getString(R.string.category_lunch);
        final ArrayList<CharSequence> oldCategories = mDB.getCategoriesList();
        int oldSize = oldCategories.size();
        assertTrue(mDB.updateCategory(mApp.getString(R.string.category_lunch), "New", "New"));
        final ArrayList<CharSequence> newCategories = mDB.getCategoriesList();
        assertEquals(oldSize, newCategories.size());
        assertTrue(newCategories.contains("New"));
        assertEquals(mDB.getCategoryCode("New"), "New");
        assertFalse(newCategories.contains(lunch));
        assertNull(mDB.getCategoryCode(lunch));
        assertEquals(oldCategories, newCategories); // Since the DB updates these lists
    }

    @Test
    public void delete() {
        final String lunch = mApp.getString(R.string.category_lunch);
        final ArrayList<CharSequence> oldCategories = mDB.getCategoriesList();
        int oldSize = oldCategories.size();
        assertTrue(mDB.deleteCategory(lunch));
        final ArrayList<CharSequence> newCategories = mDB.getCategoriesList();
        assertEquals(oldSize - 1, newCategories.size());
        assertFalse(newCategories.contains(lunch));
        assertNull(mDB.getCategoryCode(lunch));
        assertEquals(oldCategories, newCategories); // Since the DB updates these lists
    }

}

package co.smartreceipts.android.model.impl;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

import java.sql.Date;
import java.util.Collections;

import co.smartreceipts.android.DefaultObjects;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.sync.SyncProvider;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import co.smartreceipts.android.sync.model.impl.Identifier;
import co.smartreceipts.android.sync.model.impl.IdentifierMap;
import co.smartreceipts.android.sync.model.impl.MarkedForDeletionMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricGradleTestRunner.class)
public class ImmutableCategoryImplTest {

    private static final String NAME = "name";
    private static final String CODE = "code";

    // Class under test
    ImmutableCategoryImpl mImmutableCategory;

    SyncState mSyncState;

    @Before
    public void setUp() throws Exception {
        mSyncState = DefaultObjects.newDefaultSyncState();
        mImmutableCategory = new ImmutableCategoryImpl(NAME, CODE, mSyncState);
    }

    @Test
    public void getName() {
        assertEquals(NAME, mImmutableCategory.getName());
    }

    @Test
    public void getCode() {
        assertEquals(CODE, mImmutableCategory.getCode());
    }

    @Test
    public void getSyncState() {
        assertEquals(mSyncState, mImmutableCategory.getSyncState());
    }

    @Test
    public void compareTo() {
        assertTrue(mImmutableCategory.compareTo(mImmutableCategory) == 0);
        assertTrue(mImmutableCategory.compareTo(new ImmutableCategoryImpl("aaa", "")) > 0);
        assertTrue(mImmutableCategory.compareTo(new ImmutableCategoryImpl("zzz", "")) < 0);
    }

    @Test
    public void equals() {
        assertEquals(mImmutableCategory, mImmutableCategory);
        assertEquals(mImmutableCategory, new ImmutableCategoryImpl(NAME, CODE, mSyncState));
        assertThat(mImmutableCategory, not(equalTo(new Object())));
        assertThat(mImmutableCategory, not(equalTo(mock(Category.class))));
        assertThat(mImmutableCategory, not(equalTo(new ImmutableCategoryImpl("wrong", CODE, mSyncState))));
        assertThat(mImmutableCategory, not(equalTo(new ImmutableCategoryImpl(NAME, "wrong", mSyncState))));
        assertThat(mImmutableCategory, not(equalTo(new ImmutableCategoryImpl(NAME, CODE, new DefaultSyncState()))));
    }

    @Test
    public void parcelEquality() {
        final Parcel parcel = Parcel.obtain();
        mImmutableCategory.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        final ImmutableCategoryImpl category = ImmutableCategoryImpl.CREATOR.createFromParcel(parcel);
        assertNotNull(category);
        assertEquals(category, mImmutableCategory);
    }

}
package co.smartreceipts.android.persistence.database.controllers.alterations;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.Collections;

@RunWith(RobolectricGradleTestRunner.class)
public class StubTableActionAlterationsTest {

    StubTableActionAlterations<Object> mStubTableActionAlterations;

    @Before
    public void setUp() throws Exception {
        mStubTableActionAlterations = new StubTableActionAlterations<>();
    }

    @Test
    public void preGet() throws Exception {
        Assert.assertNotNull(mStubTableActionAlterations);
    }

    @Test
    public void postGet() throws Exception {
        // Verify we don't throw an exception
        mStubTableActionAlterations.postGet(Collections.emptyList());
    }

    @Test
    public void preInsert() throws Exception {
        Assert.assertNotNull(mStubTableActionAlterations.preInsert(new Object()));
    }

    @Test
    public void postInsert() throws Exception {
        mStubTableActionAlterations.postInsert(new Object());
    }

    @Test
    public void preUpdate() throws Exception {
        Assert.assertNotNull(mStubTableActionAlterations.preUpdate(new Object(), new Object()));
    }

    @Test
    public void postUpdate() throws Exception {
        mStubTableActionAlterations.postUpdate(new Object(), new Object());
    }

    @Test
    public void preDelete() throws Exception {
        Assert.assertNotNull(mStubTableActionAlterations.preDelete(new Object()));
    }

    @Test
    public void postDelete() throws Exception {
        mStubTableActionAlterations.postDelete(new Object());
    }
}
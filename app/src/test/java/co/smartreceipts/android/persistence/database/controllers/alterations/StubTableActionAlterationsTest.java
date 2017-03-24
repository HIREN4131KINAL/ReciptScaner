package co.smartreceipts.android.persistence.database.controllers.alterations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class StubTableActionAlterationsTest {

    StubTableActionAlterations<Object> mStubTableActionAlterations;
    
    @Before
    public void setUp() throws Exception {
        mStubTableActionAlterations = new StubTableActionAlterations<>();
    }

    @Test
    public void preGet() throws Exception {
        final Observable<Void> observable = mStubTableActionAlterations.preGet();
        assertNotNull(observable);

        final TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

    
    @Test
    public void postGet() {
        final List<Object> objects = Arrays.asList(new Object(), new Object(), new Object());     
        final Observable<List<Object>> observable = mStubTableActionAlterations.postGet(objects);
        assertNotNull(observable);
        
        final TestSubscriber<List<Object>> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertValue(objects);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
}
    
    @Test
    public void preInsert() {
        final Object object = new Object();        
        final Observable<Object> observable = mStubTableActionAlterations.preInsert(object);
        assertNotNull(observable);
        
        final TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertValue(object);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }
    
    @Test
    public void postInsert() {
        final Object object = new Object();        
        final Observable<Object> observable = mStubTableActionAlterations.postInsert(object);
        assertNotNull(observable);
        
        final TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertValue(object);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

    @Test
    public void postInsertNull() {
        final Observable<Object> observable = mStubTableActionAlterations.postInsert(null);
        assertNotNull(observable);

        final TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertNoValues();
        testSubscriber.assertNotCompleted();
        testSubscriber.assertError(Exception.class);
    }
    
    @Test
    public void preUpdate() {
        final Object oldObject = new Object();
        final Object object = new Object();        
        final Observable<Object> observable = mStubTableActionAlterations.preUpdate(oldObject, object);
        assertNotNull(observable);
        
        final TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertValue(object);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }
    
    @Test
    public void postUpdate() {
        final Object oldObject = new Object();
        final Object object = new Object();        
        final Observable<Object> observable = mStubTableActionAlterations.postUpdate(oldObject, object);
        assertNotNull(observable);
        
        final TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertValue(object);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

    @Test
    public void postUpdateNull() {
        final Object oldObject = new Object();
        final Observable<Object> observable = mStubTableActionAlterations.postUpdate(oldObject, null);
        assertNotNull(observable);

        final TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertNoValues();
        testSubscriber.assertNotCompleted();
        testSubscriber.assertError(Exception.class);
    }
    
    @Test
    public void preDelete() {
        final Object object = new Object();        
        final Observable<Object> observable = mStubTableActionAlterations.preDelete(object);
        assertNotNull(observable);
        
        final TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertValue(object);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }
    
    @Test
    public void postDelete() {
        final Object object = new Object();        
        final Observable<Object> observable = mStubTableActionAlterations.postDelete(object);
        assertNotNull(observable);
        
        final TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertValue(object);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

    @Test
    public void postDeleteNull() {
        final Observable<Object> observable = mStubTableActionAlterations.postDelete(null);
        assertNotNull(observable);

        final TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        observable.subscribe(testSubscriber);
        testSubscriber.assertNoValues();
        testSubscriber.assertNotCompleted();
        testSubscriber.assertError(Exception.class);
    }
}
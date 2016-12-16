package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.alterations.TableActionAlterations;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AbstractTableControllerTest {

    /**
     * Test impl for our abstract class
     */
    private class AbstractTableControllerTestImpl extends AbstractTableController<Object> {

        AbstractTableControllerTestImpl(@NonNull Table<Object, ?> table, @NonNull TableActionAlterations<Object> tableActionAlterations, @NonNull Analytics analytics,
                                        @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler) {
            super(table, tableActionAlterations, analytics, subscribeOnScheduler, observeOnScheduler);
        }
    }

    // Class under test
    AbstractTableController<Object> mAbstractTableController;

    @Mock
    Table<Object, ?> mTable;

    @Mock
    TableActionAlterations<Object> mTableActionAlterations;

    @Mock
    Analytics mAnalytics;

    @Mock
    TableEventsListener<Object> mListener1;

    @Mock
    TableEventsListener<Object> mListener2;

    @Mock
    TableEventsListener<Object> mListener3;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mAbstractTableController = new AbstractTableControllerTestImpl(mTable, mTableActionAlterations, mAnalytics, Schedulers.immediate(), Schedulers.immediate());
        mAbstractTableController.subscribe(mListener1);
        mAbstractTableController.subscribe(mListener2);
        mAbstractTableController.subscribe(mListener3);
    }

    @Test
    public void onGetSuccess() throws Exception {
        final List<Object> objects = Arrays.asList(new Object(), new Object(), new Object());
        when(mTableActionAlterations.preGet()).thenReturn(Observable.<Void>just(null));
        when(mTable.get()).thenReturn(Observable.just(objects));

        mAbstractTableController.unsubscribe(mListener2);
        mAbstractTableController.get();

        verify(mListener1).onGetSuccess(objects);
        verify(mListener3).onGetSuccess(objects);
        verifyZeroInteractions(mListener2);
    }

    @Test
    public void onPreGetException() throws Exception {
        final List<Object> objects = Arrays.asList(new Object(), new Object(), new Object());
        final Exception e = new Exception();
        when(mTableActionAlterations.preGet()).thenReturn(Observable.<Void>error(e));
        when(mTable.get()).thenReturn(Observable.just(objects));

        mAbstractTableController.unsubscribe(mListener2);
        mAbstractTableController.get();

        verify(mAnalytics).record(any(ErrorEvent.class));
        verify(mListener1).onGetFailure(e);
        verify(mListener3).onGetFailure(e);
        verifyZeroInteractions(mListener2);
    }

    @Test
    public void onGetException() throws Exception {
        final List<Object> objects = Arrays.asList(new Object(), new Object(), new Object());
        final Exception e = new Exception();
        when(mTableActionAlterations.preGet()).thenReturn(Observable.<Void>just(null));
        when(mTable.get()).thenReturn(Observable.<List<Object>>error(e));

        mAbstractTableController.unsubscribe(mListener2);
        mAbstractTableController.get();

        verify(mAnalytics).record(any(ErrorEvent.class));
        verify(mListener1).onGetFailure(e);
        verify(mListener3).onGetFailure(e);
        verifyZeroInteractions(mListener2);
    }

    @Test
    public void onPostGetException() throws Exception {
        final List<Object> objects = Arrays.asList(new Object(), new Object(), new Object());
        when(mTableActionAlterations.preGet()).thenReturn(Observable.<Void>just(null));
        when(mTable.get()).thenReturn(Observable.just(objects));
        doThrow(new Exception()).when(mTableActionAlterations).postGet(objects);

        mAbstractTableController.unsubscribe(mListener2);
        mAbstractTableController.get();

        verify(mAnalytics).record(any(ErrorEvent.class));
        verify(mListener1).onGetFailure(any(Exception.class));
        verify(mListener3).onGetFailure(any(Exception.class));
        verifyZeroInteractions(mListener2);
    }

    @Test
    public void onInsertSuccess() throws Exception {
        final Object insertItem = new Object();
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        when(mTableActionAlterations.preInsert(insertItem)).thenReturn(Observable.just(insertItem));
        when(mTable.insert(insertItem, databaseOperationMetadata)).thenReturn(Observable.just(insertItem));

        mAbstractTableController.unsubscribe(mListener2);
        mAbstractTableController.insert(insertItem, databaseOperationMetadata);

        verify(mListener1).onInsertSuccess(insertItem, databaseOperationMetadata);
        verify(mListener3).onInsertSuccess(insertItem, databaseOperationMetadata);
        verifyZeroInteractions(mListener2);
    }

    @Test
    public void onPreInsertException() throws Exception {
        final Object insertItem = new Object();
        final Exception e = new Exception();
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        when(mTableActionAlterations.preInsert(insertItem)).thenReturn(Observable.error(e));
        when(mTable.insert(insertItem, databaseOperationMetadata)).thenReturn(Observable.just(insertItem));

        mAbstractTableController.unsubscribe(mListener2);
        mAbstractTableController.insert(insertItem, databaseOperationMetadata);

        verify(mAnalytics).record(any(ErrorEvent.class));
        verify(mListener1).onInsertFailure(insertItem, e, databaseOperationMetadata);
        verify(mListener3).onInsertFailure(insertItem, e, databaseOperationMetadata);
        verifyZeroInteractions(mListener2);
    }

    @Test
    public void onInsertException() throws Exception {
        final Object insertItem = new Object();
        final Exception e = new Exception();
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        when(mTableActionAlterations.preInsert(insertItem)).thenReturn(Observable.just(insertItem));
        when(mTable.insert(insertItem, databaseOperationMetadata)).thenReturn(Observable.error(e));

        mAbstractTableController.unsubscribe(mListener2);
        mAbstractTableController.insert(insertItem, databaseOperationMetadata);

        verify(mAnalytics).record(any(ErrorEvent.class));
        verify(mListener1).onInsertFailure(insertItem, e, databaseOperationMetadata);
        verify(mListener3).onInsertFailure(insertItem, e, databaseOperationMetadata);
        verifyZeroInteractions(mListener2);
    }

    @Test
    public void onPostInsertException() throws Exception {
        final Object insertItem = new Object();
        final Exception e = new Exception();
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        when(mTableActionAlterations.preInsert(insertItem)).thenReturn(Observable.just(insertItem));
        when(mTable.insert(insertItem, databaseOperationMetadata)).thenReturn(Observable.just(insertItem));
        doThrow(e).when(mTableActionAlterations).postInsert(insertItem);

        mAbstractTableController.unsubscribe(mListener2);
        mAbstractTableController.insert(insertItem, databaseOperationMetadata);

        // The Exceptions.propagate call wraps our exception inside a RuntimeException
        verify(mAnalytics).record(any(ErrorEvent.class));
        verify(mListener1).onInsertFailure(eq(insertItem), any(Exception.class), eq(databaseOperationMetadata));
        verify(mListener3).onInsertFailure(eq(insertItem), any(Exception.class), eq(databaseOperationMetadata));
        verifyZeroInteractions(mListener2);
    }

    @Test
    public void onUpdateSuccess() throws Exception {
        final Object oldItem = new Object();
        final Object newItem = new Object();
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        when(mTableActionAlterations.preUpdate(oldItem, newItem)).thenReturn(Observable.just(newItem));
        when(mTable.update(oldItem, newItem, databaseOperationMetadata)).thenReturn(Observable.just(newItem));

        mAbstractTableController.unsubscribe(mListener2);
        mAbstractTableController.update(oldItem, newItem, databaseOperationMetadata);

        verify(mListener1).onUpdateSuccess(oldItem, newItem, databaseOperationMetadata);
        verify(mListener3).onUpdateSuccess(oldItem, newItem, databaseOperationMetadata);
        verifyZeroInteractions(mListener2);
    }

    @Test
    public void onPreUpdateException() throws Exception {
        final Object oldItem = new Object();
        final Object newItem = new Object();
        final Exception e = new Exception();
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        when(mTableActionAlterations.preUpdate(oldItem, newItem)).thenReturn(Observable.error(e));
        when(mTable.update(oldItem, newItem, databaseOperationMetadata)).thenReturn(Observable.just(newItem));

        mAbstractTableController.unsubscribe(mListener2);
        mAbstractTableController.update(oldItem, newItem, databaseOperationMetadata);

        verify(mAnalytics).record(any(ErrorEvent.class));
        verify(mListener1).onUpdateFailure(oldItem, e, databaseOperationMetadata);
        verify(mListener3).onUpdateFailure(oldItem, e, databaseOperationMetadata);
        verifyZeroInteractions(mListener2);
    }

    @Test
    public void onUpdateException() throws Exception {
        final Object oldItem = new Object();
        final Object newItem = new Object();
        final Exception e = new Exception();
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        when(mTableActionAlterations.preUpdate(oldItem, newItem)).thenReturn(Observable.just(newItem));
        when(mTable.update(oldItem, newItem, databaseOperationMetadata)).thenReturn(Observable.error(e));

        mAbstractTableController.unsubscribe(mListener2);
        mAbstractTableController.update(oldItem, newItem, databaseOperationMetadata);

        verify(mAnalytics).record(any(ErrorEvent.class));
        verify(mListener1).onUpdateFailure(oldItem, e, databaseOperationMetadata);
        verify(mListener3).onUpdateFailure(oldItem, e, databaseOperationMetadata);
        verifyZeroInteractions(mListener2);
    }

    @Test
    public void onPostUpdateException() throws Exception {
        final Object oldItem = new Object();
        final Object newItem = new Object();
        final Exception e = new Exception();
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        when(mTableActionAlterations.preUpdate(oldItem, newItem)).thenReturn(Observable.just(newItem));
        when(mTable.update(oldItem, newItem, databaseOperationMetadata)).thenReturn(Observable.just(newItem));
        doThrow(e).when(mTableActionAlterations).postUpdate(oldItem, newItem);

        mAbstractTableController.unsubscribe(mListener2);
        mAbstractTableController.update(oldItem, newItem, databaseOperationMetadata);

        // The Exceptions.propagate call wraps our exception inside a RuntimeException
        verify(mAnalytics).record(any(ErrorEvent.class));
        verify(mListener1).onUpdateFailure(eq(oldItem), any(Exception.class), eq(databaseOperationMetadata));
        verify(mListener3).onUpdateFailure(eq(oldItem), any(Exception.class), eq(databaseOperationMetadata));
        verifyZeroInteractions(mListener2);
    }

    @Test
    public void onDeleteSuccess() throws Exception {
        final Object deleteCandidateItem = new Object();
        final Object deletedItem = new Object();
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        when(mTableActionAlterations.preDelete(deleteCandidateItem)).thenReturn(Observable.just(deleteCandidateItem));
        when(mTable.delete(deleteCandidateItem, databaseOperationMetadata)).thenReturn(Observable.just(deletedItem));

        mAbstractTableController.unsubscribe(mListener2);
        mAbstractTableController.delete(deleteCandidateItem, databaseOperationMetadata);

        verify(mListener1).onDeleteSuccess(deletedItem, databaseOperationMetadata);
        verify(mListener3).onDeleteSuccess(deletedItem, databaseOperationMetadata);
        verifyZeroInteractions(mListener2);
    }

    @Test
    public void onPreDeleteException() throws Exception {
        final Object deleteCandidateItem = new Object();
        final Object deletedItem = new Object();
        final Exception e = new Exception();
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        when(mTableActionAlterations.preDelete(deleteCandidateItem)).thenReturn(Observable.error(e));
        when(mTable.delete(deleteCandidateItem, databaseOperationMetadata)).thenReturn(Observable.just(deletedItem));

        mAbstractTableController.unsubscribe(mListener2);
        mAbstractTableController.delete(deleteCandidateItem, databaseOperationMetadata);

        verify(mAnalytics).record(any(ErrorEvent.class));
        verify(mListener1).onDeleteFailure(deleteCandidateItem, e, databaseOperationMetadata);
        verify(mListener3).onDeleteFailure(deleteCandidateItem, e, databaseOperationMetadata);
        verifyZeroInteractions(mListener2);
    }

    @Test
    public void onDeleteException() throws Exception {
        final Object deleteCandidateItem = new Object();
        final Object deletedItem = new Object();
        final Exception e = new Exception();
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        when(mTableActionAlterations.preDelete(deleteCandidateItem)).thenReturn(Observable.just(deleteCandidateItem));
        when(mTable.delete(deleteCandidateItem, databaseOperationMetadata)).thenReturn(Observable.error(e));

        mAbstractTableController.unsubscribe(mListener2);
        mAbstractTableController.delete(deleteCandidateItem, databaseOperationMetadata);

        verify(mAnalytics).record(any(ErrorEvent.class));
        verify(mListener1).onDeleteFailure(deleteCandidateItem, e, databaseOperationMetadata);
        verify(mListener3).onDeleteFailure(deleteCandidateItem, e, databaseOperationMetadata);
        verifyZeroInteractions(mListener2);
    }

    @Test
    public void onPostDeleteException() throws Exception {
        final Object deleteCandidateItem = new Object();
        final Object deletedItem = new Object();
        final Exception e = new Exception();
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        when(mTableActionAlterations.preDelete(deleteCandidateItem)).thenReturn(Observable.just(deleteCandidateItem));
        when(mTable.delete(deleteCandidateItem, databaseOperationMetadata)).thenReturn(Observable.just(deletedItem));
        doThrow(e).when(mTableActionAlterations).postDelete(deletedItem);

        mAbstractTableController.unsubscribe(mListener2);
        mAbstractTableController.delete(deleteCandidateItem, databaseOperationMetadata);

        // The Exceptions.propagate call wraps our exception inside a RuntimeException
        verify(mAnalytics).record(any(ErrorEvent.class));
        verify(mListener1).onDeleteFailure(eq(deleteCandidateItem), any(Exception.class), eq(databaseOperationMetadata));
        verify(mListener3).onDeleteFailure(eq(deleteCandidateItem), any(Exception.class), eq(databaseOperationMetadata));
        verifyZeroInteractions(mListener2);
    }

}
package co.smartreceipts.android.persistence.database.controllers.alterations;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.BuilderFactory1;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import rx.Observable;
import rx.observers.TestSubscriber;
import wb.android.storage.StorageManager;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReceiptTableActionAlterationsTest {

    // Class under test
    ReceiptTableActionAlterations mReceiptTableActionAlterations;

    File file1;

    File file2;

    @Mock
    ReceiptsTable mReceiptsTable;

    @Mock
    StorageManager mStorageManager;

    @Mock
    BuilderFactory1<Receipt, ReceiptBuilderFactory> mReceiptBuilderFactoryFactory;

    @Mock
    ReceiptBuilderFactory mReceiptBuilderFactory;

    @Mock
    Receipt mReceipt;

    @Mock
    Trip mTrip;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mReceipt.getTrip()).thenReturn(mTrip);
        when(mReceiptBuilderFactory.build()).thenReturn(mReceipt);
        when(mReceiptBuilderFactoryFactory.build(mReceipt)).thenReturn(mReceiptBuilderFactory);
        when(mReceiptBuilderFactory.setIndex(anyInt())).thenReturn(mReceiptBuilderFactory);

        doAnswer(new Answer() {
            @Override
            public File answer(InvocationOnMock invocation) throws Throwable {
                return new File((String)invocation.getArguments()[1]);
            }
        }).when(mStorageManager).getFile(any(File.class), anyString());
        doAnswer(new Answer() {
            @Override
            public File answer(InvocationOnMock invocation) throws Throwable {
                return new File((String)invocation.getArguments()[1]);
            }
        }).when(mStorageManager).rename(any(File.class), anyString());
        doAnswer(new Answer() {
            @Override
            public ReceiptBuilderFactory answer(InvocationOnMock invocation) throws Throwable {
                when(mReceipt.getFile()).thenReturn((File)invocation.getArguments()[0]);
                return mReceiptBuilderFactory;
            }
        }).when(mReceiptBuilderFactory).setFile(any(File.class));
        doAnswer(new Answer() {
            @Override
            public ReceiptBuilderFactory answer(InvocationOnMock invocation) throws Throwable {
                when(mReceipt.getIndex()).thenReturn((Integer) invocation.getArguments()[0]);
                return mReceiptBuilderFactory;
            }
        }).when(mReceiptBuilderFactory).setIndex(anyInt());

        mReceiptTableActionAlterations = new ReceiptTableActionAlterations(RuntimeEnvironment.application, mReceiptsTable, mStorageManager, mReceiptBuilderFactoryFactory);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void tearDown() throws Exception {
        if (file1 != null) {
            file1.delete();
        }
        if (file2 != null) {
            file2.delete();
        }
    }

    @Test
    public void preInsertWithoutFile() {
        final String name = "name";
        final List<Receipt> receiptsInTrip = Arrays.asList(mock(Receipt.class), mock(Receipt.class), mock(Receipt.class));
        when(mReceiptsTable.get(mTrip)).thenReturn(Observable.just(receiptsInTrip));
        when(mReceipt.hasFile()).thenReturn(false);
        when(mReceipt.getName()).thenReturn(name);

        final TestSubscriber<Receipt> testSubscriber = new TestSubscriber<>();
        mReceiptTableActionAlterations.preInsert(mReceipt).subscribe(testSubscriber);
        testSubscriber.assertValue(mReceipt);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();

        assertNull(mReceipt.getFile());
        verify(mReceiptBuilderFactory).setIndex(4);
    }

    @Test
    public void preInsertWithFile() {
        final String name = "name";
        final List<Receipt> receiptsInTrip = Arrays.asList(mock(Receipt.class), mock(Receipt.class), mock(Receipt.class));
        when(mReceiptsTable.get(mTrip)).thenReturn(Observable.just(receiptsInTrip));
        when(mReceipt.hasFile()).thenReturn(true);
        when(mReceipt.getName()).thenReturn(name);
        when(mReceipt.getFile()).thenReturn(new File("12345.jpg"));

        final TestSubscriber<Receipt> testSubscriber = new TestSubscriber<>();
        mReceiptTableActionAlterations.preInsert(mReceipt).subscribe(testSubscriber);
        testSubscriber.assertValue(mReceipt);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();

        assertEquals(new File("4_name.jpg"), mReceipt.getFile());
        verify(mReceiptBuilderFactory).setIndex(4);
    }

    @Test
    public void preInsertWithIllegalCharactersInName() {
        final String name = "before_|\\\\?*<\\:>+[]/'\n\r\t\0\f_after";
        final List<Receipt> receiptsInTrip = Arrays.asList(mock(Receipt.class), mock(Receipt.class), mock(Receipt.class));
        when(mReceiptsTable.get(mTrip)).thenReturn(Observable.just(receiptsInTrip));
        when(mReceipt.hasFile()).thenReturn(true);
        when(mReceipt.getName()).thenReturn(name);
        when(mReceipt.getFile()).thenReturn(new File("12345.jpg"));

        final TestSubscriber<Receipt> testSubscriber = new TestSubscriber<>();
        mReceiptTableActionAlterations.preInsert(mReceipt).subscribe(testSubscriber);
        testSubscriber.assertValue(mReceipt);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();

        assertEquals(new File("4_before__after.jpg"), mReceipt.getFile());
        verify(mReceiptBuilderFactory).setIndex(4);
    }

    @Test
    public void preUpdateWithoutFile() {
        final Receipt oldReceipt = mock(Receipt.class);
        when(mReceipt.getFile()).thenReturn(null);
        final TestSubscriber<Receipt> testSubscriber = new TestSubscriber<>();

        mReceiptTableActionAlterations.preUpdate(oldReceipt, mReceipt).subscribe(testSubscriber);
        testSubscriber.assertValue(mReceipt);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

    @Test
    public void preUpdateWithBrandNewFile() {
        final String name = "name";
        final Receipt oldReceipt = mock(Receipt.class);
        when(oldReceipt.getFile()).thenReturn(null);
        when(mReceipt.getIndex()).thenReturn(4);
        when(mReceipt.getName()).thenReturn(name);
        when(mReceipt.getFile()).thenReturn(new File("12345.jpg"));
        final TestSubscriber<Receipt> testSubscriber = new TestSubscriber<>();

        mReceiptTableActionAlterations.preUpdate(oldReceipt, mReceipt).subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();

        final List<Receipt> onNextResults = testSubscriber.getOnNextEvents();
        assertNotNull(onNextResults);
        assertTrue(onNextResults.size() == 1);
        final Receipt result = onNextResults.get(0);
        assertEquals(new File("4_name.jpg"), result.getFile());
    }

    @Test
    public void preUpdateWithUpdatedFile() throws Exception {
        this.file1 = new File("1_name.jpg");
        this.file2 = new File("12345.jpg");
        assertTrue(this.file1.createNewFile());
        assertTrue(this.file2.createNewFile());

        final String name = "name";
        final Receipt oldReceipt = mock(Receipt.class);
        when(oldReceipt.getIndex()).thenReturn(1);
        when(oldReceipt.getName()).thenReturn(name);
        when(oldReceipt.getFile()).thenReturn(file1);
        when(mReceipt.getIndex()).thenReturn(1);
        when(mReceipt.getName()).thenReturn(name);
        when(mReceipt.getFile()).thenReturn(file2);
        final TestSubscriber<Receipt> testSubscriber = new TestSubscriber<>();

        mReceiptTableActionAlterations.preUpdate(oldReceipt, mReceipt).subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();

        final List<Receipt> onNextResults = testSubscriber.getOnNextEvents();
        assertNotNull(onNextResults);
        assertTrue(onNextResults.size() == 1);
        final Receipt result = onNextResults.get(0);
        assertNotNull(result.getFile());
        assertEquals("1_name.jpg", result.getFile().getName());
    }

    @Test
    public void preUpdateWithUpdatedFileAndFileType() throws Exception {
        this.file1 = new File("1_name.jpg");
        this.file2 = new File("12345.pdf");
        assertTrue(this.file1.createNewFile());
        assertTrue(this.file2.createNewFile());

        final String name = "name";
        final Receipt oldReceipt = mock(Receipt.class);
        when(oldReceipt.getIndex()).thenReturn(1);
        when(oldReceipt.getName()).thenReturn(name);
        when(oldReceipt.getFile()).thenReturn(file1);
        when(mReceipt.getIndex()).thenReturn(1);
        when(mReceipt.getName()).thenReturn(name);
        when(mReceipt.getFile()).thenReturn(file2);
        final TestSubscriber<Receipt> testSubscriber = new TestSubscriber<>();

        mReceiptTableActionAlterations.preUpdate(oldReceipt, mReceipt).subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();

        final List<Receipt> onNextResults = testSubscriber.getOnNextEvents();
        assertNotNull(onNextResults);
        assertTrue(onNextResults.size() == 1);
        final Receipt result = onNextResults.get(0);
        assertNotNull(result.getFile());
        assertEquals("1_name.pdf", result.getFile().getName());
        assertTrue(result.getFile().delete());
    }

    @Test
    public void preUpdateWithNewIndex() {
        final String name = "name";
        final Receipt oldReceipt = mock(Receipt.class);
        when(oldReceipt.getIndex()).thenReturn(1);
        when(oldReceipt.getName()).thenReturn(name);
        when(oldReceipt.getFile()).thenReturn(new File("1_name.jpg"));
        when(mReceipt.getIndex()).thenReturn(4);
        when(mReceipt.getName()).thenReturn(name);
        when(mReceipt.getFile()).thenReturn(new File("1_name.jpg"));
        final TestSubscriber<Receipt> testSubscriber = new TestSubscriber<>();

        mReceiptTableActionAlterations.preUpdate(oldReceipt, mReceipt).subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();

        final List<Receipt> onNextResults = testSubscriber.getOnNextEvents();
        assertNotNull(onNextResults);
        assertTrue(onNextResults.size() == 1);
        final Receipt result = onNextResults.get(0);
        assertEquals(new File("4_name.jpg"), result.getFile());
    }

    @Test
    public void postUpdateNull() throws Exception {
        final TestSubscriber<Receipt> testSubscriber = new TestSubscriber<>();
        mReceiptTableActionAlterations.postUpdate(mReceipt, null).subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        testSubscriber.assertNotCompleted();
        testSubscriber.assertError(Exception.class);

        verifyZeroInteractions(mStorageManager);
    }

    @Test
    public void postUpdateSuccessWithoutFile() throws Exception {
        when(mReceipt.hasFile()).thenReturn(false);

        final TestSubscriber<Receipt> testSubscriber = new TestSubscriber<>();
        final Receipt updatedReceipt = mock(Receipt.class);
        final Observable<Receipt> observable = mReceiptTableActionAlterations.postUpdate(mReceipt, updatedReceipt);
        assertNotNull(observable);
        observable.subscribe(testSubscriber);
        testSubscriber.assertValue(updatedReceipt);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        verifyZeroInteractions(mStorageManager);
    }

    @Test
    public void postUpdateSuccessWithSameFile() throws Exception {
        final Receipt updatedReceipt = mock(Receipt.class);
        final File file = new File("abc");
        when(mReceipt.hasFile()).thenReturn(true);
        when(updatedReceipt.hasFile()).thenReturn(true);
        when(mReceipt.getFile()).thenReturn(file);
        when(updatedReceipt.getFile()).thenReturn(file);

        final TestSubscriber<Receipt> testSubscriber = new TestSubscriber<>();
        final Observable<Receipt> observable = mReceiptTableActionAlterations.postUpdate(mReceipt, updatedReceipt);
        assertNotNull(observable);
        observable.subscribe(testSubscriber);
        testSubscriber.assertValue(updatedReceipt);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        verify(mStorageManager, never()).delete(file);
    }

    @Test
    public void postUpdateSuccessWithNewFile() throws Exception {
        final Receipt updatedReceipt = mock(Receipt.class);
        final File file = new File("abc");
        final File newFile = new File("efg");
        when(mReceipt.hasFile()).thenReturn(true);
        when(updatedReceipt.hasFile()).thenReturn(true);
        when(mReceipt.getFile()).thenReturn(file);
        when(updatedReceipt.getFile()).thenReturn(newFile);

        final TestSubscriber<Receipt> testSubscriber = new TestSubscriber<>();
        final Observable<Receipt> observable = mReceiptTableActionAlterations.postUpdate(mReceipt, updatedReceipt);
        assertNotNull(observable);
        observable.subscribe(testSubscriber);
        testSubscriber.assertValue(updatedReceipt);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        verify(mStorageManager).delete(file);
    }

    @Test
    public void postDeleteNull() throws Exception {
        final TestSubscriber<Receipt> testSubscriber = new TestSubscriber<>();
        mReceiptTableActionAlterations.postDelete(null).subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        testSubscriber.assertNotCompleted();
        testSubscriber.assertError(Exception.class);

        verifyZeroInteractions(mStorageManager);
    }

    @Test
    public void postDeleteSuccessWithoutFile() throws Exception {
        when(mReceipt.hasFile()).thenReturn(false);

        final TestSubscriber<Receipt> testSubscriber = new TestSubscriber<>();
        final Observable<Receipt> observable = mReceiptTableActionAlterations.postDelete(mReceipt);
        assertNotNull(observable);
        observable.subscribe(testSubscriber);
        testSubscriber.assertValue(mReceipt);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        verifyZeroInteractions(mStorageManager);
    }

    @Test
    public void postDeleteSuccessWithFile() throws Exception {
        final File file = new File("abc");
        when(mReceipt.hasFile()).thenReturn(true);
        when(mReceipt.getFile()).thenReturn(file);

        final TestSubscriber<Receipt> testSubscriber = new TestSubscriber<>();
        final Observable<Receipt> observable = mReceiptTableActionAlterations.postDelete(mReceipt);
        assertNotNull(observable);
        observable.subscribe(testSubscriber);
        testSubscriber.assertValue(mReceipt);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        verify(mStorageManager).delete(file);
    }


}
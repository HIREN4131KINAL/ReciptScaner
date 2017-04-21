package co.smartreceipts.android.ocr.widget.configuration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;

import co.smartreceipts.android.identity.store.EmailAddress;
import co.smartreceipts.android.purchases.model.AvailablePurchase;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

import static org.mockito.Mockito.*;

public class OcrConfigurationPresenterTest {

    private static final boolean SAVE_IMAGES_REMOTELY = true;
    private static final int REMAINING_SCANS = 25;
    private static final InAppPurchase PURCHASE = InAppPurchase.OcrScans10;

    @InjectMocks
    OcrConfigurationPresenter ocrConfigurationPresenter;

    @Mock
    OcrConfigurationView view;

    @Mock
    OcrConfigurationInteractor interactor;

    @Mock
    EmailAddress emailAddress;

    @Mock
    Consumer<Boolean> allowUsToSaveImagesRemotelyConsumer;

    @Mock
    AvailablePurchase availablePurchase;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(availablePurchase.getInAppPurchase()).thenReturn(PURCHASE);
        when(interactor.getEmail()).thenReturn(emailAddress);
        when(interactor.getAllowUsToSaveImagesRemotely()).thenReturn(Observable.just(SAVE_IMAGES_REMOTELY));
        when(interactor.getRemainingScansStream()).thenReturn(Observable.just(REMAINING_SCANS));
        when(interactor.getAvailableOcrPurchases()).thenReturn(Single.just(Collections.singletonList(availablePurchase)));

        when(view.getAllowUsToSaveImagesRemotelyCheckboxChanged()).thenReturn(Observable.just(SAVE_IMAGES_REMOTELY));
        when(view.getAvailablePurchaseClicks()).thenReturn(Observable.just(availablePurchase));
        doReturn(allowUsToSaveImagesRemotelyConsumer).when(view).getAllowUsToSaveImagesRemotelyConsumer();
    }

    @Test
    public void onResume() throws Exception {
        ocrConfigurationPresenter.onResume();

        // Presents Email
        verify(view).present(emailAddress);

        // Consumes Save Images Remotely State
        verify(allowUsToSaveImagesRemotelyConsumer).accept(SAVE_IMAGES_REMOTELY);

        // Interacts With Save Images Remotely State On Check Changed
        verify(interactor).setAllowUsToSaveImagesRemotely(SAVE_IMAGES_REMOTELY);

        // Presents Remaining Scans
        verify(view).present(REMAINING_SCANS);

        // Presents Available purchases
        verify(view).present(Collections.singletonList(availablePurchase));

        // Interacts with purchase clicks
        verify(interactor).startOcrPurchase(availablePurchase);
    }

}
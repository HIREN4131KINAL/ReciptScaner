package co.smartreceipts.android.rating;

import org.junit.Test;
import org.mockito.Mockito;

import rx.Single;
import rx.observers.TestSubscriber;

public class AppRatingManagerShould {

    private static final int LAUNCHES_LESS = 5;
    private static final int LAUNCHES_MORE = 16;

    private static final int NO_ADDITIONAL_THRESHOLD = 0;
    private static final int ADDITIONAL_THRESHOLD = 7;

    private static final boolean CAN_SHOW = true;
    private static final boolean CANT_SHOW = false;

    private static final boolean CRASH = true;
    private static final boolean NO_CRASH = false;

    private static final long daysToMillis = 24 * 60 * 60 * 1000;
    private static final long OLD_TIME = System.currentTimeMillis() - 8 * daysToMillis;
    private static final long RECENT_TIME = System.currentTimeMillis() - 2 * daysToMillis;


    @Test
    public void returnFalseIfCrashOccurred() {
        // given
        AppRatingStorage mockedStorage = Mockito.mock(AppRatingStorage.class);

        AppRatingModel appRatingModel = new AppRatingModel(CAN_SHOW, CRASH, LAUNCHES_MORE, NO_ADDITIONAL_THRESHOLD, OLD_TIME);
        Mockito.when(mockedStorage.readAppRatingData()).thenReturn(Single.just(appRatingModel));

        // when
        AppRatingManager.clearStateForTesting();
        AppRatingManager manager = AppRatingManager.getInstance(mockedStorage);
        TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();

        manager.checkIfNeedToAskRating()
                .subscribe(testSubscriber);

        // then
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue(false);
    }

    @Test
    public void returnFalseIfCantShow() {
        // given
        AppRatingStorage mockedStorage = Mockito.mock(AppRatingStorage.class);

        AppRatingModel appRatingModel = new AppRatingModel(CANT_SHOW, NO_CRASH, LAUNCHES_MORE, NO_ADDITIONAL_THRESHOLD, OLD_TIME);
        Mockito.when(mockedStorage.readAppRatingData()).thenReturn(Single.just(appRatingModel));

        // when
        AppRatingManager.clearStateForTesting();
        AppRatingManager manager = AppRatingManager.getInstance(mockedStorage);
        TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();

        manager.checkIfNeedToAskRating()
                .subscribe(testSubscriber);

        // then
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue(false);
    }

    @Test
    public void returnFalseIfLaunchesLess() {
        // given
        AppRatingStorage mockedStorage = Mockito.mock(AppRatingStorage.class);

        AppRatingModel appRatingModel = new AppRatingModel(CAN_SHOW, NO_CRASH, LAUNCHES_LESS, NO_ADDITIONAL_THRESHOLD, OLD_TIME);
        Mockito.when(mockedStorage.readAppRatingData()).thenReturn(Single.just(appRatingModel));

        // when
        AppRatingManager.clearStateForTesting();
        AppRatingManager manager = AppRatingManager.getInstance(mockedStorage);
        TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();

        manager.checkIfNeedToAskRating()
                .subscribe(testSubscriber);

        // then
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue(false);
    }

    @Test
    public void returnFalseIfDaysSinceInstallLess() {
        // given
        AppRatingStorage mockedStorage = Mockito.mock(AppRatingStorage.class);

        AppRatingModel appRatingModel = new AppRatingModel(CAN_SHOW, NO_CRASH, LAUNCHES_MORE, NO_ADDITIONAL_THRESHOLD, RECENT_TIME);
        Mockito.when(mockedStorage.readAppRatingData()).thenReturn(Single.just(appRatingModel));

        // when
        AppRatingManager.clearStateForTesting();
        AppRatingManager manager = AppRatingManager.getInstance(mockedStorage);
        TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();

        manager.checkIfNeedToAskRating()
                .subscribe(testSubscriber);

        // then
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue(false);
    }

    @Test
    public void returnFalseIfDaysSinceInstallWithProrogueLess() {
        // given
        AppRatingStorage mockedStorage = Mockito.mock(AppRatingStorage.class);

        AppRatingModel appRatingModel = new AppRatingModel(CAN_SHOW, NO_CRASH, LAUNCHES_MORE, ADDITIONAL_THRESHOLD, OLD_TIME);
        Mockito.when(mockedStorage.readAppRatingData()).thenReturn(Single.just(appRatingModel));

        // when
        AppRatingManager.clearStateForTesting();
        AppRatingManager manager = AppRatingManager.getInstance(mockedStorage);
        TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();

        manager.checkIfNeedToAskRating()
                .subscribe(testSubscriber);

        // then
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue(false);
    }

    @Test
    public void returnTrueIfAllOkWithoutAdditionalThreshold() {
        // given
        AppRatingStorage mockedStorage = Mockito.mock(AppRatingStorage.class);

        AppRatingModel appRatingModel = new AppRatingModel(CAN_SHOW, NO_CRASH, LAUNCHES_MORE, NO_ADDITIONAL_THRESHOLD, OLD_TIME);
        Mockito.when(mockedStorage.readAppRatingData()).thenReturn(Single.just(appRatingModel));

        // when
        AppRatingManager.clearStateForTesting();
        AppRatingManager manager = AppRatingManager.getInstance(mockedStorage);
        TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();

        manager.checkIfNeedToAskRating()
                .subscribe(testSubscriber);

        // then
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue(true);
    }

    @Test
    public void returnTrueIfAllOkWithAdditionalThreshold() {
        // given
        AppRatingStorage mockedStorage = Mockito.mock(AppRatingStorage.class);

        AppRatingModel appRatingModel = new AppRatingModel(CAN_SHOW, NO_CRASH, LAUNCHES_MORE, ADDITIONAL_THRESHOLD, RECENT_TIME);
        Mockito.when(mockedStorage.readAppRatingData()).thenReturn(Single.just(appRatingModel));

        // when
        AppRatingManager.clearStateForTesting();
        AppRatingManager manager = AppRatingManager.getInstance(mockedStorage);
        TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();

        manager.checkIfNeedToAskRating()
                .subscribe(testSubscriber);

        // then
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue(true);
    }


}
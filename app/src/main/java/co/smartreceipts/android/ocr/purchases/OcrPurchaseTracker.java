package co.smartreceipts.android.ocr.purchases;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import java.util.Set;

import javax.inject.Inject;

import co.smartreceipts.android.apis.ApiValidationException;
import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.apis.me.MeResponse;
import co.smartreceipts.android.purchases.PurchaseEventsListener;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.apis.MobileAppPurchasesService;
import co.smartreceipts.android.purchases.apis.PurchaseRequest;
import co.smartreceipts.android.purchases.apis.PurchaseResponse;
import co.smartreceipts.android.purchases.model.ConsumablePurchase;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.ManagedProduct;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

@ApplicationScope
public class OcrPurchaseTracker implements PurchaseEventsListener {

    private static final String GOAL = "Recognition";

    private final IdentityManager identityManager;
    private final ServiceManager serviceManager;
    private final PurchaseManager purchaseManager;
    private final PurchaseWallet purchaseWallet;
    private final LocalOcrScansTracker localOcrScansTracker;
    private final Scheduler subscribeOnScheduler;

    @Inject
    public OcrPurchaseTracker(Context context, IdentityManager identityManager, ServiceManager serviceManager,
                              PurchaseManager purchaseManager, PurchaseWallet purchaseWallet) {
        this(identityManager, serviceManager, purchaseManager, purchaseWallet, new LocalOcrScansTracker(context), Schedulers.io());
    }

    @VisibleForTesting
    OcrPurchaseTracker(@NonNull IdentityManager identityManager, @NonNull ServiceManager serviceManager,
                       @NonNull PurchaseManager purchaseManager, @NonNull PurchaseWallet purchaseWallet,
                       @NonNull LocalOcrScansTracker localOcrScansTracker, @NonNull Scheduler subscribeOnScheduler) {
        this.identityManager = Preconditions.checkNotNull(identityManager);
        this.serviceManager = Preconditions.checkNotNull(serviceManager);
        this.purchaseManager = Preconditions.checkNotNull(purchaseManager);
        this.purchaseWallet = Preconditions.checkNotNull(purchaseWallet);
        this.localOcrScansTracker = Preconditions.checkNotNull(localOcrScansTracker);
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    public void initialize() {
        Logger.info(this, "Initializing...");
        this.purchaseManager.addEventListener(this);
        this.identityManager.isLoggedInStream()
                .subscribeOn(subscribeOnScheduler)
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean isLoggedIn) {
                        return isLoggedIn;
                    }
                })
                .flatMap(new Func1<Boolean, Observable<?>>() {
                    @Override
                    public Observable<?> call(Boolean aBoolean) {
                        // Attempt to update our latest scan count
                        return fetchAndPersistAvailableRecognitions();
                    }
                })
                .flatMap(new Func1<Object, Observable<Set<ManagedProduct>>>() {
                    @Override
                    public Observable<Set<ManagedProduct>> call(Object next) {
                        return purchaseManager.getAllOwnedPurchases();
                    }
                })
                .flatMap(new Func1<Set<ManagedProduct>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Set<ManagedProduct> managedProducts) {
                        for (final ManagedProduct managedProduct : managedProducts) {
                            if (InAppPurchase.OcrScans50 == managedProduct.getInAppPurchase()) {
                                if (managedProduct instanceof ConsumablePurchase) {
                                    return uploadOcrPurchase((ConsumablePurchase) managedProduct);
                                }
                            }
                        }
                        return Observable.empty();
                    }
                })
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object next) {
                        Logger.info(OcrPurchaseTracker.this, "Successfully initialized");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.error(OcrPurchaseTracker.this, "Failed to initialize.", throwable);
                    }
                });
    }

    @Override
    public void onPurchaseSuccess(@NonNull InAppPurchase inAppPurchase, @NonNull PurchaseSource purchaseSource) {
        if (inAppPurchase == InAppPurchase.OcrScans50) {
            final ManagedProduct managedProduct = purchaseWallet.getManagedProduct(InAppPurchase.OcrScans50);
            if (managedProduct instanceof ConsumablePurchase) {
                final ConsumablePurchase consumablePurchase = (ConsumablePurchase) managedProduct;
                this.identityManager.isLoggedInStream()
                        .subscribeOn(subscribeOnScheduler)
                        .filter(new Func1<Boolean, Boolean>() {
                            @Override
                            public Boolean call(Boolean isLoggedIn) {
                                return isLoggedIn;
                            }
                        })
                        .flatMap(new Func1<Boolean, Observable<?>>() {
                            @Override
                            public Observable<?> call(Boolean aBoolean) {
                                return uploadOcrPurchase(consumablePurchase);
                            }
                        })
                        .subscribe(new Subscriber<Object>() {
                            @Override
                            public void onCompleted() {
                                Logger.info(OcrPurchaseTracker.this, "Successfully uploaded and consumed purchase of {}.", consumablePurchase.getInAppPurchase());
                            }

                            @Override
                            public void onError(Throwable e) {
                                Logger.error(OcrPurchaseTracker.this, "Failed to upload purchase of " + consumablePurchase.getInAppPurchase(), e);
                            }

                            @Override
                            public void onNext(Object o) {

                            }
                        });
            }
        }
    }

    @Override
    public void onPurchaseFailed(@NonNull PurchaseSource purchaseSource) {

    }

    /**
     * @return the remaining Ocr scan count that is allowed for this user. Please note that is
     * this not the authority for this (ie it's not the server), this may not be fully accurate, so we
     * may still get a remote error after a scan
     */
    public int getRemainingScans() {
        return localOcrScansTracker.getRemainingScans();
    }

    /**
     * @return {@code true} if we have OCR scans remaining. {@code false} otherwise. Please note that is
     * this not the authority for this (ie it's not the server), this may not be fully accurate, so we
     * may still get a remote error after a scan
     */
    public boolean hasAvailableScans() {
        return localOcrScansTracker.getRemainingScans() > 0;
    }

    /**
     * Decrements the remaining scan count by 1, to indicate that we've successfully used one of our scans
     */
    public void decrementRemainingScans() {
        localOcrScansTracker.decrementRemainingScans();
    }

    @NonNull
    private Observable<Object> uploadOcrPurchase(@NonNull final ConsumablePurchase consumablePurchase) {
        if (consumablePurchase.getInAppPurchase() != InAppPurchase.OcrScans50) {
            throw new IllegalArgumentException("Unsupported purchase type: " + consumablePurchase.getInAppPurchase());
        }
        Logger.info(this, "Uploading purchase: {}", consumablePurchase.getInAppPurchase());
        return serviceManager.getService(MobileAppPurchasesService.class).addPurchase(new PurchaseRequest(consumablePurchase, GOAL))
                .flatMap(new Func1<PurchaseResponse, Observable<?>>() {
                    @Override
                    public Observable<?> call(PurchaseResponse purchaseResponse) {
                        Logger.debug(OcrPurchaseTracker.this, "Received purchase response of {}", purchaseResponse);
                        return purchaseManager.consumePurchase(consumablePurchase);
                    }
                })
                .flatMap(new Func1<Object, Observable<?>>() {
                    @Override
                    public Observable<?> call(Object next) {
                        return fetchAndPersistAvailableRecognitions();
                    }
                });

    }

    @NonNull
    private Observable<Integer> fetchAndPersistAvailableRecognitions() {
        return this.identityManager.getMe()
                .subscribeOn(subscribeOnScheduler)
                .flatMap(new Func1<MeResponse, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(MeResponse meResponse) {
                        if (meResponse != null && meResponse.getUser() != null) {
                            return Observable.just(meResponse.getUser().getRecognitionsAvailable());
                        } else {
                            return Observable.error(new ApiValidationException("Failed to get a user response back"));
                        }
                    }
                })
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(Integer recognitionsAvailable) {
                        if (recognitionsAvailable != null) {
                            localOcrScansTracker.setRemainingScans(recognitionsAvailable);
                        }
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.error(OcrPurchaseTracker.this, "Failed to get the available OCR scans", throwable);
                    }
                })
                .onErrorReturn(new Func1<Throwable, Integer>() {
                    @Override
                    public Integer call(Throwable throwable) {
                        return 0; // ignore errors and keep moving to get the owned purchases
                    }
                });
    }
}

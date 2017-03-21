package co.smartreceipts.android.purchases.wallet;

import android.support.annotation.NonNull;

import java.util.Collection;

import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.ManagedProduct;

public interface PurchaseWallet {

    /**
     * Checks if this user owns a particular {@link InAppPurchase} for this application
     *
     * @param inAppPurchase the purchase to check for
     * @return {@code true} if it's both owned and active. {@code false} otherwise
     */
    boolean hasActivePurchase(@NonNull InAppPurchase inAppPurchase);

    /**
     * Adds a new purchase to our existing wallet
     *
     * @param managedProduct the {@link ManagedProduct} to add to our wallet
     */
    void addPurchaseToWallet(@NonNull ManagedProduct managedProduct);

    /**
     * Updates the list of purchased products that are owned in this wallet
     *
     * @param managedProducts the {@link Collection} of {@link ManagedProduct}s that are owned by this wallet
     */
    void updatePurchasesInWallet(@NonNull Collection<ManagedProduct> managedProducts);

    /**
     * Removes an existing purchase from our wallet
     *
     * @param inAppPurchase the {@link InAppPurchase} to remove from our wallet
     */
    void removePurchaseFromWallet(@NonNull InAppPurchase inAppPurchase);

}

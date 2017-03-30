package co.smartreceipts.android.purchases.wallet;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.Set;

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
     * Fetches the {@link ManagedProduct} that is associated with a particular {@link InAppPurchase}
     *
     * @param inAppPurchase - the {@link InAppPurchase} to look for
     * @return the corresponding {@link ManagedProduct} of {@code null} if this item is unowned
     */
    @Nullable
    ManagedProduct getManagedProduct(@NonNull InAppPurchase inAppPurchase);

    /**
     * Adds a new purchase to our existing wallet
     *
     * @param managedProduct the {@link ManagedProduct} to add to our wallet
     */
    void addPurchaseToWallet(@NonNull ManagedProduct managedProduct);

    /**
     * Updates the list of purchased products that are owned in this wallet
     *
     * @param managedProducts the {@link Set} of {@link ManagedProduct}s that are owned by this wallet
     */
    void updatePurchasesInWallet(@NonNull Set<ManagedProduct> managedProducts);

    /**
     * Removes an existing purchase from our wallet
     *
     * @param inAppPurchase the {@link InAppPurchase} to remove from our wallet
     */
    void removePurchaseFromWallet(@NonNull InAppPurchase inAppPurchase);

}

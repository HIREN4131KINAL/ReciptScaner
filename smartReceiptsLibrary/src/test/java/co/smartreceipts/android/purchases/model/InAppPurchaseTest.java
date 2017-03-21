package co.smartreceipts.android.purchases.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class InAppPurchaseTest {

    @Test
    public void getters() {
        assertEquals(InAppPurchase.SmartReceiptsPlus.getSku(), "pro_sku_3");
        assertEquals(InAppPurchase.SmartReceiptsPlus.getType(), Subscription.class);
        assertEquals(InAppPurchase.SmartReceiptsPlus.getProductType(), "subs");

        assertEquals(InAppPurchase.OcrScans50.getSku(), "TODO_OCR_TODO");
        assertEquals(InAppPurchase.OcrScans50.getType(), ConsumablePurchase.class);
        assertEquals(InAppPurchase.OcrScans50.getProductType(), "inapp");
    }

    @Test
    public void from() {
        assertEquals(InAppPurchase.SmartReceiptsPlus, InAppPurchase.from("pro_sku_3"));
        assertEquals(InAppPurchase.OcrScans50, InAppPurchase.from("TODO_OCR_TODO"));
    }

    @Test
    public void getConsumablePurchaseSkus() {
        final List<String> purchases = Collections.singletonList("TODO_OCR_TODO");
        assertEquals(InAppPurchase.getConsumablePurchaseSkus(), purchases);
    }

    @Test
    public void getSubscriptionSkus() {
        final List<String> purchases = Collections.singletonList("pro_sku_3");
        assertEquals(InAppPurchase.getSubscriptionSkus(), purchases);
    }

}
package co.smartreceipts.android.model.factory;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.impl.ImmutablePaymentMethodImpl;

/**
 * A {@link co.smartreceipts.android.model.Receipt} {@link co.smartreceipts.android.model.factory.BuilderFactory}
 * implementation, which will be used to generate instances of {@link co.smartreceipts.android.model.PaymentMethod} objects
 */
public class PaymentMethodBuilderFactory implements BuilderFactory<PaymentMethod> {

    private int _id;
    private String _method;

    /**
     * Default constructor for this class
     */
    public PaymentMethodBuilderFactory() {
        _id = -1;
        _method = "";
    }

    /**
     * Defines the primary key id for this object
     *
     * @param id - the id
     * @return this {@link PaymentMethodBuilderFactory} for method chaining
     */
    public PaymentMethodBuilderFactory setId(int id) {
        _id = id;
        return this;
    }

    /**
     * Defines the payment method type for this object
     *
     * @param method - the payment method
     * @return this {@link PaymentMethodBuilderFactory} for method chaining
     */
    public PaymentMethodBuilderFactory setMethod(@NonNull String method) {
        _method = method;
        return this;
    }

    /**
     * @return - the {@link co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory} object as set by the setter methods in this class
     */
    @NonNull
    public PaymentMethod build() {
        return new ImmutablePaymentMethodImpl(_id, _method);
    }
}

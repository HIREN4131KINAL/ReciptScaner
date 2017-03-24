package co.smartreceipts.android.apis.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;

public class PaymentMethodGsonAdapter implements GsonAdapter<PaymentMethod> {

    @Override
    public PaymentMethod deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final String method = json.getAsString();
        return new PaymentMethodBuilderFactory().setMethod(method).build();
    }
}

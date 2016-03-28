package co.smartreceipts.android.apis;

import android.support.annotation.NonNull;

import java.sql.Date;

import co.smartreceipts.android.model.gson.ExchangeRate;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * An interface that we can use in conjunction with network requests
 */
public interface ExchangeRateService {

    /**
     * Submits a synchronous network request to get the exchange rate for a particular currency on a given date
     *
     * @param date                 the desired date to get the exchange rate for
     * @param baseCurrencyCode     the base currency code (to which all conversion rates apply)
     * @param exchangeCurrencyCode the currency code that we would want to exchange the base into
     * @return the {@link co.smartreceipts.android.model.gson.ExchangeRate} containing this information
     */
    @GET("/{date}")
    ExchangeRate getExchangeRate(@NonNull @Path("date") Date date, @NonNull @Query("base") String baseCurrencyCode, @NonNull @Query("symbols") String exchangeCurrencyCode);

    /**
     * Submits an asynchronous network request to get the exchange rate for a particular currency on a given date
     *
     * @param date                 the desired date to get the exchange rate for
     * @param baseCurrencyCode     the base currency code (to which all conversion rates apply)
     * @param exchangeCurrencyCode the currency code that we would want to exchange the base into
     * @param callback             the callback that gets triggered when the request completes
     */
    @GET("/{date}")
    void getExchangeRate(@NonNull @Path("date") Date date, @NonNull @Query("base") String baseCurrencyCode, @NonNull @Query("symbols") String exchangeCurrencyCode, @NonNull Callback<ExchangeRate> callback);
}

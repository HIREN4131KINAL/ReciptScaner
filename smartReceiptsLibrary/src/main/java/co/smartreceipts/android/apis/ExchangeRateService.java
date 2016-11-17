package co.smartreceipts.android.apis;

import android.support.annotation.NonNull;

import java.sql.Date;

import co.smartreceipts.android.model.gson.ExchangeRate;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * An interface that we can use in conjunction with network requests
 */
public interface ExchangeRateService {

    /**
     * Submits an asynchronous network request to get the exchange rate for a particular currency on a given date
     *
     * @param date                 the desired date to get the exchange rate for
     * @param app_id               the app id token
     * @param baseCurrencyCode     the base currency code (to which all conversion rates apply)
     * @param callback             the callback that gets triggered when the request completes
     */
    @GET("/api/historical/{date}.json")
    void getExchangeRate(@NonNull @Path("date") Date date, @NonNull @Query("app_id") String app_id, @NonNull @Query("base") String baseCurrencyCode, @NonNull Callback<ExchangeRate> callback);

    /**
     * Submits an asynchronous network request to get the exchange rate for a particular currency on a given date
     *
     * @param date                 the desired date to get the exchange rate for
     * @param app_id               the app id token
     * @param baseCurrencyCode     the base currency code (to which all conversion rates apply)
     * @param exchangeCurrencyCode the currency code that we would want to exchange the base into
     * @param callback             the callback that gets triggered when the request completes
     */
    @GET("/api/historical/{date}.json")
    void getExchangeRate(@NonNull @Path("date") Date date, @NonNull @Query("app_id") String app_id, @NonNull @Query("base") String baseCurrencyCode, @NonNull @Query("symbols") String exchangeCurrencyCode, @NonNull Callback<ExchangeRate> callback);
}

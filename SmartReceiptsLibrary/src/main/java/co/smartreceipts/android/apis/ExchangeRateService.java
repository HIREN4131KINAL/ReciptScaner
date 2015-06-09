package co.smartreceipts.android.apis;

import java.util.Date;

import co.smartreceipts.android.model.gson.ExchangeRate;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface ExchangeRateService {

    @GET("/{date}")
    ExchangeRate getExchangeRate(@Path("date") String date, @Query("base") String baseCurrencyCode, @Query("symbol") String exchangeCurrencyCode);
}

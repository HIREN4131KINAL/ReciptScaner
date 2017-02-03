package co.smartreceipts.android.apis.hosts;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class BetaSmartReceiptsHostConfiguration implements HostConfiguration {

    private final SmartReceiptsGsonBuilder mSmartReceiptsGsonBuilder;

    public BetaSmartReceiptsHostConfiguration(@NonNull SmartReceiptsGsonBuilder smartReceiptsGsonBuilder) {
        mSmartReceiptsGsonBuilder = Preconditions.checkNotNull(smartReceiptsGsonBuilder);
    }

    @NonNull
    @Override
    public String getBaseUrl() {
        return "https://beta.smartreceipts.co";
    }

    @NonNull
    @Override
    public OkHttpClient getClient() {
        return getUnsafeOkHttpClient();
    }

    @NonNull
    @Override
    public Gson getGson() {
        return mSmartReceiptsGsonBuilder.create();
    }

    /**
     * Only meant for beta testing, so we don't have to buy another cert
     *
     * @return an UNSAFE {@link OkHttpClient}
     */
    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            final OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

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
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.store.IdentityStore;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class BetaSmartReceiptsHostConfiguration extends SmartReceiptsHostConfiguration {

    public BetaSmartReceiptsHostConfiguration(@NonNull IdentityStore identityStore, @NonNull SmartReceiptsGsonBuilder smartReceiptsGsonBuilder) {
        super(identityStore, smartReceiptsGsonBuilder);
    }

    @NonNull
    @Override
    public String getBaseUrl() {
        return "https://beta.smartreceipts.co";
    }

    @NonNull
    @Override
    protected OkHttpClient.Builder okHttpBuilder() {
        return getUnsafeOkHttpClient();
    }

    /**
     * Only meant for beta testing, so we don't have to buy another cert
     *
     * @return an UNSAFE {@link OkHttpClient}
     */
    private static OkHttpClient.Builder getUnsafeOkHttpClient() {
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

            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

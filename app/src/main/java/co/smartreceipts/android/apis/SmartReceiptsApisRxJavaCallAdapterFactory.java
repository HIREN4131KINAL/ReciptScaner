package co.smartreceipts.android.apis;

import android.support.annotation.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;

public class SmartReceiptsApisRxJavaCallAdapterFactory extends CallAdapter.Factory {

    private final CallAdapter.Factory original;

    private SmartReceiptsApisRxJavaCallAdapterFactory(@NonNull CallAdapter.Factory original) {
        this.original = original;
    }

    public static SmartReceiptsApisRxJavaCallAdapterFactory createWithScheduler(@NonNull Scheduler scheduler) {
        return new SmartReceiptsApisRxJavaCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(scheduler));
    }

    @Override
    public CallAdapter<?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        return new RxCallAdapterWrapper(retrofit, original.get(returnType, annotations, retrofit));
    }

    private static class RxCallAdapterWrapper implements CallAdapter<Observable<?>> {
        private final Retrofit retrofit;
        private final CallAdapter<?> wrapped;

        public RxCallAdapterWrapper(Retrofit retrofit, CallAdapter<?> wrapped) {
            this.retrofit = retrofit;
            this.wrapped = wrapped;
        }

        @Override
        public Type responseType() {
            return wrapped.responseType();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <R> Observable<?> adapt(Call<R> call) {
            return ((Observable) wrapped.adapt(call)).onErrorResumeNext(new Func1<Throwable, Observable>() {
                @Override
                public Observable call(Throwable throwable) {
                    return Observable.error(asPossiblyMappedException(throwable));
                }
            });
        }

        private Throwable asPossiblyMappedException(Throwable throwable) {
            // We had non-200 http error
            if (throwable instanceof HttpException) {
                final HttpException httpException = (HttpException) throwable;
                final Response response = httpException.response();
                return new SmartReceiptsApiException(response, httpException, retrofit);
            } else {
                // Network errors get returned as IO Exceptions, so pass those along
                return throwable;
            }
        }
    }
}


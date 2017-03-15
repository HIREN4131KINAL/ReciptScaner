package co.smartreceipts.android.identity.widget.presenters;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.common.base.Preconditions;
import com.jakewharton.rxbinding.view.RxView;

import co.smartreceipts.android.R;
import co.smartreceipts.android.identity.store.EmailAddress;
import rx.Observable;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class LogoutPresenter {

    private final View existingUserLayout;
    private final TextView myAccountWelcomeMessage;
    private final Button logoutButton;

    private CompositeSubscription compositeSubscription;

    public LogoutPresenter(@NonNull View view) {
        existingUserLayout = Preconditions.checkNotNull(view.findViewById(R.id.existing_user_layout));
        myAccountWelcomeMessage = Preconditions.checkNotNull((TextView) view.findViewById(R.id.my_account_welcome));
        logoutButton = Preconditions.checkNotNull((Button) view.findViewById(R.id.logout_button));
    }

    public void onResume() {
        compositeSubscription = new CompositeSubscription();
    }

    public void onPause() {
        if (compositeSubscription != null) {
            compositeSubscription.unsubscribe();
            compositeSubscription = null;
        }
    }

    @NonNull
    public Observable<Void> getLogoutStream() {
        return RxView.clicks(logoutButton)
                .doOnNext(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        logoutButton.setEnabled(false);
                    }
                });
    }

    public void present(@NonNull EmailAddress emailAddress) {
        existingUserLayout.setVisibility(View.VISIBLE);
        myAccountWelcomeMessage.setText(myAccountWelcomeMessage.getContext().getString(R.string.my_account_welcome, emailAddress));
    }

    public void hide() {
        existingUserLayout.setVisibility(View.GONE);
    }
}

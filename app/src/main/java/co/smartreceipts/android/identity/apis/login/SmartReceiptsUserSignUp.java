package co.smartreceipts.android.identity.apis.login;

import android.os.Parcel;
import android.support.annotation.NonNull;

public class SmartReceiptsUserSignUp extends UserCredentialsPayload {

    public SmartReceiptsUserSignUp(@NonNull String email, @NonNull String password) {
        super(null, email, password, null, LoginType.SignUp);
    }

    private SmartReceiptsUserSignUp(Parcel in) {
        super(in);
    }

    public static final Creator<SmartReceiptsUserSignUp> CREATOR = new Creator<SmartReceiptsUserSignUp>() {
        @Override
        public SmartReceiptsUserSignUp createFromParcel(Parcel source) {
            return new SmartReceiptsUserSignUp(source);
        }

        @Override
        public SmartReceiptsUserSignUp[] newArray(int size) {
            return new SmartReceiptsUserSignUp[size];
        }
    };
}

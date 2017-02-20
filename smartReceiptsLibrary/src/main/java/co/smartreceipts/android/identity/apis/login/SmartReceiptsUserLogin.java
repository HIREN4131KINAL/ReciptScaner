package co.smartreceipts.android.identity.apis.login;

import android.os.Parcel;
import android.support.annotation.NonNull;

public class SmartReceiptsUserLogin extends LoginParams {

    public SmartReceiptsUserLogin(@NonNull String email, @NonNull String password) {
        super("login", email, password, null);
    }

    private SmartReceiptsUserLogin(Parcel in) {
        super(in);
    }

    public static final Creator<SmartReceiptsUserLogin> CREATOR = new Creator<SmartReceiptsUserLogin>() {
        @Override
        public SmartReceiptsUserLogin createFromParcel(Parcel source) {
            return new SmartReceiptsUserLogin(source);
        }

        @Override
        public SmartReceiptsUserLogin[] newArray(int size) {
            return new SmartReceiptsUserLogin[size];
        }
    };
}

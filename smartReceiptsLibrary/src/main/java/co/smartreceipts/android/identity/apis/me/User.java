package co.smartreceipts.android.identity.apis.me;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {

    private String id;
    private String email;

    private String name;
    private String display_name;
    private List<String> registration_ids;
    private Cognito cognito;

    public User(@NonNull List<String> registrationIds) {
        this.registration_ids = Preconditions.checkNotNull(registrationIds);
    }

    @Nullable
    public String getId() {
        return id;
    }

    @Nullable
    public String getEmail() {
        return email;
    }



    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getDisplayName() {
        return display_name;
    }

    @Nullable
    public List<String> getRegistrationIds() {
        return registration_ids;
    }

    @Nullable
    public Cognito getCognito() {
        return this.cognito;
    }

}

package co.smartreceipts.android.identity.apis.organizations;

import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.List;

public class OrganizationsResponse implements Serializable {

    private final List<Organization> organizations;

    public OrganizationsResponse(List<Organization> organizations) {
        this.organizations = organizations;
    }

    @Nullable
    public List<Organization> getOrganizations() {
        return organizations;
    }
}

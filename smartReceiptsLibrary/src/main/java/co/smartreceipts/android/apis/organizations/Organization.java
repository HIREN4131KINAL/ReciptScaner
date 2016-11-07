package co.smartreceipts.android.apis.organizations;

import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.List;

public class Organization implements Serializable {

    private final String id;
    private final String name;
    private final long createdAt;
    private final AppSettings appSettings;
    private final List<OrganizationUser> organizationUsers;

    public Organization(String id, String name, long createdAt, AppSettings appSettings, List<OrganizationUser> organizationUsers) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.appSettings = appSettings;
        this.organizationUsers = organizationUsers;
    }

    @Nullable
    public String getId() {
        return id;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public long getCreatedAt() {
        return createdAt;
    }

    @Nullable
    public AppSettings getAppSettings() {
        return appSettings;
    }

    @Nullable
    public List<OrganizationUser> getOrganizationUsers() {
        return organizationUsers;
    }
}

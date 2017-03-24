package co.smartreceipts.android.settings.versions;

public interface VersionUpgradedListener {

    /**
     * Called when the application version was upgraded.
     * <p>
     * This was added after version 78 was release, so version 79 was the first "new" one
     * </p>
     *
     * @param oldVersion the old application version
     * @param newVersion the new application version
     */
    void onVersionUpgrade(int oldVersion, int newVersion);

}

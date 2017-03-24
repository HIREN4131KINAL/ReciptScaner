package co.smartreceipts.android.sync.network;

interface PossibleNetworkChangeListener {

    void initialize();

    void deinitialize();

    void onPossibleNetworkStateChange();
}

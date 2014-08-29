package co.smartreceipts.android.sync;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class operates as the entry point for all data synchronization activities
 * 
 * @author Will Baumann
 * 
 */
public final class SyncManager {

	private final CopyOnWriteArraySet<SyncProvider> mProviders;
	private final NetworkProvider mNetworkProvider;

	public SyncManager(NetworkProvider networkProvider) {
		this(Collections.<SyncProvider> emptyList(), networkProvider);
	}

	public SyncManager(List<SyncProvider> providers, NetworkProvider networkProvider) {
		mProviders = new CopyOnWriteArraySet<SyncProvider>(providers);
		mNetworkProvider = networkProvider;
	}

	/**
	 * Registers a {@link SyncProvider} with this manager
	 * 
	 * @param syncProvider
	 *            - the desired {@link SyncProvider} to register
	 * @return {@code true} if this provider was successfully registered. {@code false} if is was previously registered
	 *         or is {@code null}
	 */
	public boolean registerSyncProvider(SyncProvider syncProvider) {
		if (syncProvider == null) {
			return false;
		}
		return mProviders.add(syncProvider);
	}

	/**
	 * Unregisters a {@link SyncProvider} with this manager
	 * 
	 * @param syncProvider
	 *            - the desired {@link SyncProvider} to unregister
	 * @return {@code true} if this provider was successfully unregistered. {@code false} if is was not previously
	 *         registered or is {@code null}
	 */
	public boolean unregisterSyncProvider(SyncProvider syncProvider) {
		if (syncProvider == null) {
			return false;
		}
		return mProviders.remove(syncProvider);
	}

	/**
	 * Submits a synchronization request to be uploaded to our back-end
	 * 
	 * @param syncRequest
	 *            - the {@link SyncRequest} to upload
	 * @return {@code true} if at {@link SyncProvider} is registered that supports this request type
	 */
	public boolean submitSyncRequest(SyncRequest<?> syncRequest) {
		boolean wasSumbitted = false;
		for (final SyncProvider provider : mProviders) {
			if (provider.supportsSynchronization(syncRequest)) {

				wasSumbitted = true;
			}
		}
		return wasSumbitted;
	}
}

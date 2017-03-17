package wb.android.storage;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wb.android.BuildConfig;

public final class SDCardFileManager extends StorageManager {
	
	//logging variables
    private static final boolean D = false;
    private static final String TAG = "SDCardFileManager";
	
    //instance vars
	private String[] mAllowedStates;
	private Context mContext;
	
	protected SDCardFileManager(Context context) throws SDCardStateException {
		super(context.getExternalFilesDir(null));
		if (D) Log.d(TAG, "Creating External SD Card"); 
		final String state = Environment.getExternalStorageState();
		if (D) Log.d(TAG, "External Storage State: " + state);
		mAllowedStates = null;
		mContext = context;
		if (_root == null)
		    throw new SDCardStateException(state);
	}
	
	protected SDCardFileManager(Context context, String[] allowedStates) throws SDCardStateException {
		super(context.getExternalFilesDir(null));
		if (D) Log.d(TAG, "Creating External SD Card");
		final String state = Environment.getExternalStorageState();
		if (D) Log.d(TAG, "External Storage State: " + state);
		mAllowedStates = allowedStates;
		mContext = context;
		final int size = allowedStates.length;
		for (int i = 0; i < size; i++) {
			if (!allowedStates[i].equals(state))
				throw new SDCardStateException(state);
		}
	} 
		
	public boolean isCurrentStateValid() {
		final String state = Environment.getExternalStorageState();
		if (mAllowedStates == null)
			return (Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
		else {
			final int size = mAllowedStates.length;
			for (int i = 0; i < size; i++) {
				if (!mAllowedStates[i].equals(state))
					return false;
			}
			return true;
		}
	}
	
	Context getContext() {
		return mContext;
	}
    
    private static final File MOUNTS_FILE = new File("/proc/mounts");
    private static final File VOLD_FILE = new File("/system/etc/vold.fstab");
    
    // Adapted from: http://stackoverflow.com/questions/7450650/how-to-list-additional-external-storage-folders-mount-points
    public static HashSet<Mount> parseMountsFile() {
    	HashSet<Mount> storageSet = new HashSet<Mount>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(MOUNTS_FILE)));
            // Replace pattern matcher with more permissive one (don't require /mnt)
            Pattern pattern = Pattern.compile("/dev/.*?( /.+?) .*");
            String line;
            while ((line = reader.readLine()) != null) {
            	if (TextUtils.isEmpty(line) || line.startsWith("#")) {
            		continue;
            	}
            	else {
            		Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        storageSet.add(new Mount(matcher.group(1), line));
                    }
            	}
            }
        }
        catch(IOException e) {
        	if (BuildConfig.DEBUG) {
				Log.e(TAG, e.toString(), e);
			}
        }
        finally {
        	if (reader != null) {
        		try {
					reader.close();
				} catch (IOException e) {
					if (BuildConfig.DEBUG) {
						Log.e(TAG, e.toString(), e);
					}
				}
        	}
        }
        return storageSet;
    }
    
    public static HashSet<Mount> parseVoldFile() {
    	HashSet<Mount> storageSet = new HashSet<Mount>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(MOUNTS_FILE)));
            String line;
            while ((line = reader.readLine()) != null) {
            	if (TextUtils.isEmpty(line) || line.startsWith("#")) {
            		continue;
            	}
            	else {
            		if (line.startsWith("dev_mount")) {
                        String[] lineElements = line.split(" ");
                        if (lineElements.length > 2) {
	                        String element = lineElements[2];
	                        if (element.contains(":")) {
	                            element = element.substring(0, element.indexOf(":"));
	                        }
	                        storageSet.add(new Mount(element, line, true));
                        }
                        
                    }
            	}
            }
        }
        catch(IOException e) {
        	if (BuildConfig.DEBUG) {
				Log.e(TAG, e.toString(), e);
			}
        }
        finally {
        	if (reader != null) {
        		try {
					reader.close();
				} catch (IOException e) {
					if (BuildConfig.DEBUG) {
						Log.e(TAG, e.toString(), e);
					}
				}
        	}
        }
        return storageSet;
    }
    
    public static final class Mount {
    	
    	private final File mMountPoint;
    	private final boolean mReadWrite;
    	
    	private Mount(String group, String line) {
    		if (TextUtils.isEmpty(group) || TextUtils.isEmpty(line)) {
    			mMountPoint = null;
    			mReadWrite = false;
    		}
    		else {
    			mMountPoint = new File(group.trim());
    			mReadWrite = line.contains("rw,");
    		}
    	}
    	
    	private Mount(String group, String line, boolean testRW) {
    		if (TextUtils.isEmpty(group) || TextUtils.isEmpty(line)) {
    			mMountPoint = null;
    			mReadWrite = false;
    		}
    		else {
    			mMountPoint = new File(group.trim());
    			mReadWrite = mMountPoint.canWrite();
    		}
    	}
    	
    	public boolean isValidMount() {
    		return mReadWrite && mMountPoint != null && mMountPoint.exists() && mMountPoint.canWrite();
    	}
    	
    	public File getMountPoint() {
    		return mMountPoint;
    	}
    	
    	@Override
    	public int hashCode() {
    		return 13*mMountPoint.hashCode() + 7 * Boolean.valueOf(mReadWrite).hashCode();
    	}
    	
		@Override
    	public boolean equals(Object o) {
    		if (!(o instanceof Mount)) {
    			return false;
    		}
    		else {
    			Mount mount = (Mount) o;
    			if (mReadWrite != mount.mReadWrite) {
    				return false;
    			}
    			else {
    				if (mMountPoint != null && mount.mMountPoint == null) {
    					return false;
    				}
    				else if (mMountPoint == null && mount.mMountPoint != null) {
    					return false;
    				}
    				else if (mMountPoint == null && mount.mMountPoint == null) {
    					return true;
    				}
    				else {
    					try {
							return mMountPoint.getCanonicalPath().equals(mount.mMountPoint.getCanonicalPath());
						} catch (IOException e) {
							return false;
						}
    				}
    			}
    		}
    	}
    	
    	
    }
	
}
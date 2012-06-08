package wb.android.storage;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

//There are a lot of helper methods here to make sure that no functionality is lost if a method is overwritten
public class StorageManager {
	
	private static final String TAG = "StorageManager";
	
	protected File _root;
	
	private static SDCardFileManager _externalInstance = null;
	private static InternalStorageManager _internalInstance = null;
	
	protected StorageManager(File root) {
		_root = root;
	}
	
	public static StorageManager getInstance(Activity activity) {
		if (_externalInstance != null)
			return _externalInstance;
		if (_internalInstance != null)
			return _internalInstance;
		try {
			_externalInstance = new SDCardFileManager(activity);
			return _externalInstance;
		} catch (SDCardStateException e) {
			_internalInstance = new InternalStorageManager(activity);
			return _internalInstance;
		}
	}
	
	public static final SDCardFileManager getExternalInstance(Activity activity) throws SDCardStateException {
		if (_externalInstance != null)
			return _externalInstance;
		_externalInstance = new SDCardFileManager(activity);
		return _externalInstance;
	}
	
	public static final SDCardFileManager getExternalInstance(Activity activity, String[] allowedStates) throws SDCardStateException {
		if (_externalInstance != null)
			return _externalInstance;
		_externalInstance = new SDCardFileManager(activity, allowedStates);
		return _externalInstance;
	}
	
	public static final InternalStorageManager getInternalInstance(Activity activity) {
		if (_internalInstance != null)
			return _internalInstance;
		_internalInstance = new InternalStorageManager(activity);
		return _internalInstance;
	}
	
	public static String getRootPath() {
		 if (_externalInstance != null)
			 return _externalInstance._root.getAbsolutePath();
		 else if (_internalInstance != null)
			 return _internalInstance._root.getAbsolutePath();
		 else
			 return "";
	}

	public boolean isRoot(final File dir) {
		return dir.equals(_root);
	}
	
	// Returns the directory if successfully built. Returns null otherwise
	public File mkdir(final String name) {
		return mkdirHelper(_root, name);
	}
	
	public File mkdir(final File root, final String name) {
		return mkdirHelper(root, name);
	}
	
	private final File mkdirHelper(final File root, final String name) {
		final File dir = new File(root, name);
		if (!dir.exists()) {
			final boolean success = dir.mkdir();
			if (!success)
				return null;
		}
		return dir;
	}
	
	public File getFile(final String filename) {
		return new File(_root, filename);
	}
	
	public File getFile(final File root, final String filename) {
		return new File(root, filename);
	}
	
	public File rename(final File oldFile, final String newName) {
		final File file = this.getFile(newName);
		final boolean success = oldFile.renameTo(file);
		if (success)
			return file;
		else
			return oldFile;
	}
	
	public boolean delete(final String filename) {
		return deleteHelper(new File(_root, filename));
	}
	
	public boolean delete(final File dir, final String filename) {
		return deleteHelper(new File(dir, filename));
	}
	
	public boolean delete(final File file) {
		return deleteHelper(file);
	}
	
	private final boolean deleteHelper(final File file) {
		if (file == null || !file.exists())
			return true;
		if  (!file.canWrite())
			return false;
		if (file.isDirectory() && file.listFiles().length > 0)
			return false;
		return file.delete();
	}
	
	public boolean deleteRecursively(final File dir) {
		if (dir == null || !dir.exists())
			return true;
		if (!dir.canWrite())
			return false;
		if (dir.isDirectory()) {
			final File[] files = dir.listFiles();
			final int len = files.length;
			for (int i = 0; i < len; i++) 
				deleteRecursively(files[i]);
			if (dir.listFiles().length > 0)
				return false;
		}
		return dir.delete();
	}
	
	public boolean deleteAll() {
		final File[] files = _root.listFiles();
		final int len = files.length;
		for (int i = 0; i < len; i++) 
			deleteRecursively(files[i]);
		return (_root.listFiles().length == 0);
	}
	
	public boolean write(final String filename, final byte[] data) {
		return writeHelper(_root, filename, data);
	}
	
	public boolean write(final File dir, final String filename, final byte[] data) {
		return writeHelper(dir, filename, data);
	}
	
	private final boolean writeHelper(final File dir, final String filename, final byte[] data) {
		String path = dir.toString();
		if (!path.endsWith(File.separator))
			path += File.separator;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path + filename);
			fos.write(data);
			fos.close();
		} catch (IOException e) {
			return false;
		} finally {
			try {
				if (fos != null)
					fos.close();
			}
			catch (IOException e) {}
		}
		return true;
	}
	
	public boolean write(final String filename, final String data) {
		return writeHelper(_root, filename, data);
	}
	
	public boolean write(final File dir, final String filename, final String data) {
		return writeHelper(dir, filename, data);
	}
	
	private final boolean writeHelper(final File dir, final String filename, final String data) {
		String path = dir.toString();
		if (!path.endsWith(File.separator))
			path += File.separator;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path + filename);
			fos.write(data.getBytes());
			fos.close();
		} catch (IOException e) {
			return false;
		} finally {
			try {
				if (fos != null)
					fos.close();
			}
			catch (IOException e) {}
		}
		return true;
	}
	
	public boolean createFile(final File file) {
		try {
			if (!file.exists()) {
				return file.createNewFile();
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public boolean writeBitmap(final Bitmap bitmap, final String filename, final CompressFormat format, int quality) {
		return writeBitmapHelper(_root, bitmap, filename, format, quality);
	}
	
	public boolean writeBitmap(final File dir, final Bitmap bitmap, final String filename, final CompressFormat format, int quality) {
		return writeBitmapHelper(dir, bitmap, filename, format, quality);
	}
	
	private final boolean writeBitmapHelper(final File dir, final Bitmap bitmap, final String filename, final CompressFormat format, int quality) {
		String path = dir.toString();
		if (!path.endsWith(File.separator))
			path += File.separator;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path + filename);
			bitmap.compress(format, quality, fos);
			fos.close();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return false;
		} finally {
			try {
				if (fos != null)
					fos.close();
			}
			catch (IOException e) {}
		}
		return true;
	}
	
	public boolean writeBitmap(final Uri imageUri, final Bitmap bitmap, final CompressFormat format, int quality) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(imageUri.getPath());
			bitmap.compress(format, quality, fos);
			fos.close();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return false;
		} finally {
			try {
				if (fos != null)
					fos.close();
			}
			catch (IOException e) {}
		}
		return true;
	}
		
	public byte[] read(final File f) {
		byte[] buffer;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			buffer = new byte[(int)f.length()];
			fis.read(buffer);
			fis.close();
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			buffer = null;
			return buffer;
		} finally {
			try {
				if (fis != null)
					fis.close();
			}
			catch (IOException e) {
				return null;
			}
		}
		return buffer;
	}
	
	public File[] listFilesAndDirectories() {
		return _root.listFiles();
	}
	
	public File[] listFilesAndDirectories(final File root) {
		return root.listFiles();
	}
	
	public File[] listFiles() {
		return listFilesHelper(_root);
	}
	
	public File[] listFiles(final File root) {
		return listFilesHelper(root);
	}
	
	private final File[] listFilesHelper(final File root) {
		FileFilter filesFilter = new FileFilter() {
			public final boolean accept(File file) {
				return !file.isDirectory();
			}
		};
		return this.list(root, filesFilter);
	}
	
	public File[] listDirs() {
		return listDirsHelper(_root);
	}
	
	public File[] listDirs(final File root) {
		return listDirsHelper(root);
	}
	
	private final File[] listDirsHelper(final File root) {
		FileFilter dirsFilter = new FileFilter() {
			public final boolean accept(File file) {
				return file.isDirectory();
			}
		};
		return this.list(root, dirsFilter);
	}
	
	public File[] list(final FileFilter filter) {
		return _root.listFiles(filter);
	}
	
	public File[] list(final File root, final FileFilter filter) {
		return root.listFiles(filter);
	}
	
	public File[] list(final String extension) {
		return listHelper(_root, extension);
	}
	
	public File[] list(final File root, final String extension) {
		return listHelper(root, extension);
	}
	
	private final File[] listHelper(final File root, final String extension) {
		FileFilter ff = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().toLowerCase().endsWith(extension))
					return true;
				else
					return false;
			}
		};
		return list(root, ff);
	}
	
	public Bitmap getBitmap(final String filename) {
		return getBitmapHelper(_root, filename);
	}
	
	public Bitmap getBitmap(final File root, final String filename) {
		return getBitmapHelper(root, filename);
	}
	
	private final Bitmap getBitmapHelper(final File root, final String filename) {
		try {
			File path = new File(root, filename);
			return BitmapFactory.decodeFile(path.getCanonicalPath());
		} catch (IOException e) {
			return null;
		}
	}
	
	public boolean isExternal() {
		return (this instanceof SDCardFileManager);
	}
	
	public FileOutputStream getFOS(String filename) throws FileNotFoundException {
		return getFOSHelper(_root, filename);
	}
	
	public FileOutputStream getFOS(File dir, String filename) throws FileNotFoundException {
		return getFOSHelper(dir, filename);
	}
	
	private final FileOutputStream getFOSHelper(File dir, String filename) throws FileNotFoundException {
		String path = dir.toString();
		if (!path.endsWith(File.separator))
			path += File.separator;
		return new FileOutputStream(path + filename);
	} 
	
}
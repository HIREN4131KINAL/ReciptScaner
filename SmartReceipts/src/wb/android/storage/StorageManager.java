package wb.android.storage;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

public abstract class StorageManager {
	
	protected File _root;
	private FileFilter _filesFilter, _dirsFilter;
	
	protected StorageManager() {}

	public final boolean isRoot(final File dir) {
		return dir.equals(_root);
	}
	
	// Returns the directory if successfully built. Returns null otherwise
	public final File mkdir(final String name) {
		return mkdir(_root, name);
	}
	
	public final File mkdir(final File root, final String name) {
		final File dir = new File(root, name);
		if (!dir.exists()) {
			final boolean success = dir.mkdir();
			if (!success)
				return null;
		}
		return dir;
	}
	
	public final File getFile(final String filename) {
		return getFile(_root, filename);
	}
	
	public final File getFile(final File root, final String filename) {
		return new File(root, filename);
	}
	
	public final File rename(final File oldFile, final String newName) {
		final File file = this.getFile(newName);
		final boolean success = oldFile.renameTo(file);
		if (success)
			return file;
		else
			return oldFile;
	}
	
	public final boolean delete(final File file) {
		if (file == null || !file.exists())
			return true;
		if  (!file.canWrite())
			return false;
		if (file.isDirectory() && file.listFiles().length > 0)
			return false;
		return file.delete();
	}
	
	public final boolean delete(final String filename) {
		return delete(_root, filename);
	}
	
	public final boolean delete(final File dir, final String filename) {
		File del = new File(dir, filename);
		return delete(del);
	}
	
	public final boolean deleteRecursively(final File dir) {
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
	
	public final boolean deleteAll() {
		final File[] files = _root.listFiles();
		final int len = files.length;
		for (int i = 0; i < len; i++) 
			deleteRecursively(files[i]);
		return (_root.listFiles().length == 0);
	}
	
	public final boolean write(final String filename, final byte[] data) {
		return this.write(_root, filename, data);
	}
	
	public final boolean write(final File dir, final String filename, final byte[] data) {
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
	
	public final boolean write(final String filename, final String data) {
		return this.write(_root, filename, data);
	}
	
	public final boolean write(final File dir, final String filename, final String data) {
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
	
	public final boolean writeBitmap(final Bitmap bitmap, final String filename, final CompressFormat format, int quality) {
		return this.writeBitmap(_root, bitmap, filename, format, quality);
	}
	
	public final boolean writeBitmap(final File dir, final Bitmap bitmap, final String filename, final CompressFormat format, int quality) {
		String path = dir.toString();
		if (!path.endsWith(File.separator))
			path += File.separator;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path + filename);
			bitmap.compress(format, quality, fos);
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
		
	public final byte[] read(final File f) {
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
	
	public final File[] listFilesAndDirectories() {
		return this.listFilesAndDirectories(_root);
	}
	
	public final File[] listFilesAndDirectories(final File root) {
		return _root.listFiles();
	}
	
	public final File[] listFiles() {
		return this.listFiles(_root);
	}
	
	public final File[] listFiles(final File root) {
		if (_filesFilter == null) {
			_filesFilter = new FileFilter() {
				public final boolean accept(File file) {
					return !file.isDirectory();
				}
			};
		}
		return this.list(root, _filesFilter);
	}
	
	public final File[] listDirs() {
		return this.listDirs(_root);
	}
	
	public final File[] listDirs(final File root) {
		if (_dirsFilter == null) {
			_dirsFilter = new FileFilter() {
				public final boolean accept(File file) {
					return file.isDirectory();
				}
			};
		}
		return this.list(root, _dirsFilter);
	}
	
	public final File[] list(final FileFilter filter) {
		return this.list(_root, filter);
	}
	
	public final File[] list(final File root, final FileFilter filter) {
		return root.listFiles(filter);
	}
	
	public File[] list(final String extension) {
		return list(_root, extension);
	}
	
	public final File[] list(final File root, final String extension) {
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
	
	public final Bitmap getBitmap(final String filename) {
		return getBitmap(_root, filename);
	}
	
	public final Bitmap getBitmap(final File root, final String filename) {
		try {
			File path = new File(root, filename);
			return BitmapFactory.decodeFile(path.getCanonicalPath());
		} catch (IOException e) {
			return null;
		}
	}
	
	public abstract FileOutputStream getFOS(String filename, int mode) throws FileNotFoundException;
	public abstract FileOutputStream getFOS(File dir, String filename, int mode) throws FileNotFoundException;
	public abstract boolean isCurrentStateValid();
	public abstract boolean isExternal();
	
}

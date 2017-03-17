package wb.android.dialog;

import android.app.Dialog;

import java.util.HashMap;

public class DialogCache {
	
	final HashMap<Integer, Dialog> cache;
	private Dialog _lastHolder;
	private int _lastID = -1;
	
	public DialogCache() {
		cache = new HashMap<Integer, Dialog>();
	}
	
	public DialogCache(int capacity) {
		cache = new HashMap<Integer, Dialog>(capacity);
	}
	
	public DialogCache(DialogCache dialogCache) {
		cache = new HashMap<Integer, Dialog>(dialogCache.cache);
	}
	
	public final Dialog get(int dialogID) {
		if (_lastID == dialogID)
			return _lastHolder;
		if (cache.containsKey(dialogID))
			return cache.get(dialogID);
		else
			return null;
	}
	
	public final boolean show(int dialogID) {
		if (_lastID == dialogID) {
			_lastHolder.show();
			return true;
		}
		if (cache.containsKey(dialogID)) {
			cache.get(dialogID).show();
			return true;
		}
		else
			return false;
	}
	
	public final boolean contains(int dialogID) {
		return cache.containsKey(dialogID);
	}
	
	public final void put(Dialog dialog, int dialogID) {
		cache.put(dialogID, dialog);
		_lastID = dialogID;
		_lastHolder = dialog;
	}
	
	public final Dialog remove(int dialogID) {
		return cache.remove(dialogID);
	}

}

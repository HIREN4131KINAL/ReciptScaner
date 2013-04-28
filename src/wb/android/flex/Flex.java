package wb.android.flex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.R.id;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import wb.android.fkexstorage.InternalStorageManager;
import wb.android.fkexstorage.StorageManager;

public class Flex {
	
	private static final String TAG = "FLEX";
	static final boolean D = true;
	
	private static final String FLEX_PREFERENCES = "Flex_Preferences";
	private static final String STRING_FLEX_FILE = "FlexFile";
	
	private static final String DEFAULT_FILENAME = "Flex.xml";
	
	private static Flex INSTANCE = null;
	private Activity activity;
	private LayoutInflater inflater;
	private FlexViews flexViews;
	private FlexStrings flexStrings;
	
	private Flex(Activity activity, Flexable flexable) {
		this.activity = activity;
		inflater = LayoutInflater.from(activity);
		int rawID = flexable.getFleXML();
		
		//Try to find an old flex file
		SharedPreferences prefs = activity.getSharedPreferences(FLEX_PREFERENCES, 0);
    	String flexFilePath = prefs.getString(STRING_FLEX_FILE, "");
    	if (D) Log.d(TAG, flexFilePath);
    	
		if (this.isFleXMLDefined(rawID)) {
			InputStream is = activity.getResources().openRawResource(rawID);
			this.parseFleXML(activity, is); //Since this InputStream gets closed here
			is = activity.getResources().openRawResource(rawID);
			if (flexFilePath.length() > 0) { //A Flex file exists. Let's test if any updates have been applied
				String rawHash = StorageManager.getMD5Checksum(is);
				String flexHash = StorageManager.getMD5Checksum(new File(flexFilePath));
				if (D) Log.d(TAG, "Raw Hash: " + rawHash);
				if (D) Log.d(TAG, "Flex Hash: " + flexHash);
				if (rawHash != null && flexHash != null) {
					if (!rawHash.equalsIgnoreCase(flexHash)) {
						//It really should check that the file on Disk has a lesser version number, instead of just auto overwriting
						writeFlexFileToSD(is, prefs);
					}
				}
				else { //Something went wrong. Try writing the file again
					writeFlexFileToSD(is, prefs);
				}
			}
			else { //Write the new Flex File
				writeFlexFileToSD(is, prefs);
			}
			try { if (is != null) is.close(); } catch (IOException e) { Log.e(TAG, e.toString()); }
		}
		else {
	    	if (flexFilePath.length() > 0) { //If a flex file exists. If not, all defaults will be used
	    		FileInputStream fis = null;
	    		try {
	    			fis = new FileInputStream(flexFilePath);
					this.parseFleXML(activity, fis);
					fis.close();
				} 
	    		catch (FileNotFoundException e) {
					Log.e(TAG, "The defined flex file does not exists: " + flexFilePath);
				} catch (IOException e) {
					Log.e(TAG, toString());
				}
	    		finally {
	    			try {
	    				if (fis != null) fis.close();
	    			} 
	    			catch (IOException e) {
						Log.e(TAG, toString());
					}
	    		}
	    	}
		}
	}
	
	private final void writeFlexFileToSD(InputStream is, SharedPreferences prefs) {
		InternalStorageManager internal = StorageManager.getInternalInstance(activity);
		byte[] data = internal.read(is);
		internal.write(DEFAULT_FILENAME, data);
		File flexFile = internal.getFile(DEFAULT_FILENAME);
		if (flexFile != null && flexFile.exists()) {
			Editor editor = prefs.edit();
			editor.putString(STRING_FLEX_FILE, flexFile.getAbsolutePath());
			editor.commit();
			if (D) Log.d(TAG, "Wrote the Flex file to: " + flexFile.getAbsolutePath());
		}
	}
	
	private final boolean isFleXMLDefined(int rawID) {
		if (rawID == Flexable.UNDEFINED) {
			if (D) Log.d(TAG, "No FleXML file was defined. All default data will be used.");
			return false;
		}
		else 
			return true;
	}
	
	public void onResume() {
		
	}
	
	public void onPause() {
		
	}
	
	public static final Flex getInstance(Activity activity, Flexable flexable) { //throws FlexFailedException {
		if (INSTANCE != null) {
			if (INSTANCE.activity != activity) //Our current activity has been changed or destroyed
				INSTANCE = new Flex(activity, flexable);
			return INSTANCE;
		}
		INSTANCE = new Flex(activity, flexable);
		return INSTANCE;
	}
		
	private final void parseFleXML(Activity activity, InputStream is) {
		try { 
			SAXParserFactory spf = SAXParserFactory.newInstance(); 
			SAXParser sp = spf.newSAXParser(); 
			XMLReader xr = sp.getXMLReader(); 
			FlexHandler dataHandler = new FlexHandler(this); 
			xr.setContentHandler(dataHandler); 
			xr.parse(new InputSource(is));
		} catch(Exception e) {
			Log.e(TAG, e.toString());
		}
	}
	
	public String getString(int resId) {
		String string = activity.getString(resId);
		if (flexStrings == null) 
			return string;
		else {
			String name = activity.getResources().getResourceEntryName(resId);
			return flexStrings.update(string, name);		
		}
	}
	
	public String[] getStringArray(int resID) {
		return activity.getResources().getStringArray(resID);
	}
	
	public View getView(int layoutID) {
    	return inflater.inflate(layoutID, null);
	}
	
	public View getSubView(View parent, int resId) {
		View view = parent.findViewById(resId);
		if (flexViews == null) return view;
		String id = "@+id/" + activity.getResources().getResourceEntryName(resId);
		if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			try {
				flexViews.addFlexViewsToParent(activity, group, id);
			} catch (FlexFailedException e) {
				Log.e(TAG, e.toString());
			}
		}
		try {
			if (view instanceof EditText)
				flexViews.updateView((EditText)view, id);
			else if (view instanceof CheckBox)
				flexViews.updateView((CheckBox)view, id);
			else
				flexViews.updateView(view, id);
		} catch (FlexFailedException e) {
			Log.e(TAG, e.toString());
		}
		return view;
	}
	
	/*
	public <T extends View> T getSubView(View parent, int resId, Class<T> viewClass) {
		View view = parent.findViewById(resId);
		T t = viewClass.cast(view);
		if (flexViews == null) return viewClass.cast(view);
		if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			try {
				flexViews.addFlexViewsToParent(activity, group);
			} catch (FlexFailedException e) {
				Log.e(TAG, e.toString());
			}
		}
		try {
			if (view instanceof EditText)
				flexViews.updateView((EditText)t);
		} catch (FlexFailedException e) {
			e.printStackTrace();
		}
		return t;
	}*/
	
	void setFlexViews(FlexViews flexViews) {
		this.flexViews = flexViews;
	}
	
	void setFlexStrings(FlexStrings flexStrings) {
		this.flexStrings = flexStrings;
	}
	
	enum Element {
		FLEX ("Flex"),
		FLEX_VIEWS ("FlexViews"),
		FLEX_STRINGS ("FlexStrings");
		
		private String name;
		private Element(String name) {this.name = name;}
		boolean isTag(String localName) {return this.name.equalsIgnoreCase(localName); }
		String tagName() { return this.name; }
	}
	
	/*Attribs
	 * xmlns:android="http://schemas.android.com/apk/res/android" 
	  version="1" 
	  apkDistURL="www.test.com/app.apk" 
	  flexURL="www.test.com/flex.xml"
	  customerID="JC1">
	 */

}
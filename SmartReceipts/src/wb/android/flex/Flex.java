package wb.android.flex;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;

public class Flex {
	
	private static final String TAG = "FLEX";
	private static final boolean D = true;
	
	private static Flex INSTANCE = null;
	private Context context;
	private LayoutInflater inflater;
	
	private Flex(Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);
	}
	
	public void update(Context context) {
		if (this.context != context) {
			this.context = context;
			inflater = LayoutInflater.from(context);
		}
	}
	
	public static final Flex getInstance(Context context) { //throws FlexFailedException {
		if (INSTANCE != null)
			return INSTANCE;
		INSTANCE = new Flex(context);
		return INSTANCE;
	}
	
	public static final void test(Context context, int rawID) {
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xml = factory.newPullParser();
			InputStream raw = context.getResources().openRawResource(rawID);
			xml.setInput(raw, Xml.Encoding.UTF_8.toString());
			int eventType = xml.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT)
					Log.e(TAG, "Start Document");
				else if (eventType == XmlPullParser.START_TAG)
					Log.e(TAG, "Tag: " + xml.getName());
				else if (eventType == XmlPullParser.END_TAG)
					Log.e(TAG, "End Tag: " + xml.getName());
				else if (eventType == XmlPullParser.TEXT)
					Log.e(TAG, "Tag: " + xml.getText());
				eventType = xml.next();
			}
			Log.e(TAG, "End Document");
		}
		catch (XmlPullParserException e) { Log.e(TAG, e.toString()); } 
		catch (IOException e) { Log.e(TAG, e.toString()); }
	}
	
	public String getString(int resID) {
		return context.getString(resID);
	}
	
	public String[] getStringArray(int resID) {
		return context.getResources().getStringArray(resID);
	}
	
	public View getView(int layoutID) {
    	return inflater.inflate(layoutID, null);
	}
	
	public View getSubView(View parent, int resId) {
		return parent.findViewById(resId);
	}

}

package wb.android.flex;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.util.Log;

class FlexStrings {

	private static final String TAG = "FlexStrings";
	@SuppressWarnings("unused")
	private static final boolean D = Flex.D;
	
	private boolean open;
	private String nameHolder;
	private HashMap<String, String> stringMap;
	
	private static final String ELEMENT = "string";
	private static final String ATTRIBUTE_NAME = "name";
	
	FlexStrings() {
		this.open = false;
		this.stringMap = new HashMap<String, String>();
	}
	
	void startElement(Attributes attributes, String localName) throws SAXException {
		if (open) throw new SAXException("You cannot have nested string elements");
		if (ELEMENT.equalsIgnoreCase(localName)) {
			String name = attributes.getValue(ATTRIBUTE_NAME);
			if (name == null) throw new SAXException("string elements must have a \"name\" attribute.");
			this.nameHolder = name;
			open = true;
		}
		else
			throw new SAXException("Undefined FlexStrings element: " + localName);
	}
	
	void endElement(String localName, String value) throws SAXException {
		if (!open) throw new SAXException("You cannot have nested string elements");
		if (!stringMap.containsKey(this.nameHolder))
			stringMap.put(this.nameHolder, value);
		else
			throw new SAXException("strings cannot have the same name: " + this.nameHolder);
		open = false;
	}
	
	void endElement(String localName) throws SAXException {
		if (!open) throw new SAXException("You cannot have nested string elements");
		open = false;
		Log.e(TAG, "Error: Failed to parse the string value for: " + this.nameHolder);
	}
	
	String update(String defaultValue, String name) {
		if (stringMap.containsKey(name))
			return stringMap.get(name);
		else
			return defaultValue;
	}
}

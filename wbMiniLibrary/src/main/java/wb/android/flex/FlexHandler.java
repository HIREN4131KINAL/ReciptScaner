package wb.android.flex;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;

import wb.android.flex.Flex.Element;

class FlexHandler extends DefaultHandler {
	
	private static final String TAG = "FlexHandler";
	private static final boolean D = Flex.D;
	
	private final Stack<Flex.Element> elements;
	private final Flex flex;
	private final FlexViews flexViews;
	private final FlexStrings flexStrings;
	private StringBuffer buffer;
	
	public FlexHandler(Flex flex) {
		this.flex = flex;
		elements = new Stack<Flex.Element>();
		flexViews = new FlexViews();
		flexStrings = new FlexStrings();
	}
	
	@Override
	public void startDocument() throws SAXException {
		if (D) Log.d(TAG, "Start Document");
	}
	
	@Override
	public void endDocument() throws SAXException {
		if (D) Log.d(TAG, "End Document");
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (D) Log.d(TAG, "StartElement: " + localName);
		if (!elements.isEmpty()) {
			Flex.Element element = elements.peek();
			if (element == Flex.Element.FLEX_VIEWS) {
				flexViews.startElement(attributes, localName);
				return;
			}
			else if (element == Flex.Element.FLEX_STRINGS) {
				buffer = new StringBuffer();
				flexStrings.startElement(attributes, localName);
				return;
			}
			else if (Flex.Element.FLEX.isTag(localName))
				throw new SAXException("The " + Flex.Element.FLEX + " element can only appear as a root element.");
		}
		if (Flex.Element.FLEX.isTag(localName)) {
			elements.push(Element.FLEX);
			//Parse Root Attributes 
		}
		else if (Flex.Element.FLEX_VIEWS.isTag(localName)) {
			elements.push(Flex.Element.FLEX_VIEWS);
			//Parse FlexViews Attributes (Currently none)
		}
		else if (Flex.Element.FLEX_STRINGS.isTag(localName)) {
			elements.push(Flex.Element.FLEX_STRINGS);
			//Parse FlexStrings Attributes (Currently none)
		}
		else {
			throw new SAXException("Undefined Flex Element: " + localName);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (D) Log.d(TAG, "EndElement: " + localName);
		if (Flex.Element.FLEX.isTag(localName)) {
			if (elements.pop() != Flex.Element.FLEX) throw new SAXException("The end XML tag for " + localName + " did not match the expected tag: " + Flex.Element.FLEX.tagName());
			flex.setFlexStrings(flexStrings);
			flex.setFlexViews(flexViews);
		}
		else if (Flex.Element.FLEX_VIEWS.isTag(localName)) {
			if (elements.pop() != Flex.Element.FLEX_VIEWS) throw new SAXException("The end XML tag for " + localName + " did not match the expected tag: " + Flex.Element.FLEX_VIEWS.tagName());
		}
		else if (Flex.Element.FLEX_STRINGS.isTag(localName)) {
			if (elements.pop() != Flex.Element.FLEX_STRINGS) throw new SAXException("The end XML tag for " + localName + " did not match the expected tag: " + Flex.Element.FLEX_STRINGS.tagName());
		}
		else {
			if (!elements.isEmpty()) {
				Flex.Element element = elements.peek();
				if (element == Flex.Element.FLEX_VIEWS) {
					flexViews.endElement(localName);
					return;
				}
				else if (element == Flex.Element.FLEX_STRINGS) {
					if (buffer != null)
						flexStrings.endElement(localName, buffer.toString());
					else
						flexStrings.endElement(localName);
					return;
				}
			}
			throw new SAXException("Undefined Flex Tag: " + localName);
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String s = new String(ch, start, length);
		if (buffer != null) 
			buffer.append(s);
	}

}
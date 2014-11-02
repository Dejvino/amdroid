package com.sound.ampache.net;

import com.sound.ampache.objects.Tag;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Created by dejvino on 2.11.14.
 */
/*package-private*/ class AmpacheTagParser extends AmpacheDataHandler
{
	private Tag current;

	public void startElement(String namespaceURI,
	                         String localName,
	                         String qName,
	                         Attributes attr) throws SAXException
	{

		super.startElement(namespaceURI, localName, qName, attr);

		if (localName.equals("tag")) {
			current = new Tag();
			current.id = attr.getValue("id");
		}
	}

	public void endElement(String namespaceURI,
	                       String localName,
	                       String qName) throws SAXException
	{

		super.endElement(namespaceURI, localName, qName);

		if (localName.equals("name")) {
			current.name = contents.toString();
		}
		if (localName.equals("albums")) {
			current.albums = contents.toString();
		}
		if (localName.equals("artists")) {
			current.artists = contents.toString();
		}
		if (localName.equals("tag")) {
			data.add(current);
		}
	}
}

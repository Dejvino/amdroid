package com.sound.ampache.net;

import com.sound.ampache.objects.Artist;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Created by dejvino on 2.11.14.
 */
/*package-private*/ class AmpacheArtistParser extends AmpacheDataHandler
{
	private Artist current;

	public void startElement(String namespaceURI,
	                         String localName,
	                         String qName,
	                         Attributes attr) throws SAXException
	{

		super.startElement(namespaceURI, localName, qName, attr);

		if (localName.equals("artist")) {
			current = new Artist();
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
			current.albums = contents.toString() + " albums";
		}

		if (localName.equals("artist")) {
			data.add(current);
		}

	}
}

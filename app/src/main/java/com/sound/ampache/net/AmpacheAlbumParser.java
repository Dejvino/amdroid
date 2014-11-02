package com.sound.ampache.net;

import com.sound.ampache.objects.Album;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Created by dejvino on 2.11.14.
 */
/*package-private*/ class AmpacheAlbumParser extends AmpacheDataHandler
{
	private Album current;

	public void startElement(String namespaceURI,
	                         String localName,
	                         String qName,
	                         Attributes attr) throws SAXException
	{

		super.startElement(namespaceURI, localName, qName, attr);

		if (localName.equals("album")) {
			current = new Album();
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

		if (localName.equals("artist")) {
			current.artist = contents.toString();
		}

		if (localName.equals("tracks")) {
			current.tracks = contents.toString();
		}

		if (localName.equals("disk")) {
			current.disk = contents.toString();
		}

		if (localName.equals("year")) {
			current.year = contents.toString();
		}

		if (localName.equals("album")) {
			data.add(current);
		}
	}
}

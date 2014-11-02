package com.sound.ampache.net;

import com.sound.ampache.objects.Playlist;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
* Created by dejvino on 2.11.14.
*/
/*package-private*/ class AmpachePlaylistParser extends AmpacheDataHandler
{
	private Playlist current;

	public void startElement(String namespaceURI,
	                         String localName,
	                         String qName,
	                         Attributes attr) throws SAXException
	{

		super.startElement(namespaceURI, localName, qName, attr);

		if (localName.equals("playlist")) {
			current = new Playlist();
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

		if (localName.equals("owner")) {
			current.owner = contents.toString();
		}

		if (localName.equals("items")) {
			current.count = contents.toString();
		}

		if (localName.equals("playlist")) {
			data.add(current);
		}
	}
}

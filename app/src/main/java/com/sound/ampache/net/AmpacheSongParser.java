package com.sound.ampache.net;

import com.sound.ampache.objects.Song;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
* Created by dejvino on 2.11.14.
*/
/*package-private*/ class AmpacheSongParser extends AmpacheDataHandler
{
	private Song current;

	public void startElement(String namespaceURI,
	                         String localName,
	                         String qName,
	                         Attributes attr) throws SAXException
	{

		super.startElement(namespaceURI, localName, qName, attr);

		if (localName.equals("song")) {
			current = new Song();
			current.id = attr.getValue("id");
		}
	}

	public void endElement(String namespaceURI,
	                       String localName,
	                       String qName) throws SAXException
	{

		super.endElement(namespaceURI, localName, qName);

		if (localName.equals("song")) {
			data.add(current);
		}

		if (localName.equals("title")) {
			current.name = contents.toString();
		}

		if (localName.equals("artist")) {
			current.artist = contents.toString();
		}

		if (localName.equals("art")) {
			current.art = contents.toString();
		}

		if (localName.equals("url")) {
			current.url = contents.toString();
		}

		if (localName.equals("album")) {
			current.album = contents.toString();
		}

		if (localName.equals("genre")) {
			current.genre = contents.toString();
		}

		if (localName.equals("size")) {
			current.size = contents.toString();
		}

		if (localName.equals("time")) {
			current.time = contents.toString();
		}
	}
}

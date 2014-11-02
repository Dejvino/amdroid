package com.sound.ampache.net;

import com.sound.ampache.objects.Video;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Created by dejvino on 2.11.14.
 */
/*package-private*/ class AmpacheVideoParser extends AmpacheDataHandler
{
	private Video current;

	public void startElement(String namespaceURI,
	                         String localName,
	                         String qName,
	                         Attributes attr) throws SAXException
	{

		super.startElement(namespaceURI, localName, qName, attr);

		if (localName.equals("video")) {
			current = new Video();
			current.id = attr.getValue("id");
		}
	}

	public void endElement(String namespaceURI,
	                       String localName,
	                       String qName) throws SAXException
	{

		super.endElement(namespaceURI, localName, qName);

		if (localName.equals("video")) {
			data.add(current);
		}

		if (localName.equals("title")) {
			current.name = contents.toString();
		}

		if (localName.equals("mime")) {
			current.mime = contents.toString();
		}

		if (localName.equals("resolution")) {
			current.resolution = contents.toString();
		}

		if (localName.equals("url")) {
			current.url = contents.toString();
		}

		if (localName.equals("genre")) {
			current.genre = contents.toString();
		}

		if (localName.equals("size")) {
			current.size = contents.toString();
		}
	}
}

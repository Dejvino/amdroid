package com.sound.ampache.net;

import org.xml.sax.SAXException;

/**
 * Created by dejvino on 2.11.14.
 */
/*package-private*/ class AmpacheAuthParser extends AmpacheDataHandler
{
	public String token = "";
	public int artists = 0;
	public int albums = 0;
	public int songs = 0;
	public String update = "";

	public void endElement(String namespaceURI,
	                       String localName,
	                       String qName) throws SAXException
	{

		super.endElement(namespaceURI, localName, qName);

		if (localName.equals("auth")) {
			token = contents.toString();
		}

		if (localName.equals("artists")) {
			artists = Integer.parseInt(contents.toString());
		}
		if (localName.equals("albums")) {
			albums = Integer.parseInt(contents.toString());
		}
		if (localName.equals("songs")) {
			songs = Integer.parseInt(contents.toString());
		}

		if (localName.equals("add")) {
			update = contents.toString();
		}
	}
}

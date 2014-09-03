package com.sound.ampache.net;

/* Copyright (c) 2008 Kevin James Purdy <purdyk@onid.orst.edu>
 * Copyright (c) 2010 Jacob Alexander   < haata@users.sf.net >
 *
 * +------------------------------------------------------------------------+
 * | This program is free software; you can redistribute it and/or          |
 * | modify it under the terms of the GNU General Public License            |
 * | as published by the Free Software Foundation; either version 2         |
 * | of the License, or (at your option) any later version.                 |
 * |                                                                        |
 * | This program is distributed in the hope that it will be useful,        |
 * | but WITHOUT ANY WARRANTY; without even the implied warranty of         |
 * | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          |
 * | GNU General Public License for more details.                           |
 * |                                                                        |
 * | You should have received a copy of the GNU General Public License      |
 * | along with this program; if not, write to the Free Software            |
 * | Foundation, Inc., 59 Temple Place - Suite 330,                         |
 * | Boston, MA  02111-1307, USA.                                           |
 * +------------------------------------------------------------------------+
 */

import java.net.*;
import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.ArrayList;
import com.sound.ampache.objects.*;

import android.content.SharedPreferences;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.Integer;
import java.lang.Long;
import java.security.MessageDigest;
import java.util.Date;

public class ampacheCommunicator
{
	public static String LOG_TAG = "Ampache_Amdroid_Comm";
    public String authToken = "";
    public int artists;
    public int albums;
    public int songs;
    private String update;
    private Context mCtxt;
    public String lastErr;

    private XMLReader reader;

    private SharedPreferences prefs;

    public ampacheCommunicator(SharedPreferences preferences, Context context) throws Exception {
        prefs = preferences;
        mCtxt = context;
        System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
        reader = XMLReaderFactory.createXMLReader();
    }

    public void ping() {
        dataHandler hand = new dataHandler();
        reader.setContentHandler(hand);
        try {
            reader.parse(new InputSource(fetchFromServer("auth=" + this.authToken)));
            if (hand.errorCode == 401) {
                this.perform_auth_request();
            }
        } catch (Exception e) {
			Log.e(LOG_TAG, "Operation PING failed: " + e.getMessage(), e);
        }
    }

    public void perform_auth_request() throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        /* Get the current time, and convert it to a string */
        String time = Long.toString((new Date()).getTime() / 1000);
        
        /* build our passphrase hash */
        md.reset();
        
        /* first hash the password */
        String pwHash = prefs.getString("server_password_preference", "");
        md.update(pwHash.getBytes(), 0, pwHash.length());
        String preHash = time + asHex(md.digest());
        
        /* then hash the timestamp in */
        md.reset();
        md.update(preHash.getBytes(), 0, preHash.length());
        String hash = asHex(md.digest());
        
        /* request server auth */
        ampacheAuthParser hand = new ampacheAuthParser();
        reader.setContentHandler(hand);
        String user = prefs.getString("server_username_preference", "");
        try {
            reader.parse(new InputSource(fetchFromServer("action=handshake&auth="+hash+"&timestamp="+time+"&version=350001&user="+user)));
        } catch (Exception e) {
			Log.e(LOG_TAG, "Operation AUTH failed: " + e.getMessage(), e);
            lastErr = "Could not connect to server";
        }

        if (hand.errorCode != 0) {
            lastErr = hand.error;
        }

        authToken = hand.token;
        artists = hand.artists;
        albums = hand.albums;
        songs = hand.songs;
        update = hand.update;
    }
   
    public InputStream fetchFromServer(String append) throws Exception {
        URL fullUrl = new URL(prefs.getString("server_url_preference", "") + "/server/xml.server.php?" + append);
        return fullUrl.openStream();
    }

    public interface ampacheDataReceiver
    {
        public void receiveObjects(ArrayList data);
    }
    
    public class ampacheRequestHandler extends Thread
    {
        private ampacheDataReceiver recv = null;
        private dataHandler hand;
        private Context mCtx;
        
        private String type;
        private String filter;
        
        public Handler incomingRequestHandler;
        public Boolean stop = false;
        
        public void run() {
            Looper.prepare();
            
            incomingRequestHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        Directive directiveObj = (Directive) msg.obj;
						String[] directive = directiveObj.args;
                        String append = "";
                        boolean goodcache = false;
                        String error = null;
                        Message reply = this.obtainMessage();
                        ArrayList<ampacheObject> goods = null;
                        InputSource dataIn = null;
                        
                        append = "action=" + directive[0];
                        
                        if (directive[0].equals("artists")) {
                            hand = new ampacheArtistParser();
                            append += "&filter=" + directive[1];
                        } else if (directive[0].equals("artist_albums")) {
                            append += "&filter=" + directive[1];
                            hand = new ampacheAlbumParser();
                        } else if (directive[0].equals("artist_songs")) {
                            append += "&filter=" + directive[1];
                            hand = new ampacheSongParser();
                        } else if (directive[0].equals("album_songs")) {
                            append += "&filter=" + directive[1];
                            hand = new ampacheSongParser();        
                        } else if (directive[0].equals("playlist_songs")) {
                            append += "&filter=" + directive[1];
                            hand = new ampacheSongParser();
                        } else if (directive[0].equals("tag_artists")) {
                            append += "&filter=" + directive[1];
                            hand = new ampacheArtistParser();
                        } else if (directive[0].equals("albums")) {
                            hand = new ampacheAlbumParser();
                            append += "&filter=" + directive[1];
                        } else if (directive[0].equals("playlists")) {
                            hand = new ampachePlaylistParser();
                            append += "&filter=" + directive[1];
                        } else if (directive[0].equals("songs")) {
                            hand = new ampacheSongParser();
                            append += "&filter=" + directive[1];
                        } else if (directive[0].equals("tags")) {
                            hand = new ampacheTagParser();
                            append += "&filter=" + directive[1];
                        } else if (directive[0].equals("videos")) {
                            hand = new ampacheVideoParser();
                        } else if (directive[0].equals("search_songs")) {
                            hand = new ampacheSongParser();
                            append += "&filter=" + directive[1];
                        } else {
                            return; // new ArrayList();
                        }
                        
                        if (msg.what == 0x1336 || msg.what == 0x1337) {
                            append += "&offset=" + msg.arg1 + "&limit=100";
                            reply.arg1 = msg.arg1;
                            reply.arg2 = msg.arg2;
                        }

                        append += "&auth=" + authToken;

                        if (stop == true) {
                            stop = false;
                            return;
                        }

                        /* now we fetch */
                        try {
                            URL theUrl = new URL(prefs.getString("server_url_preference", "") + "/server/xml.server.php?" + append);
                            dataIn = new InputSource(theUrl.openStream());
                        } catch (Exception e) {
							Log.e(LOG_TAG, "Fetching #904 failed: " + e.getMessage(), e);
                            error = e.toString();
                        }

                        if (stop == true) {
                            stop = false;
                            return;
                        }

                        /* all done loading data, now to parse */
                        reader.setContentHandler(hand);
                        try {
                            reader.parse(dataIn);
                        } catch (Exception e) {
							Log.e(LOG_TAG, "Parsing #995 failed: " + e.getMessage(), e);
                            error = e.toString();
                        }
                        
                        if (hand.error != null) {
                            if (hand.errorCode == 401) {
                                try {
                                    ampacheCommunicator.this.perform_auth_request();
                                    this.sendMessage(msg);
                                } catch (Exception e) {
									Log.e(LOG_TAG, "Operation AUTH #953 failed: " + e.getMessage(), e);
                                }
                                return;
                            }
                            error = hand.error;
                        }

                        if (stop == true) {
                            stop = false;
                            return;
                        }

                        if (error == null) {
                            reply.what = msg.what;
                            reply.obj = hand.data;
                        } else {
                            reply.what = 0x1338;
                            reply.obj = error;
                        }
                        try {
                            msg.replyTo.send(reply);
                        } catch (Exception e) {
							Log.e(LOG_TAG, "Operation REPLY #958 failed: " + e.getMessage(), e);
                            //well shit, that sucks doesn't it
                        }
                    }
                };
            Looper.loop();
        }
    }     
    
    private class dataHandler extends DefaultHandler {
        public ArrayList<ampacheObject> data = new ArrayList();
        public String error = null;
        public int errorCode = 0;
        protected CharArrayWriter contents = new CharArrayWriter();

        public void startDocument() throws SAXException {
            
        }
        
        public void endDocument() throws SAXException {

        }

        public void characters( char[] ch, int start, int length )throws SAXException {
            contents.write( ch, start, length );
        }

        public void startElement( String namespaceURI,
                                  String localName,
                                  String qName,
                                  Attributes attr) throws SAXException {

            if (localName.equals("error"))
                errorCode = Integer.parseInt(attr.getValue("code"));
            contents.reset();
        }

        public void endElement( String namespaceURI,
                                String localName,
                                String qName) throws SAXException {
            if (localName.equals("error")) {
                error = contents.toString();
            }
        }
        
    }

    private class ampacheAuthParser extends dataHandler {
        public String token = "";
        public int artists = 0;
        public int albums = 0;
        public int songs = 0;
        public String update = "";

        public void endElement( String namespaceURI,
                                String localName,
                                String qName) throws SAXException {

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
    
    private class ampacheArtistParser extends dataHandler {
        private Artist current;
        
        public void startElement( String namespaceURI,
                                  String localName,
                                  String qName,
                                  Attributes attr) throws SAXException {
            
            super.startElement(namespaceURI, localName, qName, attr);

            if (localName.equals("artist")) {
                current = new Artist();
                current.id = attr.getValue("id");
            }
        }
        
        public void endElement( String namespaceURI,
                                String localName,
                                String qName) throws SAXException {
            
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
    
    private class ampacheAlbumParser extends dataHandler {
        private Album current;
        
        public void startElement( String namespaceURI,
                                  String localName,
                                  String qName,
                                  Attributes attr) throws SAXException {
            
            super.startElement(namespaceURI, localName, qName, attr);

            if (localName.equals("album")) {
                current = new Album();
                current.id = attr.getValue("id");
            }
        }
        
        public void endElement( String namespaceURI,
                                String localName,
                                String qName) throws SAXException {
            
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
    
    private class ampacheTagParser extends dataHandler {
        private Tag current;
        
        public void startElement( String namespaceURI,
                                  String localName,
                                  String qName,
                                  Attributes attr) throws SAXException {
            
            super.startElement(namespaceURI, localName, qName, attr);

            if (localName.equals("tag")) {
                current = new Tag();
                current.id = attr.getValue("id");
            }
        }
        
        public void endElement( String namespaceURI,
                                String localName,
                                String qName) throws SAXException {
            
            super.endElement(namespaceURI, localName, qName);

            if (localName.equals("name")) {
                current.name = contents.toString();
            }
	    if (localName.equals("albums")) {
		current.albums = contents.toString();
	    }
	    if (localName.equals("artists")){
		current.artists = contents.toString();
	    }
            if (localName.equals("tag")) {
                data.add(current);
            }
        }
    }
    
    private class ampachePlaylistParser extends dataHandler {
        private Playlist current;
        
        public void startElement( String namespaceURI,
                                  String localName,
                                  String qName,
                                  Attributes attr) throws SAXException {
            
            super.startElement(namespaceURI, localName, qName, attr);

            if (localName.equals("playlist")) {
                current = new Playlist();
                current.id = attr.getValue("id");
            }
        }
        
        public void endElement( String namespaceURI,
                                String localName,
                                String qName) throws SAXException {
            
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
    
    private class ampacheSongParser extends dataHandler {
        private Song current;
        
        public void startElement( String namespaceURI,
                                  String localName,
                                  String qName,
                                  Attributes attr) throws SAXException {
            
            super.startElement(namespaceURI, localName, qName, attr);

            if (localName.equals("song")) {
                current = new Song();
                current.id = attr.getValue("id");
            }
        }
        
        public void endElement( String namespaceURI,
                                String localName,
                                String qName) throws SAXException {
            
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
 
    private class ampacheVideoParser extends dataHandler {
        private Video current;

        public void startElement( String namespaceURI,
                                  String localName,
                                  String qName,
                                  Attributes attr) throws SAXException {

            super.startElement(namespaceURI, localName, qName, attr);

            if (localName.equals("video")) {
                current = new Video();
                current.id = attr.getValue("id");
            }
        }

        public void endElement( String namespaceURI,
                                String localName,
                                String qName) throws SAXException {

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


    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    public static String asHex(byte[] buf)
    {
        char[] chars = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i)
            {
                chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
                chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
            }
        return new String(chars);
    }
}

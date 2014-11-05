package com.sound.ampache.net;

/* Copyright (c) 2008 Kevin James Purdy <purdyk@onid.orst.edu>
 * Copyright (c) 2010 Jacob Alexander   < haata@users.sf.net >
 * Copyright (c) 2014 David Hrdina Nemecek <dejvino@gmail.com>
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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.sound.ampache.amdroid;
import com.sound.ampache.objects.Directive;
import com.sound.ampache.objects.ampacheObject;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;

/**
 * Communicator responsible for making network calls to the Ampache XML API interface.
 *
 * This class is NOT thread safe. Only a single thread should access this class
 * (preferably from a non-UI thread, i.e. {@link com.sound.ampache.net.NetworkWorker}).
 */
public class AmpacheApiClient
{
	public static String LOG_TAG = "Ampache_Amdroid_Comm";

	private String authToken = "";
	public int artists;
	public int albums;
	public int songs;
	private String update;
	private Context mCtxt;
	public String lastErr;

	private XMLReader reader;

	private SharedPreferences prefs;

	public AmpacheApiClient(SharedPreferences preferences, Context context) throws Exception
	{
		prefs = preferences;
		mCtxt = context;
		System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
		reader = XMLReaderFactory.createXMLReader();
	}

	/**
	 * Performs a PING command to the server to check if a session is still valid and to extend its
	 * lifetime if it is.
	 *
	 * After calling this method, consider checking the output of {@link #isAuthenticated()}.
	 */
	public void ping()
	{
		AmpacheDataHandler hand = new AmpacheDataHandler();
		reader.setContentHandler(hand);
		try {
			reader.parse(new InputSource(fetchFromServer("auth=" + this.authToken)));
			if (hand.errorCode == 401) {
				this.perform_auth_request();
			}
		} catch (MalformedURLException e) {
			Log.e(LOG_TAG, "Operation PING failed due to invalid URL: " + e.getMessage(), e);
			amdroid.logger.logCritical("Server PING failed", e.getLocalizedMessage());
		} catch (Exception e) {
			Log.e(LOG_TAG, "Operation PING failed: " + e.getMessage(), e);
			amdroid.logger.logCritical("Server PING failed", "Error details: " + e.toString());
		}
	}

	/**
	 * Performs an authentication request, creating a new session.
	 *
	 * After calling this method, consider checking the output of {@link #isAuthenticated()}.
	 *
	 * @throws Exception
	 */
	public void perform_auth_request() throws Exception
	{
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
		AmpacheAuthParser hand = new AmpacheAuthParser();
		reader.setContentHandler(hand);
		String user = prefs.getString("server_username_preference", "");
		try {
			reader.parse(new InputSource(fetchFromServer("action=handshake&auth=" + hash + "&timestamp=" + time + "&version=350001&user=" + user)));
		} catch (Exception e) {
			Log.e(LOG_TAG, "Operation AUTH failed: " + e.getMessage(), e);
			amdroid.logger.logCritical("Server AUTH failed", "Error Details: " + e.toString());
			lastErr = "Could not connect to server";
		}

		if (hand.errorCode != 0) {
			lastErr = hand.error;
			amdroid.logger.logCritical("Server AUTH failed", "Error: " + lastErr);
		} else {
			amdroid.logger.logInfo("Server AUTH successful");
		}

		authToken = hand.token;
		artists = hand.artists;
		albums = hand.albums;
		songs = hand.songs;
		update = hand.update;
	}

	/**
	 * @return true when we have an AUTH token, false otherwise.
	 */
	public boolean isAuthenticated()
	{
		return authToken != null && !authToken.equals("");
	}

	/**
	 * @return Session identifier that is used when communicating with the server.
	 */
	public String getAuthToken()
	{
		return authToken;
	}

	/**
	 * A generic method for performing the API calls.
	 *
	 * @param append Query part of the URL.
	 * @return Input stream for reading the result.
	 * @throws Exception
	 */
	private InputStream fetchFromServer(String append) throws Exception
	{
		URL fullUrl = new URL(prefs.getString("server_url_preference", "") + "/server/xml.server.php?" + append);
		return fullUrl.openStream();
	}

	public class ampacheRequestHandler extends Thread
	{
		private AmpacheDataReceiver recv = null;
		private AmpacheDataHandler hand;
		private Context mCtx;

		private String type;
		private String filter;

		public Handler incomingRequestHandler;
		public Boolean stop = false;

		public void run()
		{
			Looper.prepare();

			incomingRequestHandler = new Handler()
			{
				public void handleMessage(Message msg)
				{
					Directive directive = (Directive) msg.obj;

					if (directive == null || directive.action == null) {
						Log.e(LOG_TAG, "Cannot handle API request message, null directive or action.");
						amdroid.logger.logWarning("Cannot handle API request message, null directive or action.");
						return;
					}
					String error = null;
					Message reply = this.obtainMessage();
					InputSource dataIn = null;

					String append = "action=" + directive.action.getKey();

					switch (directive.action) {
						case ARTISTS:
							hand = new AmpacheArtistParser();
							append += "&filter=" + directive.getFilterForUrl();
							break;
						case ARTIST_ALBUMS:
							hand = new AmpacheAlbumParser();
							append += "&filter=" + directive.getFilterForUrl();
							break;
						case ARTIST_SONGS:
							hand = new AmpacheSongParser();
							append += "&filter=" + directive.getFilterForUrl();
							break;
						case ALBUM_SONGS:
							hand = new AmpacheSongParser();
							append += "&filter=" + directive.getFilterForUrl();
							break;
						case PLAYLIST_SONGS:
							hand = new AmpacheSongParser();
							append += "&filter=" + directive.getFilterForUrl();
							break;
						case TAG_ARTISTS:
							hand = new AmpacheArtistParser();
							append += "&filter=" + directive.getFilterForUrl();
							break;
						case TAG_SONGS:
							hand = new AmpacheSongParser();
							append += "&filter=" + directive.getFilterForUrl();
							break;
						case ALBUMS:
							hand = new AmpacheAlbumParser();
							append += "&filter=" + directive.getFilterForUrl();
							break;
						case PLAYLISTS:
							hand = new AmpachePlaylistParser();
							append += "&filter=" + directive.getFilterForUrl();
							break;
						case SONGS:
							hand = new AmpacheSongParser();
							append += "&filter=" + directive.getFilterForUrl();
							break;
						case TAGS:
							hand = new AmpacheTagParser();
							append += "&filter=" + directive.getFilterForUrl();
							break;
						case VIDEOS:
							hand = new AmpacheVideoParser();
							break;
						case SEARCH_SONGS:
							hand = new AmpacheSongParser();
							append += "&filter=" + directive.getFilterForUrl();
							break;
						case STATS:
							hand = new AmpacheAlbumParser();
							append += "&type=" + directive.getFilterForUrl();
							break;
						default:
							throw new RuntimeException("Unhandled API action: " + directive.action);
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
					String urlText = prefs.getString("server_url_preference", "") + "/server/xml.server.php?" + append;
					try {
						URL url = new URL(urlText);
						dataIn = new InputSource(url.openStream());
					} catch (MalformedURLException e) {
						Log.e(LOG_TAG, "Fetching #904 failed: " + e.getMessage(), e);
						error = e.toString();
						amdroid.logger.logCritical("Failed preparing server request, malformed URL",
								"URL used: " + urlText + "\n" + "Error details: " + error);
					} catch (Exception e) {
						Log.e(LOG_TAG, "Fetching #904 failed: " + e.getMessage(), e);
						error = e.toString();
						amdroid.logger.logCritical("Failed preparing server request", "Error details: " + error);
					}

					if (stop == true) {
						stop = false;
						return;
					}

					Log.d(LOG_TAG, "Server request URL: " + urlText);

                        /* all done loading data, now to parse */
					reader.setContentHandler(hand);
					try {
						reader.parse(dataIn);
					} catch (Exception e) {
						Log.e(LOG_TAG, "Parsing #995 failed: " + e.getMessage(), e);
						error = e.toString();
						amdroid.logger.logCritical("Failed parsing server response", "URL used: " + urlText + "\n"
								+ "Error details: " + error);
					}

					if (hand.error != null) {
						if (hand.errorCode == 401) {
							try {
								AmpacheApiClient.this.perform_auth_request();
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


	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

	public static String asHex(byte[] buf)
	{
		char[] chars = new char[2 * buf.length];
		for (int i = 0; i < buf.length; ++i) {
			chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
			chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
		}
		return new String(chars);
	}
}

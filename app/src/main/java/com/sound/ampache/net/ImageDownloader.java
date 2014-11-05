package com.sound.ampache.net;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.sound.ampache.amdroid;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Utility class for downloading image file as a Bitmap object.
 */
public class ImageDownloader
{
	private static final String LOG_TAG = "Ampache_ImageDownloader";

	public static Bitmap getImageBitmap(String url) {
		Bitmap bm = null;
		try {
			URL aURL = new URL(url);
			URLConnection conn = aURL.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			bm = BitmapFactory.decodeStream(bis);
			bis.close();
			is.close();
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error getting bitmap from URL: " + url, e);
			amdroid.logger.logWarning("Could not download image", "URL: " + url + "\nDetails: " + e.getLocalizedMessage());
		}
		return bm;
	}
}

package com.sound.ampache.utility;

import android.graphics.Bitmap;

import com.sound.ampache.amdroid;

import java.util.HashMap;
import java.util.Map;

/**
 * Caching manager for working with album art images.
 *
 * Frequently accessed images should not be re-downloaded every time, we should just cache them
 * and save bandwidth and time in doing so.
 */
public class AlbumArtCache
{
	private static final int CACHE_LIMIT = 100;

	private Map<String, Bitmap> cache = new HashMap<String, Bitmap>();

	/**
	 * Performs a cache query for an image from the given URL.
	 * If there is a cache-hit, the image is loaded locally. Otherwise a request to the network
	 * worker is sent and the image is downloaded.
	 *
	 * @param url URL of the image.
	 * @param resultCallback Callback function to invoke once the result is available.
	 */
	public void requestBitmap(final String url, final ResultCallback<Bitmap> resultCallback)
	{
		// TODO: better cache!
		// currently we have a simple in-memory cache, using a local DB might be better
		/*if (cache.containsKey(url)) {
			resultCallback.onResultCallback(cache.get(url));
			return;
		}*/
		amdroid.networkClient.downloadImage(url, new ResultCallback<Bitmap>()
		{
			@Override
			public void onResultCallback(Bitmap result)
			{
				if (cache.size() > CACHE_LIMIT) {
					String victim = cache.keySet().iterator().next();
					cache.remove(victim);
				}
				cache.put(url, result);
				resultCallback.onResultCallback(result);
			}
		});
	}
}

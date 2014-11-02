package com.sound.ampache.net;

/**
 * Created by dejvino on 2.11.14.
 */
public enum AmpacheApiAction
{
	ALBUM_SONGS("Album songs"),
	ALBUMS("Albums"),
	ARTIST_ALBUMS("Artist albums"),
	ARTIST_SONGS("Artist songs"),
	ARTISTS("Artists"),
	PLAYLIST_SONGS("Playlist songs"),
	PLAYLISTS("Playlists"),
	SEARCH_SONGS("Search songs"),
	SONGS("Songs"),
	STATS("Stats"),
	TAG_ALBUMS("Tag albums"),
	TAG_ARTISTS("Tag artists"),
	TAG_SONGS("Tag songs"),
	TAGS("Tags"),
	VIDEOS("Videos");

	private final String name;

	AmpacheApiAction(String name)
	{
		this.name = name;
	}

	public String getKey()
	{
		return name().toLowerCase();
	}

	public String getName()
	{
		return name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}

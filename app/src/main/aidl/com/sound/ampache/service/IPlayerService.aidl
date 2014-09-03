// AIDL bindings for PlayerService
//
// This file lays out all of the Remote Procedure Calls exposed by PlayerService

package com.sound.ampache.service;

// Special types that we're using
import com.sound.ampache.objects.Media;
import com.sound.ampache.service.Messenger;

interface IPlayerService {
	// Player Status
	boolean isPlaying();
	boolean isSeekable();
	int getBuffer();
	int getCurrentPosition();
	int getDuration();

	// Player Controls
	void playMedia( in Media media );
	void playPause();
	void stop();
	void next();
	void prev();
	void seek( in int msec );

	// Playlist Controls
	Media nextItem();
	Media prevItem();

	// Playlist List Modifiers
	Media[] currentPlaylist();
	boolean add( in Media media );
	boolean enqueue( in Media[] media );
	boolean replace( in Media[] media );
	void clearPlaylist();
	
	// Playlist Item
	int getCurrentIndex();
	int getPlaylistSize();
	Media getCurrentMedia();
	Media setCurrentIndex( in int index );
	
	// Shuffle/Repeat
	boolean getShufflePlay();
	boolean getRepeatPlay();
	void setShufflePlay( in boolean randomize );
	void setRepeatPlay( in boolean loop );
	void clearShuffleHistory();

	// Misc
	void closeService();
	void registerMessenger( in Messenger messenger );
	void unregisterMessenger( in Messenger messenger );

	void setAuthToken(in String authToken);
}


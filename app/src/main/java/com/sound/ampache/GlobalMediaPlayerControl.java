package com.sound.ampache;

/* Copyright (c) 2010 Kristopher Heijari < iix.ftw@gmail.com >
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

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.sound.ampache.objects.Media;
import com.sound.ampache.service.PlayerServiceClient;

public class GlobalMediaPlayerControl extends PlayerServiceClient {

	final static String LOG_TAG = "Ampache_Amdroid_GlobalMediaPlayerControl";
	public Boolean prepared = true;

	// Playlist variables
	private PlaylistCurrentListener playlistCurrentListener;
	private PlayingIndexListener playingIndexListener;

	public GlobalMediaPlayerControl()
	{
		amdroid.playListVisible = true;
	}

	public int getCurrentPosition() {
		try {
			return serviceInterface().getCurrentPosition();
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return -1;
		}
	}

	public int getDuration() {
		try {
			return serviceInterface().getDuration();
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return -1;
		}
	}

	public int getBuffer() {
		try {
			return serviceInterface().getBuffer();
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return -1;
		}
	}

	public boolean isPlaying() {
		try {
			return serviceInterface().isPlaying();
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return false;
		}
	}

	public void pause() {
		try {
			if ( isPlaying() )
				serviceInterface().playPause();
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}
	}

	public void seekTo( int pos ) {
		try {
			serviceInterface().seek( pos );
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}
	}

	public void start()
	{
		play();
	}

	public void playMedia( Media media ) {
		try {
			serviceInterface().playMedia( media );
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}
	}

	public void play() {
		// set to show that our mediaplayer has been initialized. 
		amdroid.mediaplayerInitialized = true;

		try {
			playMedia( serviceInterface().getCurrentMedia() );
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}
	}

	public void stop() {
		try {
			serviceInterface().stop();
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}
	}
    
	public void doPauseResume() {
		try {
			serviceInterface().playPause();
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}
	}
   
	public void nextInPlaylist() {
		try {
			serviceInterface().next();
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}
	}

	public void prevInPlaylist() {
		try {
			serviceInterface().prev();
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}
	}
    
	// Functions used to modify playingIndex and notify about changes
	public void setPlayingIndex( int i ) {
		try {
			serviceInterface().setCurrentIndex( i );
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}

		// GUI Trigger
		if ( playingIndexListener != null )
			playingIndexListener.onPlayingIndexChange();
	}
    
	public int getPlayingIndex() {
		try {
			return serviceInterface().getCurrentIndex();
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return -1;
		}
	}

	public int getPlaylistSize() {
		try {
			return serviceInterface().getPlaylistSize();
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return -1;
		}
	}
    
	public void setPlayingIndexListener( PlayingIndexListener listener ) {
		playingIndexListener = listener;
	}

	// Functions used to modify playlistCurrent and notify about changes
	public void setPlaylistCurrentListener( PlaylistCurrentListener listener ) {
		playlistCurrentListener = listener;
	}
	public void addAllPlaylistCurrent( ArrayList<Media> mediaList ) {
		try
		{
			serviceInterface().enqueue( (Media[])mediaList.toArray( new Media[0] ) );
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}

		if ( playlistCurrentListener != null )
			playlistCurrentListener.onPlaylistCurrentChange();
	}
	public void addPlaylistCurrent( Media media ) {
		try {
			serviceInterface().add( media );
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}

		if ( playlistCurrentListener != null )
			playlistCurrentListener.onPlaylistCurrentChange();
	}
	public void clearPlaylistCurrent() {
		try {
			serviceInterface().clearPlaylist();
			serviceInterface().clearShuffleHistory();
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}

		if ( playlistCurrentListener != null )
			playlistCurrentListener.onPlaylistCurrentChange();
	}

	public boolean shuffleEnabled() {
		try {
			return serviceInterface().getShufflePlay();
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return false;
		}
	}

	public boolean repeatEnabled() {
		try {
			return serviceInterface().getRepeatPlay();
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return false;
		}
	}

	public void setShuffle( boolean randomize ) {
		try {
			serviceInterface().setShufflePlay( randomize );
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}
	}

	public void setRepeat( boolean loop ) {
		try {
			serviceInterface().setRepeatPlay( loop );
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}
	}

	/*
	 * The returned object is a copy of the playlist used in the service. 
	 */
	public ArrayList<Media> getPlaylistCurrent(){
		try {
			return new ArrayList( Arrays.asList( serviceInterface().currentPlaylist() ) );
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return null;
		}
	}

	public void setAuthToken(String authToken)
	{
		try {
			serviceInterface().setAuthToken(authToken);
		} catch ( RemoteException ex ) {
			Log.e(LOG_TAG, "DeadObjectException", ex);
		}
	}

	/*
	* Listener Interfaces
	*/
	public interface PlayingIndexListener {
		public void onPlayingIndexChange();
	}

	public interface PlaylistCurrentListener {
		public void onPlaylistCurrentChange();
	}
}


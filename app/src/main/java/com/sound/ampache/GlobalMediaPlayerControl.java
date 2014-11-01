package com.sound.ampache;

/* Copyright (c) 2010 Kristopher Heijari < iix.ftw@gmail.com >
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

import java.util.ArrayList;
import java.util.Arrays;

import android.os.RemoteException;
import android.util.Log;

import com.sound.ampache.objects.Media;
import com.sound.ampache.objects.UserLogEntry;
import com.sound.ampache.service.IPlayerService;
import com.sound.ampache.service.PlayerServiceClient;
import com.sound.ampache.utility.UserLogEntryFactory;

public class GlobalMediaPlayerControl extends PlayerServiceClient {

	private final static String LOG_TAG = "Ampache_Amdroid_GlobalMediaPlayerControl";
	private static final String PLAYER_PREFIX = "[Player] ";

	public Boolean prepared = true;

	// Playlist variables
	private PlaylistCurrentListener playlistCurrentListener;
	private PlayingIndexListener playingIndexListener;

	public GlobalMediaPlayerControl()
	{
		amdroid.playListVisible = true;
	}

	public int getCurrentPosition()
	{
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				return service.getCurrentPosition();
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
		}
		return -1;
	}

	public int getDuration()
	{
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				return service.getDuration();
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
		}
		return -1;
	}

	public int getBuffer()
	{
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				return service.getBuffer();
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
		}
		return -1;
	}

	public boolean isPlaying()
	{
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				return service.isPlaying();
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
		}
		return false;
	}

	public void pause()
	{
		amdroid.logger.logDebug(PLAYER_PREFIX + "Pause");
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				if (service.isPlaying()) {
					service.playPause();
				}
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
		}
	}

	public void seekTo( int pos )
	{
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				service.seek(pos);
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
		}
	}

	public void playMedia( Media media )
	{
		amdroid.logger.log(UserLogEntryFactory.create(UserLogEntry.Severity.DEBUG, PLAYER_PREFIX + "Play media", media));
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				service.playMedia(media);
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
		}
	}

	public void play()
	{
		amdroid.logger.logDebug(PLAYER_PREFIX + "Play");
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				service.playMedia(service.getCurrentMedia());
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
		}
	}

	public void stop()
	{
		amdroid.logger.logDebug(PLAYER_PREFIX + "Stop");
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				service.stop();
			}
		}
		catch ( RemoteException ex ) {
			Log.e(LOG_TAG, "DeadObjectException", ex);
		}
	}
    
	public void doPauseResume()
	{
		amdroid.logger.logDebug(PLAYER_PREFIX + "Pause/Resume");
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				service.playPause();
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
		}
	}
   
	public void nextInPlaylist()
	{
		amdroid.logger.logDebug(PLAYER_PREFIX + "Next");
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				service.next();
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
		}
	}

	public void prevInPlaylist()
	{
		amdroid.logger.logDebug(PLAYER_PREFIX + "Previous");
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				service.prev();
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
		}
	}

	public void setPlayingIndex(int i)
	{
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				service.setCurrentIndex(i);
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}

		// TODO: remove, change to com.sound.ampache.service.PlayerServiceStatusListener
		// GUI Trigger
		if ( playingIndexListener != null )
			playingIndexListener.onPlayingIndexChange();
	}
    
	public int getPlayingIndex()
	{
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				return service.getCurrentIndex();
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
		}
		return -1;
	}

	public int getPlaylistSize()
	{
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				return service.getPlaylistSize();
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
		}
		return -1;
	}
    
	public void setPlayingIndexListener( PlayingIndexListener listener ) {
		playingIndexListener = listener;
	}

	// Functions used to modify playlistCurrent and notify about changes
	public void setPlaylistCurrentListener( PlaylistCurrentListener listener ) {
		playlistCurrentListener = listener;
	}

	public void addAllPlaylistCurrent( ArrayList<Media> mediaList )
	{
		amdroid.logger.logDebug(PLAYER_PREFIX + "Add list of media", mediaList.toString());
		try
		{
			IPlayerService service = serviceInterface();
			if (service != null) {
				service.enqueue((Media[]) mediaList.toArray(new Media[0]));
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}

		if ( playlistCurrentListener != null )
			playlistCurrentListener.onPlaylistCurrentChange();
	}

	public void addPlaylistCurrent( Media media )
	{
		amdroid.logger.log(UserLogEntryFactory.create(UserLogEntry.Severity.DEBUG, PLAYER_PREFIX + "Add media", media));
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				service.add(media);
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}

		if ( playlistCurrentListener != null )
			playlistCurrentListener.onPlaylistCurrentChange();
	}

	public void clearPlaylistCurrent()
	{
		amdroid.logger.logDebug(PLAYER_PREFIX + "Clear playlist");
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				service.clearPlaylist();
				service.clearShuffleHistory();
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return;
		}

		if ( playlistCurrentListener != null )
			playlistCurrentListener.onPlaylistCurrentChange();
	}

	public boolean shuffleEnabled()
	{
		amdroid.logger.logDebug(PLAYER_PREFIX + "Shuffle");
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				return service.getShufflePlay();
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
		}
		return false;
	}

	public boolean repeatEnabled()
	{
		amdroid.logger.logDebug(PLAYER_PREFIX + "Repeat");
		try {
			return serviceInterface().getRepeatPlay();
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
			return false;
		}
	}

	public void setShuffle( boolean randomize )
	{
		amdroid.logger.logDebug(PLAYER_PREFIX + "Shuffle " + randomize);
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				service.setShufflePlay(randomize);
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
		}
	}

	public void setRepeat( boolean loop )
	{
		amdroid.logger.logDebug(PLAYER_PREFIX + "Repeat " + loop);
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				service.setRepeatPlay(loop);
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
		}
	}

	/*
	 * The returned object is a copy of the playlist used in the service. 
	 */
	public ArrayList<Media> getPlaylistCurrent()
	{
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				if (service.currentPlaylist() != null) {
					return new ArrayList<Media>(Arrays.asList(service.currentPlaylist()));
				}
			}
		}
		catch ( RemoteException ex ) {
			Log.e( LOG_TAG, "DeadObjectException",ex );
		}
		return new ArrayList<Media>();
	}

	public void setAuthToken(String authToken)
	{
		amdroid.logger.logDebug(PLAYER_PREFIX + "Setting AUTH token", "New token: " + authToken);
		try {
			IPlayerService service = serviceInterface();
			if (service != null) {
				service.setAuthToken(authToken);
			}
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


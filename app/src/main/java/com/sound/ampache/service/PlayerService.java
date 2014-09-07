package com.sound.ampache.service;

/* Copyright (c) 2010 Jacob Alexander   < haata@users.sf.net >
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

import java.util.Arrays;
import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.sound.ampache.MainActivity;
import com.sound.ampache.PlaylistFragment;
import com.sound.ampache.objects.*;
import com.sound.ampache.utility.Player;
import com.sound.ampache.utility.Playlist;

public class PlayerService extends Service
{
	private static final String LOG_TAG = "Ampache_Amdroid_PlayerService";

	// Basic service components
	private Player mediaPlayer;
	private Playlist playlist;

	private PlayerInterfaceListener listener;

	// Clients to send messages to
	private ArrayList<Messenger> clients = new ArrayList<Messenger>();


	// **********************************************************************
	// Client to Service Requests *******************************************
	// **********************************************************************

	@Override
	public IBinder onBind( Intent intent ) {
		Log.d( LOG_TAG, "onBind" );

		// Make sure it's a valid request TODO
		if ( IPlayerService.class.getName().equals( intent.getAction() ) )
			return mBinder;

		return mBinder;
	}

	@Override
	public void onRebind( Intent intent ) {
		// All clients disconnected, and another connection is made
		Log.d( LOG_TAG, "onRebind" );
	}

	@Override
	public boolean onUnbind( Intent intent ) {
		Log.d( LOG_TAG, "onUnbind" );
		return true;
	}

	@Override
	public void onCreate() {
		Log.d( LOG_TAG, "onCreate" );
		playlist = new Playlist();
		mediaPlayer = new Player( this, playlist );

		// Setup Listner
		listener = new PlayerInterfaceListener();
		mediaPlayer.setPlayerListener( listener );
	}

	@Override
	public void onDestroy() {
		Log.d( LOG_TAG, "onDestroy" );
		mediaPlayer.quit(); // Cleanup the telephony handler
	}

	@Override
	protected void finalize() {
		Log.d( LOG_TAG, "Android hath slain me :(killed):" );
		// TODO Warn the user (text) that the VM is killing the service
	}

	// Interface **********************************************************

	private final IPlayerService.Stub mBinder = new IPlayerService.Stub() {
		// Player Status
		public boolean isPlaying() {
			return mediaPlayer.isPlaying();
		}
		public boolean isSeekable() {
			return mediaPlayer.isSeekable();
		}
		public int getBuffer() {
			return mediaPlayer.getBuffer();
		}
		public int getCurrentPosition() {
			return mediaPlayer.getCurrentPosition();
		}
		public int getDuration() {
			return mediaPlayer.getDuration();
		}

		// Player Controls
		public void playMedia( Media media ) {
			// TODO REMOVEME
			sendMessage( MSG_PLAY );
			mediaPlayer.playMedia( media );
			statusNotify();
		}
		public void playPause() {
			mediaPlayer.doPauseResume();
			stopNotify();
		}
		public void stop() {
			mediaPlayer.stop();
			stopNotify();
		}
		public void next() {
			mediaPlayer.playMedia( playlist.next() );
			statusNotify();
		}
		public void prev() {
			mediaPlayer.playMedia( playlist.prev() );
			statusNotify();
		}
		public void seek( int msec ) {
			mediaPlayer.seekTo( msec );
		}

		// Playlist Controls
		public Media nextItem() {
			return playlist.next();
		}
		public Media prevItem() {
			return playlist.prev();
		}

		// Playlist List Modifiers
		public Media[] currentPlaylist() {
			Media[] tmp = new Media[ playlist.size() ];

			for ( int c = 0; c < playlist.size(); c++ ) {
				tmp[c] = playlist.get( c );
			}

			return tmp;
		}
		public boolean add( Media media ) {
			return playlist.add( media );
		}
		public boolean enqueue( Media[] media ) {
			// Adds the given list of media items to the playlist
			return playlist.addAll( Arrays.asList( media ) );
		}
		public boolean replace( Media[] media ) {
			// Clears the playlist and replaces it with the given one
			playlist.clearPlaylist();
			playlist.clearShuffleHistory();
			return playlist.addAll( Arrays.asList( media ) );
		}
		public void clearPlaylist() {
			playlist.clearPlaylist();
		}
		
		// Playlist Item
		public int getCurrentIndex() {
			return playlist.getCurrentIndex();
		}
		public int getPlaylistSize() {
			return playlist.size();
		}
		public Media getCurrentMedia() {
			return playlist.getCurrentMedia();
		}
		public Media setCurrentIndex( int index ) {
			return playlist.setCurrentIndex( index );
		}
		
		// Shuffle/Repeat
		public boolean getShufflePlay() {
			return playlist.getShufflePlay();
		}
		public boolean getRepeatPlay() {
			return playlist.getRepeatPlay();
		}
		public void setShufflePlay( boolean randomize ) {
			playlist.setShufflePlay( randomize );

			// Callback
			sendMessage( MSG_SHUFFLE_CHANGED, randomize ? 0 : 1 );
		}
		public void setRepeatPlay( boolean loop ) {
			playlist.setRepeatPlay( loop );

			// Callback
			sendMessage( MSG_REPEAT_CHANGED, loop ? 0 : 1 );
		}
		public void clearShuffleHistory() {
			playlist.clearShuffleHistory();
		}

		// Misc
		public void closeService() {
		}
		public void registerMessenger( Messenger messenger ) {
			clients.add( messenger );
		}
		public void unregisterMessenger( Messenger messenger ) {
			int remove = clients.lastIndexOf( messenger );

			if ( remove >= 0 )
				clients.remove( remove );
		}

		public void setAuthToken(String authToken)
		{
			mediaPlayer.setAuthToken(authToken);
		}
	};


	// **********************************************************************
	// Service to Client Messages *******************************************
	// **********************************************************************

	// Basic message types
	static final int MSG_SEEK_POSITION = 1;       // Sent every time a seek is completed
	static final int MSG_BUFFER_PERCENTAGE = 2;   // Sent when buffer updates
	static final int MSG_NEW_MEDIA = 3;           // Sent on new media playing
	static final int MSG_PLAYLIST_INDEX = 4;      // Sent on new media playing
	static final int MSG_SHUFFLE_CHANGED = 5;     // Sent if the shuffle setting is changed
	static final int MSG_REPEAT_CHANGED = 6;      // Sent if the repeat setting is changed
	static final int MSG_PLAY = 7;                // Sent if the media player starts playing
	static final int MSG_PAUSE = 8;               // Sent if the media player pauses the media
	static final int MSG_STOP = 9;                // Sent if the media player stops playing
	static final int MSG_VIDEO_SIZE_CHANGED = 10; // Sent if the video size changes
	static final int MSG_PLAYLIST_CHANGED = 11;
	static final int MSG_SERVICE_CONNECTED = 100;
	static final int MSG_SERVICE_DISCONNECTED = 101;
	static final int MSG_ERROR = 200;

	// 1  - arg1 | getCurrentPosition - arg2 | 0
	// 2  - arg1 | buffer percentage - arg2 | 0
	// 3  - no args
	// 4  - arg1 | playlist index - arg2 | 0
	// 5  - arg1 | 0 = True / 1 = False - arg2 | 0
	// 6  - arg1 | 0 = True / 1 = False - arg2 | 0
	// 7  - no args
	// 8  - no args
	// 9  - no args
	// 10 - arg1 | new width - arg2 | new height
	// 11 - arg1 | new size

	public void sendMessage( int message, int arg1, int arg2 ) {
		for ( int c = 0; c < clients.size(); c++ ) {
			try {
				clients.get( c ).send( Message.obtain( null, message, arg1, arg2 ) );
			}
			catch ( RemoteException exp ) {
				// Client is dead, remove it
				clients.remove( c );
			}
		}
	}

	public void sendMessage( int message, int arg ) {
		sendMessage( message, arg, 0 );
	}

	public void sendMessage( int message ) {
		for ( int c = 0; c < clients.size(); c++ ) {
			try {
				clients.get( c ).send( Message.obtain( null, message ) );
			}
			catch ( RemoteException exp ) {
				// Client is dead, remove it
				clients.remove( c );
			}
		}
	}

	// Player Interface
	private class PlayerInterfaceListener implements Player.PlayerListener {
		public void onPlayerStopped() {
			sendMessage( MSG_STOP );
		}

		public void onTogglePlaying( boolean playing ) {
			if ( playing )
				sendMessage( MSG_PLAY );
			else
				sendMessage( MSG_PAUSE );
		}

		public void onNewMediaPlaying( Media media ) {
			sendMessage( MSG_NEW_MEDIA );
		}

		public void onVideoSizeChanged( int width, int height ) {
			sendMessage( MSG_VIDEO_SIZE_CHANGED, width, height );
		}

		public void onBuffering( int buffer ) {
			sendMessage( MSG_BUFFER_PERCENTAGE, buffer );
		}

		public void onSeek( int position ) {
			sendMessage( MSG_SEEK_POSITION, position );
		}

		@Override
		public void onError(int what, int extra)
		{
			sendMessage(MSG_ERROR, what, extra);
		}
	}


	// **********************************************************************
	// Notifications ********************************************************
	// **********************************************************************

	// Start notifications
	public void statusNotify() {
		// Setup Notification Manager for Amdroid
		Context context = getApplicationContext();
		NotificationManager amdroidNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = com.sound.ampache.R.drawable.amdroid_notification;

		String mediaName = "No media";
		String extraString = "";
		if (playlist != null && playlist.getCurrentMedia() != null) {
			mediaName = playlist.getCurrentMedia().name;
			extraString = playlist.getCurrentMedia().extraString();
		}
		CharSequence tickerText = "Amdroid - " + mediaName;              
		long when = System.currentTimeMillis();        

		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent mediaIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		// TODO: upgrade
		Notification notification = new Notification(icon, tickerText, when);
		notification.setLatestEventInfo(context, mediaName, extraString, mediaIntent);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		amdroidNotifyManager.notify(1, notification);
	}
    
	// Stop notifications
	public void stopNotify() {
		// Setup Notification Manager for Amdroid
		NotificationManager amdroidNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		amdroidNotifyManager.cancel(1);
	}

}


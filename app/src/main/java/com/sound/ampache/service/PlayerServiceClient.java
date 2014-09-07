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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class PlayerServiceClient {

	private static final String LOG_TAG = "Ampache_Amdroid_PlayerServiceClient";

	private IPlayerService playerService;
	private PlayerServiceConnection conn;
	private Context mContext;
	private Set<PlayerServiceStatusListener> statusListeners = new HashSet<PlayerServiceStatusListener>();

	public PlayerServiceClient() {
	}

	protected void finalize() {
		releaseService();
	}

	public void registerServiceStatusListener(PlayerServiceStatusListener listener)
	{
		Log.d(LOG_TAG, "Adding new service status listener: " + listener);
		statusListeners.add(listener);
	}

	public void unregisterServiceStatusListener(PlayerServiceStatusListener listener)
	{
		Log.d(LOG_TAG, "Removing service status listener: " + listener);
		statusListeners.remove(listener);
	}

	// **********************************************************************
	// Client to Service Requests *******************************************
	// **********************************************************************

	public void initService( Context context ) {
		if ( conn == null ) {
			mContext = context;
			conn = new PlayerServiceConnection();

			Intent intent = new Intent();
			intent.setClassName( "com.sound.ampache", "com.sound.ampache.service.PlayerService" );
			try {
				if ( !mContext.bindService( intent, conn, Context.BIND_AUTO_CREATE ) )
					Log.d( LOG_TAG, "Failed: bindService()" );
			}
			catch ( SecurityException exp ) {
				Log.d( LOG_TAG, "Failed (Security): bindService()" );
			}
			Log.d( LOG_TAG, "bindService()" );
			// TODO Ready
		}
		else {
			// TODO Warn user that init binding failed
		}
	}

	public void releaseService() {
		if ( conn != null ) {
			mContext.unbindService( conn );
			conn = null;
			Log.d( LOG_TAG, "unbindService()" );
		}
		else {
			// TODO Warn user that service was already unbound
		}
	}

	public IPlayerService serviceInterface() {
		// Make sure to check for DeadObjectException when calling interface functions
		if ( conn != null ) {
			if ( playerService != null ) {
				//Log.d( LOG_TAG, "Accessing PlayerService interface." );
				return playerService;
			}

			Log.d( LOG_TAG, "Tried accessing PlayerService interface, but it's null..." );
			return null;
		}

		Log.d( LOG_TAG, "Tried accessing PlayerService interface, but connection does not exist..." );
		// TODO Warn user that the service is not bound/connected
		return null;
	}


	class PlayerServiceConnection implements ServiceConnection {
		public void onServiceConnected( ComponentName className, IBinder boundService ) {
			playerService = IPlayerService.Stub.asInterface( (IBinder)boundService );
			Log.d( LOG_TAG, "onServiceConnected" );

			// Send Messenger
			try {
				serviceInterface().registerMessenger( mMessenger );
			}
			catch ( RemoteException ex ) {
				Log.e( LOG_TAG, "Could not register Service Callback Messenger!! Very Bad!!" );
			}

			// notify service status listeners
			Message msg = new Message();
			msg.what = PlayerService.MSG_SERVICE_CONNECTED;
			try {
				mMessenger.send(msg);
			} catch (RemoteException e) {
				Log.d(LOG_TAG, "Local listener seems dead.", e);
			}
		}

		public void onServiceDisconnected( ComponentName className ) {
			playerService = null;
			Log.d( LOG_TAG, "onServiceDisconnected" );

			// notify service status listeners
			Message msg = new Message();
			msg.what = PlayerService.MSG_SERVICE_CONNECTED;
			try {
				mMessenger.send(msg);
			} catch (RemoteException e) {
				Log.d(LOG_TAG, "Local listener seems dead.", e);
			}
		}
	}


	// **********************************************************************
	// Service to Client Messages *******************************************
	// **********************************************************************

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage( Message msg )
		{
			// handle (log) message
			switch ( msg.what ) {
				case PlayerService.MSG_SEEK_POSITION:
					Log.d( LOG_TAG, "MSG_SEEK_POSITION: " + msg.arg1 );
					break;

				case PlayerService.MSG_BUFFER_PERCENTAGE:
					//Log.d( LOG_TAG, "MSG_BUFFER_PERCENTAGE: " + msg.arg1 );
					break;

				case PlayerService.MSG_NEW_MEDIA:
					Log.d( LOG_TAG, "MSG_NEW_MEDIA" );
					break;

				case PlayerService.MSG_PLAYLIST_INDEX:
					Log.d( LOG_TAG, "MSG_PLAYLIST_INDEX: " + msg.arg1 );
					break;

				case PlayerService.MSG_SHUFFLE_CHANGED:
					Log.d( LOG_TAG, "MSG_SHUFFLE_CHANGED: " + msg.arg1 == "0" ? "on" : "off" );
					break;

				case PlayerService.MSG_REPEAT_CHANGED:
					Log.d( LOG_TAG, "MSG_REPEAT_CHANGED: " + msg.arg1 == "0" ? "on" : "off");
					break;

				case PlayerService.MSG_PLAY:
					Log.d( LOG_TAG, "MSG_PLAY" );
					break;

				case PlayerService.MSG_PAUSE:
					Log.d( LOG_TAG, "MSG_PAUSE" );
					break;

				case PlayerService.MSG_STOP:
					Log.d( LOG_TAG, "MSG_STOP" );
					break;

				case PlayerService.MSG_VIDEO_SIZE_CHANGED:
					Log.d( LOG_TAG, "MSG_VIDEO_SIZE_CHANGED | Width: " + msg.arg1 + " | Height: " + msg.arg2 );
					break;

				case PlayerService.MSG_PLAYLIST_CHANGED:
					Log.d( LOG_TAG, "MSG_PLAYLIST_CHANGED: " + msg.arg1 );
					break;

				case PlayerService.MSG_SERVICE_CONNECTED:
					Log.d(LOG_TAG, "MSG_SERVICE_CONNECTED.");
					break;

				case PlayerService.MSG_SERVICE_DISCONNECTED:
					Log.d(LOG_TAG, "MSG_SERVICE_CONNECTED.");
					break;

				case PlayerService.MSG_ERROR:
					Log.d(LOG_TAG, "MSG_ERROR | What: " + msg.arg1 + " | Extra: " + msg.arg2);
					break;

				default:
					super.handleMessage(msg);
					break;
			}

			// pass message to status listeners
			for (PlayerServiceStatusListener listener : statusListeners) {
				switch (msg.what) {
					case PlayerService.MSG_SEEK_POSITION:
						listener.onSeek(msg.arg1);
						break;

					case PlayerService.MSG_BUFFER_PERCENTAGE:
						listener.onBuffering(msg.arg1);
						break;

					case PlayerService.MSG_NEW_MEDIA:
						listener.onNewMedia();
						break;

					case PlayerService.MSG_PLAYLIST_INDEX:
						listener.onPlaylistIndexChanged(msg.arg1);
						break;

					case PlayerService.MSG_SHUFFLE_CHANGED:
						listener.onShuffledChanged(msg.arg1);
						break;

					case PlayerService.MSG_REPEAT_CHANGED:
						listener.onRepeatChanged(msg.arg1);
						break;

					case PlayerService.MSG_PLAY:
						listener.onPlay();
						break;

					case PlayerService.MSG_PAUSE:
						listener.onPause();
						break;

					case PlayerService.MSG_STOP:
						listener.onStop();
						break;

					case PlayerService.MSG_VIDEO_SIZE_CHANGED:
						listener.onVideoSizeChanged(msg.arg1, msg.arg2);
						break;

					case PlayerService.MSG_PLAYLIST_CHANGED:
						listener.onPlaylistChanged(msg.arg1);
						break;

					case PlayerService.MSG_SERVICE_CONNECTED:
						listener.onServiceConnected();
						break;

					case PlayerService.MSG_SERVICE_DISCONNECTED:
						listener.onServiceDisconnected();
						break;

					case PlayerService.MSG_ERROR:
						listener.onError(msg.arg1, msg.arg2);
						break;
				}
			}
		}
	}

	// Target for the incoming messages
	final Messenger mMessenger = new Messenger( new IncomingHandler() );
}


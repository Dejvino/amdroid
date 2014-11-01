package com.sound.ampache.utility;

/* Copyright (c) 2010 ABAAKOUK Mehdi <theli48@gmail.com>
 *
 * For the PhoneStateListener:
 *  Copyright (c) 2008 Kevin James Purdy <purdyk@onid.orst.edu>
 * 
 * Amdroid Port:
 *  Copyright (c) 2010 Jacob Alexander < haata@users.sf.net >
 *  Copyrigth (c) 2010 Michael Gapczynski <GapczynskiM@gmail.com>
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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.sound.ampache.objects.Media;
import com.sound.ampache.objects.Song;
import com.sound.ampache.objects.Video;

import java.util.ArrayList;

public class Player
{

	private MediaPlayer mPlayer;
	private Playlist mPlaylist;

	private static String TAG = "AmdroidPlayer";
	private Song mSong;
	private Video mVideo;
	private int mBuffering = -1;

	private Boolean mPlayAfterPrepared = true;

	private enum STATE
	{
		Idle, Initialised, Prepared, Started, Paused, Stopped
	}

	private STATE mState;

	private Context mContext;

	private MyPhoneStateListener mPhoneStateListener;
	private MyMediaPlayerListener mMediaPlayerListener;

	private ArrayList<PlayerListener> mPlayerListeners;

	private String authToken;

	public interface PlayerListener
	{
		void onTogglePlaying(boolean playing);

		void onPlayerStopped();

		void onNewMediaPlaying(Media media);

		void onVideoSizeChanged(int width, int height);

		void onBuffering(int buffer);

		void onSeek(int position);

		void onError(int what, int extra);
	}

	public Player(Context context, Playlist playlist)
	{

		mContext = context;
		mPlaylist = playlist;

		mPhoneStateListener = new MyPhoneStateListener();

		mPlayerListeners = new ArrayList<PlayerListener>();

		mMediaPlayerListener = new MyMediaPlayerListener();

		// Prepare Android Media Player
		mPlayer = new MediaPlayer();
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

		// Setting up Media Player Listeners
		mPlayer.setOnBufferingUpdateListener(mMediaPlayerListener);
		mPlayer.setOnCompletionListener(mMediaPlayerListener);
		mPlayer.setOnErrorListener(mMediaPlayerListener);
		mPlayer.setOnInfoListener(mMediaPlayerListener);
		mPlayer.setOnPreparedListener(mMediaPlayerListener);
		mPlayer.setOnSeekCompleteListener(mMediaPlayerListener);
		mPlayer.setOnVideoSizeChangedListener(mMediaPlayerListener);

		setState(STATE.Idle);
	}

	public void stop()
	{
		for (PlayerListener obj : mPlayerListeners) {
			obj.onPlayerStopped();
		}

		mPlayer.stop();
		mPlayer.reset();
		setState(STATE.Stopped);
	}

	private void setState(STATE state)
	{
		mState = state;

		for (PlayerListener obj : mPlayerListeners) {
			obj.onTogglePlaying(isPlaying());
		}

		String st = "";
		switch (state) {
			case Idle:
				st = "Idle";
				break;
			case Initialised:
				st = "Initialised";
				break;
			case Prepared:
				st = "Prepared";
				break;
			case Started:
				st = "Started";
				break;
			case Paused:
				st = "Paused";
				break;
			case Stopped:
				st = "Stopped";
				break;
			default:
				st = "Unknown";
				break;
		}
		Log.v(TAG, "setState(" + st + ")");
	}

	private void updateBuffer(int buffer)
	{
		mBuffering = buffer;
		for (PlayerListener obj : mPlayerListeners) {
			obj.onBuffering(mBuffering);
		}
	}

	public int getBuffer()
	{
		return mBuffering;
	}

	public void playMedia(Media media)
	{
		if (media == null) {
			Log.w(TAG, "Null input, cannot playMedia().");
			return;
		}
		if ("Song".equals(media.getType())) {
			playSong((Song) media);
		} else if ("Video".equals(media.getType())) {
			playVideo((Video) media);
		} else {
			Log.e(TAG, "Invalid Media Type: " + media.getType());
		}
	}

	public void playSong(Song song)
	{
		setState(STATE.Idle);

		String uri = song.liveUrl(authToken);
		Log.v(TAG, "Playing uri: " + uri);

		if (mState == STATE.Prepared || mState == STATE.Started
				|| mState == STATE.Paused) {
			mPlayer.stop();
		}

		mPlayAfterPrepared = true;
		mSong = song;
		updateBuffer(-1);

		for (PlayerListener obj : mPlayerListeners) {
			obj.onNewMediaPlaying((Media) mSong);
		}

		mPlayer.reset();
		try {
			mPlayer.setDataSource(uri);
			setState(STATE.Initialised);
			mPlayer.prepareAsync();
		} catch (Exception blah) {
			return;
		}
	}

	public void playVideo(Video video)
	{
		setState(STATE.Idle);

		String uri = video.liveUrl(authToken);
		Log.v(TAG, "Playing uri: " + uri);

		if (mState == STATE.Prepared || mState == STATE.Started
				|| mState == STATE.Paused) {
			mPlayer.stop();
		}

		mPlayAfterPrepared = true;
		mVideo = video;
		updateBuffer(-1);

		for (PlayerListener obj : mPlayerListeners) {
			obj.onNewMediaPlaying((Media) mVideo);
		}

		mPlayer.reset();
		try {
			mPlayer.setDataSource(uri);
			setState(STATE.Initialised);
			mPlayer.prepareAsync();
		} catch (Exception blah) {
			return;
		}
	}

	public void doPauseResume()
	{
		if (mState == STATE.Started || mState == STATE.Paused) {
			if (mPlayer.isPlaying()) {
				mPlayer.pause();
				setState(STATE.Paused);
			} else {
				mPlayer.start();
				setState(STATE.Started);
			}
		} else if (mState == STATE.Initialised) {
			mPlayAfterPrepared = !mPlayAfterPrepared;
		} else if (mState == STATE.Prepared) {
			mPlayer.start();
			setState(STATE.Started);
		}
	}

	public void seekTo(int position)
	{
		if (mState == STATE.Prepared || mState == STATE.Started
				|| mState == STATE.Paused) {
			mPlayer.seekTo(position);
		}
	}

	public boolean isSeekable()
	{
		return mState == STATE.Prepared || mState == STATE.Started
				|| mState == STATE.Paused;
	}

	public boolean isPlaying()
	{
		return (mState == STATE.Initialised && mPlayAfterPrepared)
				|| mState == STATE.Started;
	}

	public int getCurrentPosition()
	{
		if (mState == STATE.Prepared || mState == STATE.Started
				|| mState == STATE.Paused) {
			return mPlayer.getCurrentPosition();
		} else {
			return 0;
		}
	}

	public int getDuration()
	{
		if (mState == STATE.Initialised || mState == STATE.Prepared
				|| mState == STATE.Started || mState == STATE.Paused) {
			try {
				return Integer.parseInt(mSong.time) * 1000;
			} catch (Exception poo) {
			}
			if (mState != STATE.Initialised) {
				return mPlayer.getDuration();
			}
		}
		return 0;
	}

	public void quit()
	{
		TelephonyManager tmgr = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		tmgr.listen(mPhoneStateListener, 0);
	}

	public void setAuthToken(String authToken)
	{
		this.authToken = authToken;
	}

	public void setPlayerListener(PlayerListener StatusChangeObject)
	{
		mPlayerListeners.add(StatusChangeObject);
	}

	private class MyMediaPlayerListener implements
			MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener,
			MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
			MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener,
			MediaPlayer.OnVideoSizeChangedListener
	{
		public void onBufferingUpdate(MediaPlayer mp, int buffer)
		{
			updateBuffer(buffer);
		}

		public void onCompletion(MediaPlayer mp)
		{
			mSong = null;
			mVideo = null;

			mPlayer.stop();
			setState(STATE.Stopped);

			Log.v(TAG, "Completion");
			Media media = mPlaylist.next();

			if (media == null) {
				for (PlayerListener obj : mPlayerListeners) {
					obj.onPlayerStopped();
				}

				mPlayer.stop();
				mPlayer.reset();
				setState(STATE.Stopped);

				return;
			}

			playMedia(media);
		}

		public boolean onError(MediaPlayer mp, int what, int extra)
		{
			Log.e(TAG, "Player error (" + what + ", " + extra + ")");
			for (PlayerListener obj : mPlayerListeners) {
				obj.onError(what, extra);
			}
			return true;
		}

		public boolean onInfo(MediaPlayer mp, int what, int extra)
		{
			Log.d(TAG, "Player info (" + what + ", " + extra + ")");
			return true;
		}

		public void onPrepared(MediaPlayer mp)
		{
			setState(STATE.Prepared);
			if (mPlayAfterPrepared) {
				mPlayer.start();
				setState(STATE.Started);
			}
			mPlayAfterPrepared = true;
		}

		public void onSeekComplete(MediaPlayer mp)
		{
			for (PlayerListener obj : mPlayerListeners) {
				obj.onSeek(mp.getCurrentPosition());
			}
		}

		public void onVideoSizeChanged(MediaPlayer mp, int width, int height)
		{
			for (PlayerListener obj : mPlayerListeners) {
				obj.onVideoSizeChanged(width, height);
			}
		}
	}

	// Handle phone calls
	private class MyPhoneStateListener extends PhoneStateListener
	{
		private Boolean mResumeAfterCall = false;

		@Override
		public void onCallStateChanged(int state, String incomingNumber)
		{
			if (state == TelephonyManager.CALL_STATE_RINGING) {
				AudioManager audioManager = (AudioManager) mContext
						.getSystemService(Context.AUDIO_SERVICE);
				int ringvolume = audioManager
						.getStreamVolume(AudioManager.STREAM_RING);
				if (ringvolume > 0) {
					mResumeAfterCall = (mPlayer.isPlaying() || mResumeAfterCall);
					mPlayer.pause();
				}
			} else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
				// pause the music while a conversation is in progress
				mResumeAfterCall = (mPlayer.isPlaying() || mResumeAfterCall);
				mPlayer.pause();
			} else if (state == TelephonyManager.CALL_STATE_IDLE) {
				// start playing again
				if (mResumeAfterCall) {
					// resume playback only if music was playing
					// when the call was answered
					mPlayer.start();
					mResumeAfterCall = false;
				}
			}
		}
	}

	;
}


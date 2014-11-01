package com.sound.ampache;

/* Copyright (c) 2008 Kevin James Purdy <purdyk@onid.orst.edu>
 * Copyright (c) 2010 Kristopher Heijari < iix.ftw@gmail.com >
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

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.sound.ampache.service.AbstractPlayerServiceStatusListener;

import java.util.Formatter;
import java.util.Locale;

/**
 * Media player controls fragment.
 */
public final class MiniPlayer extends Fragment
{
	private static final String LOG_TAG = "Ampache_Amdroid_MiniPlayer";

	private static int bufferingPercent = 0;

	private ProgressBar mProgress;
	private TextView mEndTime, mCurrentTime;
	private boolean mDragging;
	private ImageButton mPauseButton;
	private ImageButton mNextButton;
	private ImageButton mPrevButton;
	private ImageButton mRepeatButton;
	private ImageButton mShuffleButton;

	private static final int FADE_OUT = 1;
	private static final int SHOW_PROGRESS = 2;

	StringBuilder mFormatBuilder;
	Formatter mFormatter;

	private PlaybackListener playbackListener = new PlaybackListener();

	/*
	 * Listener variables for our buttons defined in the layout. The listeners are bound to their
	 * buttons in initControllerView
	 */
	private View.OnClickListener mPauseListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			amdroid.playbackControl.doPauseResume();
		}
	};

	private View.OnClickListener mNextListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			amdroid.playbackControl.nextInPlaylist();
			amdroid.playbackControl.play();
		}

	};

	private View.OnClickListener mPrevListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			amdroid.playbackControl.prevInPlaylist();
			amdroid.playbackControl.play();
		}

	};

	private View.OnClickListener mShuffleListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			if (amdroid.playbackControl.shuffleEnabled()) {
				((ImageButton) view).setImageResource(R.drawable.ic_menu_shuffle_disabled);
				// Disable Shuffle
				amdroid.playbackControl.setShuffle(false);
				Toast.makeText(getContext(), "Shuffle Disabled", Toast.LENGTH_SHORT).show();
			} else {
				((ImageButton) view).setImageResource(R.drawable.ic_menu_shuffle);

				// Enable Shuffle
				amdroid.playbackControl.setShuffle(true);
				Toast.makeText(getContext(), "Shuffle Enabled", Toast.LENGTH_SHORT).show();
			}
		}

	};

	private View.OnClickListener mRepeatListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			if (amdroid.playbackControl.repeatEnabled()) {
				((ImageButton) view).setImageResource(R.drawable.ic_menu_revert_disabled);
				// Disable Repeat
				amdroid.playbackControl.setRepeat(false);
				Toast.makeText(getContext(), "Repeat Disabled", Toast.LENGTH_SHORT).show();
			} else {
				((ImageButton) view).setImageResource(R.drawable.ic_menu_revert);
				// Enable Repeat
				amdroid.playbackControl.setRepeat(true);
				Toast.makeText(getContext(), "Repeat Enabled", Toast.LENGTH_SHORT).show();
			}
		}

	};

	/*
	 * Listener to respond to changes made to the seekbar.
	 */
	private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener()
	{
		long duration;

		public void onStartTrackingTouch(SeekBar bar)
		{
			duration = amdroid.playbackControl.getDuration();
		}

		public void onProgressChanged(SeekBar bar, int progress, boolean fromtouch)
		{
			if (fromtouch) {
				mDragging = true;
				long newposition = (duration * progress) / 1000L;
				amdroid.playbackControl.seekTo((int) newposition);
				if (mCurrentTime != null)
					mCurrentTime.setText(stringForTime((int) newposition));
			}
		}

		public void onStopTrackingTouch(SeekBar bar)
		{
			mDragging = false;
			setProgress();
			updatePausePlay();
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.mini_player, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		// Bind listeners to our components
		mPauseButton = (ImageButton) view.findViewById(R.id.pause);
		if (mPauseButton != null) {
			mPauseButton.setOnClickListener(mPauseListener);
		}

		mNextButton = (ImageButton) view.findViewById(R.id.next);
		if (mNextButton != null) {
			mNextButton.setOnClickListener(mNextListener);
		}

		mPrevButton = (ImageButton) view.findViewById(R.id.prev);
		if (mPrevButton != null) {
			mPrevButton.setOnClickListener(mPrevListener);
		}

		// TODO: move to playlist
//        mRepeatButton = (ImageButton) view.findViewById(R.id.repeat);
//        if (mRepeatButton != null) {
//			mRepeatButton.setOnClickListener( mRepeatListener );
//			/* TODO!!
//			if ( amdroid.playbackControl.repeatEnabled() )
//				mRepeatButton.setImageResource( R.drawable.ic_menu_revert );
//			else
//				mRepeatButton.setImageResource( R.drawable.ic_menu_revert_disabled );
//			*/
//		}
//
//        mShuffleButton = (ImageButton) view.findViewById(R.id.shuffle);
//        if (mShuffleButton != null) {
//            mShuffleButton.setOnClickListener(mShuffleListener);
//		/* TODO!!
//            if ( amdroid.playbackControl.shuffleEnabled() )
//            	mShuffleButton.setImageResource( R.drawable.ic_menu_shuffle );
//            else
//            	mShuffleButton.setImageResource( R.drawable.ic_menu_shuffle_disabled );
//		*/
//        }

		mProgress = (SeekBar) view.findViewById(R.id.mediacontroller_progress);
		if (mProgress != null) {
			if (mProgress instanceof SeekBar) {
				SeekBar seeker = (SeekBar) mProgress;
				seeker.setOnSeekBarChangeListener(mSeekListener);
			}
			mProgress.setMax(1000);
		}

		mEndTime = (TextView) view.findViewById(R.id.time);
		mCurrentTime = (TextView) view.findViewById(R.id.time_current);
		mFormatBuilder = new StringBuilder();
		mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

		amdroid.playbackControl.registerServiceStatusListener(playbackListener);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		amdroid.playbackControl.unregisterServiceStatusListener(playbackListener);
	}

	private String stringForTime(int timeMs)
	{
		int totalSeconds = timeMs / 1000;

		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		mFormatBuilder.setLength(0);
		if (hours > 0) {
			return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
		} else {
			return mFormatter.format("%02d:%02d", minutes, seconds).toString();
		}
	}

	/**
	 * Reads the progress information from the player service and updates the corresponding UI elements.
	 *
	 * @return position (progress), returned from {@link GlobalMediaPlayerControl#getCurrentPosition()}.
	 */
	private int setProgress()
	{
		if (mDragging) {
			return 0;
		}
		int position = amdroid.playbackControl.getCurrentPosition();
		int duration = amdroid.playbackControl.getDuration();
		if (mProgress != null) {
			if (duration > 0) {
				// use long to avoid overflow
				long pos = 1000L * position / duration;
				mProgress.setProgress((int) pos);
			}
			mProgress.setSecondaryProgress(bufferingPercent * 10);
		}

		if (mEndTime != null)
			mEndTime.setText(stringForTime(duration));
		if (mCurrentTime != null)
			mCurrentTime.setText(stringForTime(position));

		return position;
	}

	private void updatePausePlay()
	{
		ImageButton button = mPauseButton;
		if (button == null)
			return;

		if (amdroid.playbackControl.isPlaying()) {
			button.setImageResource(android.R.drawable.ic_media_pause);
		} else {
			button.setImageResource(android.R.drawable.ic_media_play);
		}
	}

	private Context getContext()
	{
		return getActivity();
	}

	/**
	 * Handles messages from the player service.
	 * <p/>
	 * Contains periodic song progress updater.
	 */
	private class PlaybackListener extends AbstractPlayerServiceStatusListener
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);

			if (msg.what == SHOW_PROGRESS) {
				updateProgress();
			}
		}

		@Override
		public void onPlay()
		{
			updateProgress();
		}

		@Override
		public void onPause()
		{
			updateProgress();
		}

		@Override
		public void onStop()
		{
			updateProgress();
		}

		@Override
		public void onSeek(int position)
		{
			setProgress();
		}

		@Override
		public void onBuffering(int buffer)
		{
			bufferingPercent = buffer;
			updateProgress();
		}

		@Override
		public void onError(int what, int extra)
		{
			Context ctx = getActivity();
			if (ctx != null) {
				Toast.makeText(ctx, "Media player error (" + what + ", " + extra + ")", Toast.LENGTH_SHORT).show();
			}
		}

		// TODO: add to interface
		public void onBufferingCompleted()
		{
			amdroid.playbackControl.nextInPlaylist();
			amdroid.playbackControl.play();
		}

		private void updateProgress()
		{
			setProgress();
			cancelProgressUpdate();
			if (amdroid.playbackControl.isPlaying()) {
				scheduleProgressUpdate();
			}
		}

		private void scheduleProgressUpdate()
		{
			sendEmptyMessageDelayed(SHOW_PROGRESS, 500);
		}

		private void cancelProgressUpdate()
		{
			removeMessages(SHOW_PROGRESS);
		}
	}
}

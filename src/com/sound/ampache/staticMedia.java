package com.sound.ampache;

/* Copyright (c) 2008 Kevin James Purdy <purdyk@onid.orst.edu>
 * Copyright (c) 2010 Kristopher Heijari < iix.ftw@gmail.com >
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

import java.util.Formatter;
import java.util.Locale;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public final class staticMedia extends SlidingDrawer {
    private ProgressBar         mProgress;
    private TextView            mEndTime, mCurrentTime;
    private boolean             mDragging;
    private ImageButton mPauseButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private ImageButton mRepeatButton;
    private ImageButton mShuffleButton;
    private static final int    FADE_OUT = 1;
    private static final int    SHOW_PROGRESS = 2;
    StringBuilder               mFormatBuilder;
    Formatter                   mFormatter;
    
	/*
	 * Listener variables for our buttons defined in the layout. The listeners are bound to their
	 * buttons in initControllerView
	 */
	private View.OnClickListener mPauseListener = new View.OnClickListener() {
		@Override
		public void onClick( View view )
		{
			amdroid.playbackControl.doPauseResume();
			//updatePausePlay();
			show();
		}
	};
	
	private View.OnClickListener mNextListener = new View.OnClickListener() {
		@Override
		public void onClick( View view )
		{
			amdroid.playbackControl.nextInPlaylist();
			amdroid.playbackControl.play();
		}
		
	};
	
	private View.OnClickListener mPrevListener = new View.OnClickListener() {
		@Override
		public void onClick( View view )
		{
			amdroid.playbackControl.prevInPlaylist();
			amdroid.playbackControl.play();
		}
		
	};
	
	private View.OnClickListener mShuffleListener = new View.OnClickListener() {
		@Override
		public void onClick( View view )
		{
			if ( amdroid.playbackControl.shuffleEnabled() )
			{
				( (ImageButton)view ).setImageResource( R.drawable.ic_menu_shuffle_disabled );
				// Disable Shuffle
				amdroid.playbackControl.setShuffle( false );
				Toast.makeText( getContext(), "Shuffle Disabled", Toast.LENGTH_SHORT ).show();
			} else
			{
				( (ImageButton)view ).setImageResource( R.drawable.ic_menu_shuffle );

				// Enable Shuffle
				amdroid.playbackControl.setShuffle( true );
				Toast.makeText( getContext(), "Shuffle Enabled", Toast.LENGTH_SHORT ).show();
			}
		}

	};

	private View.OnClickListener mRepeatListener = new View.OnClickListener() {
		@Override
		public void onClick( View view )
		{
			if ( amdroid.playbackControl.repeatEnabled() )
			{
				( (ImageButton)view ).setImageResource( R.drawable.ic_menu_revert_disabled );
				// Disable Repeat
				amdroid.playbackControl.setRepeat( false );
				Toast.makeText( getContext(), "Repeat Disabled", Toast.LENGTH_SHORT ).show();
			} else
			{
				( (ImageButton)view ).setImageResource( R.drawable.ic_menu_revert );
				// Enable Repeat
				amdroid.playbackControl.setRepeat( true );
				Toast.makeText( getContext(), "Repeat Enabled", Toast.LENGTH_SHORT ).show();
			}
		}

	};

	/*
	 * Listener to respond to changes made to the seekbar.
	 */
	private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
		long duration;

		public void onStartTrackingTouch( SeekBar bar )
		{
			duration = amdroid.playbackControl.getDuration();
		}

		public void onProgressChanged( SeekBar bar, int progress, boolean fromtouch )
		{
			if ( fromtouch )
			{
				mDragging = true;
				long newposition = ( duration * progress ) / 1000L;
				amdroid.playbackControl.seekTo( (int)newposition );
				if ( mCurrentTime != null )
					mCurrentTime.setText( stringForTime( (int)newposition ) );
			}
		}

		public void onStopTrackingTouch( SeekBar bar )
		{
			mDragging = false;
			setProgress();
			updatePausePlay();
		}
	};

	/* 
	 * Handler variable for handling messages to show playback progress 
	 */	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage( Message msg )
		{
			int pos;
			switch (msg.what) {
			case SHOW_PROGRESS:
				pos = setProgress();
				if ( !mDragging && amdroid.playbackControl.isPlaying() )
				{
					msg = obtainMessage( SHOW_PROGRESS );
					sendMessageDelayed( msg, 1000 - ( pos % 1000 ) );
				}
				break;
			}
		}
	};
	
	/*
	 * Completion, buffering and prepared listener that is bound to our mediaPlayer object
	 */
	private OnCompletionListener mCompletionListener = new OnCompletionListener() {
		@Override
		public void onCompletion( MediaPlayer mp )
		{
	        amdroid.playbackControl.nextInPlaylist();
	        amdroid.playbackControl.play(); 
		}
		
	};
	
	private OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {
		@Override
		public void onBufferingUpdate( MediaPlayer mp, int percent )
		{
			amdroid.bufferPC = percent;
		}
		
	};
	
	private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
		public void onPrepared( MediaPlayer mp )
		{
			amdroid.playbackControl.start();
			if ( amdroid.playListVisible )
			{
				// mc.setEnabled(true);
				show();
				amdroid.playbackControl.prepared = true;
			}
		}
	};

	/* 
	 * Constructor and initialization functions
	 */
    public staticMedia(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void onFinishInflate(){
    	super.onFinishInflate();
	// TODO!!!
    	//amdroid.mp.setOnCompletionListener( mCompletionListener );
    	//amdroid.mp.setOnPreparedListener( mPreparedListener );
    	//amdroid.mp.setOnBufferingUpdateListener( mBufferingUpdateListener );
    	initControllerView();
    }
    

    /* 
     * Functions
     */
    private void initControllerView() {
    	// Bind listeners to our components
        mPauseButton = (ImageButton) findViewById(R.id.pause);
        if (mPauseButton != null) {
            mPauseButton.setOnClickListener(mPauseListener);
        }
        
        mNextButton = (ImageButton) findViewById(R.id.next);
        if (mNextButton != null) {
            mNextButton.setOnClickListener(mNextListener);
        }
        
        mPrevButton = (ImageButton) findViewById(R.id.prev);
        if (mPrevButton != null) {
            mPrevButton.setOnClickListener(mPrevListener);
        }
        
        mRepeatButton = (ImageButton) findViewById(R.id.repeat);
        if (mRepeatButton != null) {
			mRepeatButton.setOnClickListener( mRepeatListener );
			/* TODO!!
			if ( amdroid.playbackControl.repeatEnabled() )
				mRepeatButton.setImageResource( R.drawable.ic_menu_revert );
			else
				mRepeatButton.setImageResource( R.drawable.ic_menu_revert_disabled );
			*/
		}
        
        mShuffleButton = (ImageButton) findViewById(R.id.shuffle);
        if (mShuffleButton != null) {
            mShuffleButton.setOnClickListener(mShuffleListener);
		/* TODO!!
            if ( amdroid.playbackControl.shuffleEnabled() )
            	mShuffleButton.setImageResource( R.drawable.ic_menu_shuffle );
            else
            	mShuffleButton.setImageResource( R.drawable.ic_menu_shuffle_disabled );
		*/
        }

        mProgress = (ProgressBar) findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }

        mEndTime = (TextView) findViewById(R.id.time);
        mCurrentTime = (TextView) findViewById(R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        //show(); // TODO!!
    }

    public void show() {
        updatePausePlay();
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    public void hide() {
        mHandler.removeMessages(SHOW_PROGRESS);
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mDragging) {
            return 0;
        }
        int position = amdroid.playbackControl.getCurrentPosition();
        int duration = amdroid.playbackControl.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress( (int) pos);
            }
            int percent = amdroid.bufferPC;
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }

    private void updatePausePlay() {
        ImageButton button = (ImageButton) findViewById(R.id.pause);
        if (button == null)
            return;

        if (amdroid.playbackControl.isPlaying()) {
            button.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            button.setImageResource(android.R.drawable.ic_media_play);
        }
    }
    
	@Override
	public boolean dispatchKeyEvent( KeyEvent event )
	{
		int keyCode = event.getKeyCode();
		if ( event.getRepeatCount() == 0
				&& ( keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_SPACE ) )
		{
			amdroid.playbackControl.doPauseResume();
			return true;
		} else if ( keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
				|| keyCode == KeyEvent.KEYCODE_VOLUME_UP )
		{
			// don't show the controls for volume adjustment
			return super.dispatchKeyEvent( event );
		}
		return super.dispatchKeyEvent( event );
	}

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mNextButton != null) {
            mNextButton.setEnabled(enabled && mNextListener != null);
        }
        if (mPrevButton != null) {
            mPrevButton.setEnabled(enabled && mPrevListener != null);
        }
        if (mShuffleButton != null) {
            mShuffleButton.setEnabled(enabled && mShuffleListener != null);
        }
        if (mRepeatButton != null) {
            mRepeatButton.setEnabled(enabled && mRepeatListener != null);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }

        super.setEnabled(enabled);
    }
    

}

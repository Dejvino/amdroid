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
import java.util.LinkedList;

import com.sound.ampache.objects.Directive;
import com.sound.ampache.objects.Media;
import com.sound.ampache.objects.Song;
import com.sound.ampache.objects.ampacheObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class AmpacheListView extends ListView implements OnItemClickListener,
		OnItemLongClickListener {

	public DataHandler mDataHandler;
	private collectionAdapter mCollectionAdapter;
	private ArrayList<ampacheObject> mAmpacheObjectList;
	private Directive directive = new Directive();
	private IsFetchingListener isFetchingListener;
	private LinkedList<Directive> history = new LinkedList<Directive>();
	public int backOffset = 0;

	public AmpacheListView( Context context, AttributeSet attrs )
	{
		super( context, attrs );
	}

	protected void onFinishInflate()
	{
		super.onFinishInflate();

		mDataHandler = new DataHandler();
		mAmpacheObjectList = new ArrayList<ampacheObject>();
		mCollectionAdapter = new collectionAdapter( getContext(), R.layout.browsable_item,
				mAmpacheObjectList );
		setAdapter( mCollectionAdapter );
		setOnItemClickListener( this );
		setOnItemLongClickListener( this );
	}

	public LinkedList<Directive> getHistory()
	{
		return history;
	}

	public void clearHistory()
	{
		history.clear();
	}

	@Override
	public void onItemClick( AdapterView<?> adapterView, View view, int position, long arg3 )
	{

		ampacheObject val = null;

		val = (ampacheObject)adapterView.getItemAtPosition( position );
		if ( val == null )
			return;
		if ( val.getType().equals( "Song" ) )
		{
			Toast.makeText( getContext(), "Enqueue " + val.getType() + ": " + val.toString(),
					Toast.LENGTH_LONG ).show();
			amdroid.playbackControl.addPlaylistCurrent( (Song)val );
			return;
		}
		Directive directive = new Directive(val.childString(), val.id, val.name);

		mDataHandler.enqueMessage( DataHandler.AMPACHE_INIT_REQUEST, directive, 0, true );

	}

	@Override
	public boolean onItemLongClick( AdapterView<?> adapterView, View view, int position, long arg3 )
	{
		ampacheObject cur = (ampacheObject)adapterView.getItemAtPosition( position );
		Toast.makeText( getContext(), "Enqueue " + cur.getType() + ": " + cur.toString(),
				Toast.LENGTH_LONG ).show();
		if ( cur.hasChildren() )
		{
			mDataHandler.enqueMessage( DataHandler.ENQUEUE_SONG, new Directive(cur.allChildren()), 0, false );
		} else
		{
			amdroid.playbackControl.addPlaylistCurrent((Media) cur);
		}
		return true;
	}

	public interface IsFetchingListener {
		public void onIsFetchingChange( boolean isFetching );
	}

	public void setIsFetchingListener( IsFetchingListener listener )
	{
		isFetchingListener = listener;
	}

	public boolean backPressed()
	{
		boolean ret = false;
		if ( history.size() > 1 )
		{
			history.removeLast();
			try {
				directive = history.getLast().clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			mDataHandler.enqueMessage( DataHandler.AMPACHE_INIT_REQUEST, directive, 0, false );
			ret = true;
		} else if ( history.size() == 1 - backOffset )
		{
			history.removeLast();
			mCollectionAdapter.clear();
			mDataHandler.stopIncFetch = true;
			mDataHandler.setIsFetching( false );
			ret = true;
		}

		return ret;
	}

	protected class DataHandler extends Handler {

		public static final int AMPACHE_INIT_REQUEST = 0x1336;
		public static final int AMPACHE_INC_REQUEST = 0x1337;
		public static final int ENQUEUE_SONG = 0x1339;

		public boolean stopIncFetch = false;
		public boolean isFetching = false;

		public void setIsFetching( boolean val )
		{
			if ( isFetching != val )
			{
				isFetching = val;
				if ( isFetchingListener != null )
				{
					isFetchingListener.onIsFetchingChange( isFetching );
				}
			}
		}

		public void enqueMessage( int what, Directive directive, int startIndex, boolean addHistory )
		{
			if ( addHistory )
			{
				try {
					history.add(directive.clone());
				} catch (CloneNotSupportedException e) {
					Log.e(VIEW_LOG_TAG, "Cloning of directive failed.", e);
				}
			}
			if ( isFetching && what == AMPACHE_INIT_REQUEST )
			{
				stopIncFetch = true;
			}
			if ( what != ENQUEUE_SONG )
			{
				setIsFetching( true );
			}
			Message requestMsg = this.obtainMessage();
			requestMsg.arg1 = startIndex;
			requestMsg.obj = directive;
			requestMsg.what = what;
			// tell it how to handle the stuff
			requestMsg.replyTo = new Messenger( this );
			// old:
			//amdroid.requestHandler.incomingRequestHandler.sendMessage( requestMsg );
			Log.d("AmpacheAmdroidList", "Sending message: " + requestMsg.toString());
			amdroid.networkClient.sendMessage(requestMsg);
		}

		@Override
		public void handleMessage( Message msg )
		{
			Log.d("AmpacheAmdroidList", "Handling message: " + msg.toString());
			if ( msg.what == AMPACHE_INIT_REQUEST || msg.what == AMPACHE_INC_REQUEST )
			{
				if ( stopIncFetch && msg.what == AMPACHE_INC_REQUEST )
				{
					return;
				}

				// Clear the collection adapter in case we have received "leftovers"
				if ( msg.what == AMPACHE_INIT_REQUEST )
				{
					mCollectionAdapter.clear();
					stopIncFetch = false;
				}

				// Update our list with the received data
				ArrayList aList = (ArrayList)msg.obj;
				mAmpacheObjectList.addAll( aList );
				mCollectionAdapter.notifyDataSetChanged();

				// queue up a new inc fetch if we did not receive 100 results. 100 is the limit set
				// in ampacheCommunicator
				if ( aList.size() >= 100 )
				{
					enqueMessage( AMPACHE_INC_REQUEST, history.getLast(), msg.arg1 + 100, false );
				} else
				{
					setIsFetching( false );
				}

			} else if ( msg.what == ENQUEUE_SONG )
			{
				amdroid.playbackControl.addAllPlaylistCurrent( (ArrayList)msg.obj );
			} else
			{
				// Handle error
			}
		}

	}

}

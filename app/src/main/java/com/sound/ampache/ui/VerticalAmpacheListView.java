package com.sound.ampache.ui;

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

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.sound.ampache.MediaCollectionAdapter;
import com.sound.ampache.R;
import com.sound.ampache.amdroid;
import com.sound.ampache.objects.Directive;
import com.sound.ampache.objects.Media;
import com.sound.ampache.objects.Song;
import com.sound.ampache.objects.ampacheObject;

import java.util.ArrayList;
import java.util.LinkedList;

public class VerticalAmpacheListView extends ListView implements OnItemClickListener,
		OnItemLongClickListener, AmpacheListView
{

	public AmpacheListHandler mDataHandler;

	private MediaCollectionAdapter mediaCollectionAdapter;
	private ArrayList<ampacheObject> ampacheObjectList;
	private Directive directive = null;
	private IsFetchingListener isFetchingListener;
	private LinkedList<Directive> history = new LinkedList<Directive>();
	public int backOffset = 0;

	public VerticalAmpacheListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();

		mDataHandler = new AmpacheListHandler(this);
		ampacheObjectList = new ArrayList<ampacheObject>();
		mediaCollectionAdapter = new MediaCollectionAdapter(getContext(), R.layout.browsable_item,
				ampacheObjectList);
		setAdapter(mediaCollectionAdapter);
		setOnItemClickListener(this);
		setOnItemLongClickListener(this);
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
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3)
	{

		ampacheObject val = null;

		val = (ampacheObject) adapterView.getItemAtPosition(position);
		if (val == null)
			return;
		if (val.getType().equals("Song")) {
			Toast.makeText(getContext(), "Enqueue " + val.getType() + ": " + val.toString(),
					Toast.LENGTH_LONG).show();
			amdroid.playbackControl.addPlaylistCurrent((Song) val);
			return;
		}
		Directive directive = new Directive(val.childAction(), val.id, val.name);

		mDataHandler.enqueMessage(AmpacheListHandler.AMPACHE_INIT_REQUEST, directive, 0, true);

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long arg3)
	{
		ampacheObject cur = (ampacheObject) adapterView.getItemAtPosition(position);
		Toast.makeText(getContext(), "Enqueue " + cur.getType() + ": " + cur.toString(),
				Toast.LENGTH_LONG).show();
		if (cur.hasChildren()) {
			mDataHandler.enqueMessage(AmpacheListHandler.ENQUEUE_SONG, cur.getAllChildrenDirective(), 0, false);
		} else {
			amdroid.playbackControl.addPlaylistCurrent((Media) cur);
		}
		return true;
	}

	public void setIsFetchingListener(IsFetchingListener listener)
	{
		isFetchingListener = listener;
	}

	public boolean backPressed()
	{
		boolean ret = false;
		if (history.size() > 1) {
			history.removeLast();
			try {
				directive = history.getLast().clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			mDataHandler.enqueMessage(AmpacheListHandler.AMPACHE_INIT_REQUEST, directive, 0, false);
			ret = true;
		} else if (history.size() == 1 - backOffset) {
			history.removeLast();
			mediaCollectionAdapter.clear();
			mDataHandler.stopIncFetch = true;
			mDataHandler.setIsFetching(false);
			ret = true;
		}

		return ret;
	}

	@Override
	public void onIsFetchingChange(boolean isFetching)
	{
		if (isFetchingListener != null) {
			isFetchingListener.onIsFetchingChange(isFetching);
		}
	}

	@Override
	public void onEnqueMessage(int what, Directive directive, int startIndex, boolean addHistory)
	{
		if (addHistory) {
			try {
				history.add(directive.clone());
			} catch (CloneNotSupportedException e) {
				Log.e(View.VIEW_LOG_TAG, "Cloning of directive failed.", e);
			}
		}
	}

	@Override
	public void onClearAmpacheObjects()
	{
		mediaCollectionAdapter.clear();
	}

	@Override
	public void onAddAmpacheObjects(ArrayList<ampacheObject> ampacheObjects)
	{
		ampacheObjectList.addAll(ampacheObjects);
		mediaCollectionAdapter.notifyDataSetChanged();
	}

	@Override
	public void enqueRequest(Directive directive)
	{
		mDataHandler.enqueMessage(0x1336, directive, 0, true);
	}
}

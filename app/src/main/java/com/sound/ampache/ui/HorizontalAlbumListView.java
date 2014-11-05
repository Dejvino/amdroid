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

import android.app.Fragment;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

import com.sound.ampache.MediaCollectionAdapter;
import com.sound.ampache.R;
import com.sound.ampache.amdroid;
import com.sound.ampache.objects.Album;
import com.sound.ampache.objects.Directive;
import com.sound.ampache.objects.Media;
import com.sound.ampache.objects.ampacheObject;
import com.sound.ampache.utility.HorizontalListView;

import java.util.ArrayList;

public class HorizontalAlbumListView extends HorizontalListView implements OnItemClickListener,
		OnItemLongClickListener, AmpacheListView
{

	private AmpacheListHandler mDataHandler;
	private MediaCollectionAdapter mediaCollectionAdapter;
	private ArrayList<ampacheObject> ampacheObjectList;
	private Directive directive = null;
	private IsFetchingListener isFetchingListener;
	private OnAmpacheObjectClickListener<Album> onAmpacheObjectClickListener;

	public HorizontalAlbumListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();

		mDataHandler = new AmpacheListHandler(this);
		ampacheObjectList = new ArrayList<ampacheObject>();
		mediaCollectionAdapter = new MediaCollectionAdapter(getContext(), R.layout.dashboard_album_item,
				ampacheObjectList);
		setAdapter(mediaCollectionAdapter);
		setOnItemClickListener(this);
		setOnItemLongClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3)
	{
		if (onAmpacheObjectClickListener != null) {
			onAmpacheObjectClickListener.onAmpacheObjectClick((Album) mAdapter.getItem(position));
		}
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

	@Override
	public void onIsFetchingChange(boolean isFetching)
	{
		if (isFetchingListener != null) {
			isFetchingListener.onIsFetchingChange(isFetching);
		}
	}

	public void setOnAmpacheObjectClickListener(OnAmpacheObjectClickListener<Album> onAmpacheObjectClickListener)
	{
		this.onAmpacheObjectClickListener = onAmpacheObjectClickListener;
	}

	@Override
	public void onEnqueMessage(int what, Directive directive, int startIndex, boolean addHistory)
	{
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

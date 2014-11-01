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

import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sound.ampache.AmpacheListView.IsFetchingListener;
import com.sound.ampache.objects.Directive;

import java.util.LinkedList;
import java.util.ListIterator;


public class PlaylistsFragment extends Fragment implements IsFetchingListener
{

	private AmpacheListView ampacheListView;
	private TextView emptyTextView;

	private ProgressBar progressBar;
	private TextView headerTextView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.playlists_layout, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		emptyTextView = (TextView) view.findViewById(android.R.id.empty);
		emptyTextView.setText("<No playlists found>");

		ampacheListView = (AmpacheListView) view.findViewById(android.R.id.list);
		ampacheListView.setFastScrollEnabled(true);
		ampacheListView.setEmptyView(emptyTextView);
		ampacheListView.setHeaderDividersEnabled(true);
		ampacheListView.setIsFetchingListener(this);

		progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
		progressBar.setIndeterminate(true);
		progressBar.setVisibility(View.INVISIBLE);
		headerTextView = (TextView) view.findViewById(R.id.text_view);
		headerTextView.setText("Playlists");

		Directive directive = new Directive("playlists", "", "");

		ampacheListView.mDataHandler.enqueMessage(0x1336, directive, 0, true);

		ampacheListView.backOffset = 1;

		amdroid.networkClient.auth();
	}

	@Override
	public void onIsFetchingChange(boolean isFetching)
	{
		if (isFetching) {
			progressBar.setVisibility(View.VISIBLE);
		} else {
			progressBar.setVisibility(View.INVISIBLE);
		}
		updateHeaderTextView();
	}

	private void updateHeaderTextView()
	{
		String append = "Playlists";
		LinkedList<Directive> history = ampacheListView.getHistory();

		ListIterator<Directive> itr = history.listIterator();
		//Increment once to remove the empty history field
		itr.next();
		while (itr.hasNext()) {
			append += "/" + itr.next().args[2];
		}

		headerTextView.setText(append);
	}

	/*
	 * Override "back button" behavior on android 1.6
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// Take care of calling this method on earlier versions of
			// the platform where it doesn't exist.
			return ampacheListView.backPressed();
		}

		return false;
	}

}

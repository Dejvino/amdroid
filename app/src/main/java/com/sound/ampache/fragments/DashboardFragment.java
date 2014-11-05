package com.sound.ampache.fragments;

/* Copyright (c) 2008-2009  Kevin James Purdy <purdyk@gmail.com>                                              
 * Copyright (c) 2010       Krisopher Heijari <iif.ftw@gmail.com>
 * Copyright (c) 2014       David Hrdina Nemecek <dejvino@gmail.com>
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
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.sound.ampache.net.AmpacheApiAction;
import com.sound.ampache.objects.Album;
import com.sound.ampache.objects.Directive;
import com.sound.ampache.ui.AmpacheListView;
import com.sound.ampache.ui.FetchingProgressBarListener;
import com.sound.ampache.ui.HorizontalAlbumListView;
import com.sound.ampache.R;
import com.sound.ampache.amdroid;
import com.sound.ampache.ui.OnAmpacheObjectClickListener;
import com.sound.ampache.ui.VerticalAmpacheListView;

public class DashboardFragment extends Fragment
{
	private View rootView;

	private HorizontalAlbumListView randomAlbumListView;
	private HorizontalAlbumListView recentAlbumListView;
	//private VerticalAmpacheListView randomAlbumListView;
	//private VerticalAmpacheListView recentAlbumListView;


	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.dashboard_layout, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		rootView = view;

		amdroid.networkClient.auth();

		OnAmpacheObjectClickListener<Album> onAlbumClickListener = new OnAmpacheObjectClickListener<Album>()
		{
			@Override
			public void onAmpacheObjectClick(Album object)
			{
				Directive directive = new Directive(AmpacheApiAction.ALBUM_SONGS, object.getId(), object.name);
				Fragment newFragment = new BrowseFragment();
				Bundle arguments = new Bundle();
				arguments.putParcelable(BrowseFragment.ARGUMENTS_KEY_DIRECTIVE, directive);
				newFragment.setArguments(arguments);
				if (getFragmentManager() == null) {
					throw new RuntimeException("Cannot get fragment manager.");
				}
				getFragmentManager().beginTransaction()
					.replace(R.id.mainContent, newFragment)
					.commit();
			}
		};

		randomAlbumListView = (HorizontalAlbumListView) rootView.findViewById(R.id.random_albums);
		randomAlbumListView.setIsFetchingListener(new FetchingProgressBarListener(rootView, R.id.random_albums_progress_bar));
		randomAlbumListView.enqueRequest(new Directive(AmpacheApiAction.STATS, "random", "Random albums"));
		randomAlbumListView.setOnAmpacheObjectClickListener(onAlbumClickListener);
		//randomAlbumListView.setEmptyView(browseListView);

		recentAlbumListView = (HorizontalAlbumListView) rootView.findViewById(R.id.recent_albums);
		recentAlbumListView.setIsFetchingListener(new FetchingProgressBarListener(rootView, R.id.recent_albums_progress_bar));
		recentAlbumListView.enqueRequest(new Directive(AmpacheApiAction.STATS, "recent", "Recent albums"));
		recentAlbumListView.setOnAmpacheObjectClickListener(onAlbumClickListener);
		//randomAlbumListView.setEmptyView(browseListView);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		menu.clear();
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.dash_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.dash_menu_preferences:
				Intent intent = new Intent().setClass(getActivity(), PreferencesFragment.class);
				if (intent != null)
					startActivity(intent);
				break;
			default:
				return false;
		}
		return true;
	}
}


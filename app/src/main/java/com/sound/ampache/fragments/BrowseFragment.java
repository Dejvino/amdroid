package com.sound.ampache.fragments;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sound.ampache.R;
import com.sound.ampache.ui.AmpacheListView;
import com.sound.ampache.ui.VerticalAmpacheListView;
import com.sound.ampache.amdroid;
import com.sound.ampache.net.AmpacheApiAction;
import com.sound.ampache.objects.Directive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

public class BrowseFragment extends Fragment implements OnItemClickListener, AmpacheListView.IsFetchingListener
{
	public static final String ARGUMENTS_KEY_DIRECTIVE = "directive";

	private View rootView;

	// Root list and adapter. This is only used to display the root options.
	private ListView browseListView;
	private ArrayList<AmpacheApiAction> browseList = new ArrayList<AmpacheApiAction>(Arrays.asList(
				new AmpacheApiAction[] { AmpacheApiAction.ALBUMS, AmpacheApiAction.ARTISTS,
						/*AmpacheApiAction.PLAYLISTS,*/ AmpacheApiAction.TAGS, AmpacheApiAction.VIDEOS }
			));
	private ArrayAdapter<AmpacheApiAction> browseListAdapter;

	private VerticalAmpacheListView ampacheListView;

	private ProgressBar progressBar;
	private TextView headerTextView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.browse_layout, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		rootView = view;

		browseListAdapter = new ArrayAdapter<AmpacheApiAction>(getActivity(), R.layout.list_item_music_root, browseList);
		browseListView = (ListView) rootView.findViewById(android.R.id.empty);
		browseListView.setAdapter(browseListAdapter);
		browseListView.setOnItemClickListener(this);

		ampacheListView = (VerticalAmpacheListView) rootView.findViewById(android.R.id.list);
		ampacheListView.setFastScrollEnabled(true);
		ampacheListView.setEmptyView(browseListView);
		ampacheListView.setHeaderDividersEnabled(true);
		ampacheListView.setIsFetchingListener(this);

		progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
		progressBar.setIndeterminate(true);
		progressBar.setVisibility(View.INVISIBLE);
		headerTextView = (TextView) rootView.findViewById(R.id.text_view);
		headerTextView.setText("Music");

		amdroid.networkClient.auth();

		if (getArguments() != null) {
			Directive initialDirective = (Directive) getArguments().getParcelable(ARGUMENTS_KEY_DIRECTIVE);
			if (initialDirective != null) {
				openDirective(initialDirective);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3)
	{
		AmpacheApiAction value = (AmpacheApiAction) adapterView.getItemAtPosition(position);

		Directive directive = new Directive(value, "", value.getName());

		openDirective(directive);
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
		String append = "Music";
		LinkedList<Directive> history = ampacheListView.getHistory();

		ListIterator<Directive> itr = history.listIterator();
		while (itr.hasNext()) {
			append += " > " + itr.next().name;
		}

		headerTextView.setText(append);
	}

	private void openDirective(Directive directive)
	{
		browseListView.setVisibility(View.GONE);
		ampacheListView.enqueRequest(directive);
	}
}

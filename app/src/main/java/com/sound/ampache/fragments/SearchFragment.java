package com.sound.ampache.fragments;

/* Copyright (c) 2008-2009 	Kevin James Purdy <purdyk@gmail.com>
 * Copyright (c) 2010 		Krisopher Heijari <iif.ftw@gmail.com>
 * Copyright (c) 2010           Jacob Alexander   < haata@users.sf.net >
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.sound.ampache.R;
import com.sound.ampache.ui.AmpacheListView;
import com.sound.ampache.ui.VerticalAmpacheListView;
import com.sound.ampache.net.AmpacheApiAction;
import com.sound.ampache.objects.Directive;

public final class SearchFragment extends Fragment implements AmpacheListView.IsFetchingListener, OnClickListener
{

	private Spinner searchCriteria;
	private EditText searchString;

	private VerticalAmpacheListView ampacheListView;
	private TextView emptyTextView;

	private ProgressBar progressBar;
	private TextView headerTextView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.search_activity, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		searchCriteria = (Spinner) view.findViewById(R.id.search_spinner);
		ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.search,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		searchCriteria.setAdapter(adapter);
		searchString = (EditText) view.findViewById(R.id.search_text);

		// Bind clicklistener for our search button
		((ImageButton) view.findViewById(R.id.search_button)).setOnClickListener(this);

		emptyTextView = (TextView) view.findViewById(android.R.id.empty);
		emptyTextView.setText("<No search results>");

		ampacheListView = (VerticalAmpacheListView) view.findViewById(android.R.id.list);
		ampacheListView.setFastScrollEnabled(true);
		ampacheListView.setEmptyView(emptyTextView);
		ampacheListView.setHeaderDividersEnabled(true);
		ampacheListView.setIsFetchingListener(this);

		progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
		progressBar.setIndeterminate(true);
		progressBar.setVisibility(View.INVISIBLE);
		headerTextView = (TextView) view.findViewById(R.id.text_view);
		headerTextView.setText("Search results");
	}

	@Override
	public void onClick(View v)
	{
		String searchQuery = searchString.getText().toString();
		if (searchQuery.length() <= 0)
			return;

		final AmpacheApiAction action;

		String spinnerValue = (String) searchCriteria.getSelectedItem();
		if (spinnerValue.equals("All"))
			action = AmpacheApiAction.SEARCH_SONGS;
		else if (spinnerValue.equals("Artists"))
			action = AmpacheApiAction.ARTISTS;
		else if (spinnerValue.equals("Albums"))
			action = AmpacheApiAction.ALBUMS;
		else if (spinnerValue.equals("Tags"))
			action = AmpacheApiAction.TAGS;
		else if (spinnerValue.equals("Songs"))
			action = AmpacheApiAction.SONGS;
		else
			throw new RuntimeException("Unhandled search spinner value: " + spinnerValue);

		// Clear history when searching, we should only be able to go back if a search result has
		// been clicked.
		ampacheListView.clearHistory();
		ampacheListView.enqueRequest(new Directive(action, searchQuery, searchQuery));

	}

	@Override
	public void onIsFetchingChange(boolean isFetching)
	{
		if (isFetching) {
			progressBar.setVisibility(View.VISIBLE);
		} else {
			progressBar.setVisibility(View.INVISIBLE);
		}
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

package com.sound.ampache;

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

import java.net.URLEncoder;

import com.sound.ampache.AmpacheListView.IsFetchingListener;
import com.sound.ampache.objects.Directive;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public final class SearchFragment extends Fragment implements IsFetchingListener, OnClickListener {

	private Spinner searchCriteria;
	private EditText searchString;

	private AmpacheListView ampacheListView;
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

		searchCriteria = (Spinner) view.findViewById( R.id.search_spinner );
		ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.search,
				android.R.layout.simple_spinner_item );
		adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		searchCriteria.setAdapter( adapter );
		searchString = (EditText) view.findViewById( R.id.search_text );

		// Bind clicklistener for our search button
		( (ImageButton) view.findViewById( R.id.search_button ) ).setOnClickListener( this );

		emptyTextView = (TextView) view.findViewById( android.R.id.empty );
		emptyTextView.setText( "<No search results>" );

		ampacheListView = (AmpacheListView) view.findViewById( android.R.id.list );
		ampacheListView.setFastScrollEnabled( true );
		ampacheListView.setEmptyView( emptyTextView );
		ampacheListView.setHeaderDividersEnabled( true );
		ampacheListView.setIsFetchingListener( this );

		progressBar = (ProgressBar) view.findViewById( R.id.progress_bar );
		progressBar.setIndeterminate( true );
		progressBar.setVisibility( View.INVISIBLE );
		headerTextView = (TextView) view.findViewById( R.id.text_view );
		headerTextView.setText( "Search results" );
	}

	@Override
	public void onClick( View v )
	{
		String string = searchString.getText().toString();
		if ( string.length() <= 0 )
			return;

		String[] directive = new String[3];

		String spinnerValue = (String)searchCriteria.getSelectedItem();
		if ( spinnerValue.equals( "All" ) )
			directive[0] = "search_songs";
		else if ( spinnerValue.equals( "Artists" ) )
			directive[0] = "artists";
		else if ( spinnerValue.equals( "Albums" ) )
			directive[0] = "albums";
		else if ( spinnerValue.equals( "Tags" ) )
			directive[0] = "tags";
		else if ( spinnerValue.equals( "Songs" ) )
			directive[0] = "songs";
		else
			return;

		try
		{
			directive[1] = URLEncoder.encode( string, "UTF-8" );
		} catch ( Exception poo )
		{
			return;
		}

		directive[2] = string;

		// Clear history when searching, we should only be able to go back if a search result has
		// been clicked.
		ampacheListView.clearHistory();
		ampacheListView.mDataHandler.enqueMessage( 0x1336, new Directive(directive), 0, true );

	}

	@Override
	public void onIsFetchingChange( boolean isFetching )
	{
		if ( isFetching )
		{
			progressBar.setVisibility( View.VISIBLE );
		} else
		{
			progressBar.setVisibility( View.INVISIBLE );
		}
	}


	/*
	 * Override "back button" behavior on android 1.6
	 */
	public boolean onKeyDown( int keyCode, KeyEvent event )
	{
		if ( keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 )
		{
			// Take care of calling this method on earlier versions of
			// the platform where it doesn't exist.
			return ampacheListView.backPressed();
		}

		return false;
	}

}

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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

import com.sound.ampache.AmpacheListView.IsFetchingListener;
import com.sound.ampache.net.NetworkWorker;
import com.sound.ampache.objects.Directive;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class BrowseFragment extends Fragment implements OnItemClickListener, IsFetchingListener
{
	private View rootView;

	// Root list and adapter. This is only used to display the root options.
	private ListView emptyListView;
	private ArrayList<String> emptyList = new ArrayList<String>( Arrays.asList( new String[] {
			"Artists", "Albums", "Tags", "Videos" } ) );
	private ArrayAdapter<String> emptyListAdapter;

	private AmpacheListView ampacheListView;
	
	private ProgressBar progressBar;
	private TextView headerTextView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.browse_layout, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		rootView = view;

		emptyListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_music_root, emptyList);
		emptyListView = (ListView) rootView.findViewById(android.R.id.empty);
		emptyListView.setAdapter( emptyListAdapter );
		emptyListView.setOnItemClickListener( this );

		ampacheListView = (AmpacheListView) rootView.findViewById(android.R.id.list);
		ampacheListView.setFastScrollEnabled( true );
		ampacheListView.setEmptyView( emptyListView );
		ampacheListView.setHeaderDividersEnabled( true );
		ampacheListView.setIsFetchingListener( this );

		progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
		progressBar.setIndeterminate( true );
		progressBar.setVisibility( View.INVISIBLE );
		headerTextView = (TextView) rootView.findViewById(R.id.text_view);
		headerTextView.setText( "Music" );

		amdroid.networkClient.auth();
	}

	@Override
	public void onItemClick( AdapterView<?> adapterView, View view, int position, long arg3 )
	{
		String value;
		value = (String)adapterView.getItemAtPosition( position );
		value = value.toLowerCase();

		Directive directive = new Directive(value, "", value);
		
		emptyListView.setVisibility( View.GONE );

		ampacheListView.mDataHandler.enqueMessage( 0x1336, directive, 0, true );

	}

	@Override
	public void onIsFetchingChange( boolean isFetching )
	{
		if (isFetching) {
			progressBar.setVisibility( View.VISIBLE );
		}
		else {
			progressBar.setVisibility( View.INVISIBLE );
		}
		
		updateHeaderTextView();
		
	}

	private void updateHeaderTextView()
	{
		String append = "Music";
		LinkedList<Directive> history = ampacheListView.getHistory();

		ListIterator<Directive> itr = history.listIterator();
	    while(itr.hasNext())
	    {
	      append += "/"+itr.next().args[2];
	    }
	    
	    headerTextView.setText( append );
	}
}

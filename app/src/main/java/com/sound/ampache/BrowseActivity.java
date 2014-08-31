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

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class BrowseActivity extends Activity implements OnItemClickListener, IsFetchingListener {

	// Root list and adapter. This is only used to display the root options.
	private ListView emptyListView;
	private ArrayList<String> emptyList = new ArrayList<String>( Arrays.asList( new String[] {
			"Artists", "Albums", "Tags", "Videos" } ) );
	private ArrayAdapter<String> emptyListAdapter;

	private AmpacheListView ampacheListView;
	
	private ProgressBar progressBar;
	private TextView headerTextView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.browse_activity );

		emptyListAdapter = new ArrayAdapter<String>( this, R.layout.list_item_music_root, emptyList );
		emptyListView = (ListView)findViewById( android.R.id.empty );
		emptyListView.setAdapter( emptyListAdapter );
		emptyListView.setOnItemClickListener( this );

		ampacheListView = (AmpacheListView)findViewById( android.R.id.list );
		ampacheListView.setFastScrollEnabled( true );
		ampacheListView.setEmptyView( emptyListView );
		ampacheListView.setHeaderDividersEnabled( true );
		ampacheListView.setIsFetchingListener( this );
		
		progressBar = (ProgressBar)findViewById(R.id.progress_bar);
		progressBar.setIndeterminate( true );
		progressBar.setVisibility( View.INVISIBLE );
		headerTextView = (TextView)findViewById(R.id.text_view);
		headerTextView.setText( "Music" );
		
	}

	@Override
	public void onItemClick( AdapterView<?> adapterView, View view, int position, long arg3 )
	{
		String value;
		value = (String)adapterView.getItemAtPosition( position );
		value = value.toLowerCase();

		String[] directive = new String[3];
		directive[0] = value;
		directive[1] = "";
		directive[2] = value;
		
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
		LinkedList<String[]> history = ampacheListView.getHistory();

		ListIterator<String[]> itr = history.listIterator();
	    while(itr.hasNext())
	    {
	      append += "/"+itr.next()[2];
	    }
	    
	    headerTextView.setText( append );
	}
	
    /*
     * Override "back button" behavior on android 1.6
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // Take care of calling this method on earlier versions of
            // the platform where it doesn't exist.
            return ampacheListView.backPressed();
        }

        return false;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    /*
     * Override "back button" behavior on android 2.0 and later
     *
    public void onBackPressed() {
        ampacheListView.backPressed();
    }*/

}

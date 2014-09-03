package com.sound.ampache;

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

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DashboardFragment extends Fragment
{
    private View rootView;

    protected ArrayList<String> songs = new ArrayList<String>(Arrays.asList(new String[]{"Song 1", "Song 2", "Song 3", "Song 4", "Song 5", "Song 6"}));
    
    protected ListView lv;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dashboard_layout, container, false);
    }

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		rootView = view;
		lv = (ListView) rootView.findViewById(R.id.recently_played_songs);
		lv.setAdapter(new ArrayAdapter(getActivity(), R.layout.list_item_music_root , songs));

		amdroid.networkClient.auth();
	}

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.dash_menu, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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


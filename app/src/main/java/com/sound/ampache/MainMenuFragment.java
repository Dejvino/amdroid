package com.sound.ampache;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.sound.ampache.objects.UserLogEntry;
import com.sound.ampache.service.AbstractUserLoggerListener;

/* Copyright (c) 2014 David Hrdina Nemecek <dejvino@gmail.com>
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

/**
 * Description: Fragment for the main menu.
 *
 * @author Dejvino
 */
public class MainMenuFragment extends Fragment implements View.OnClickListener
{
	//Identifiers for the different activities. The id's can be anything as longs as they are unique.
	public static final String GOTO_HOME = "goto_home";
	public static final String GOTO_MUSIC = "goto_music";
	public static final String GOTO_PLAYLISTS = "goto_playlists";
	public static final String GOTO_SEARCH = "goto_search";
	public static final String GOTO_PLAYING = "goto_playing";
	public static final String GOTO_PREFS = "goto_prefs";
	public static final String GOTO_LOGS = "goto_logs";

	private MiniPlayer miniPlayer;
	private Button logsButton;
	private UserLogEntry.Severity highestLogsSeverity = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.main_menu_layout, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		miniPlayer = (MiniPlayer) getFragmentManager().findFragmentById(R.id.mini_player);

        /*  Bind onclicklisteners to our buttons in menuview_laout.xml */
		ImageButton b = (ImageButton) view.findViewById(R.id.goto_home);
		b.setOnClickListener(this);
		b = (ImageButton) view.findViewById(R.id.goto_music);
		b.setOnClickListener(this);
		b = (ImageButton) view.findViewById(R.id.goto_playlists);
		b.setOnClickListener(this);
		b = (ImageButton) view.findViewById(R.id.goto_playing);
		b.setOnClickListener(this);
		b = (ImageButton) view.findViewById(R.id.goto_search);
		b.setOnClickListener(this);
		b = (ImageButton) view.findViewById(R.id.goto_preferences);
		b.setOnClickListener(this);

		logsButton = (Button) view.findViewById(R.id.goto_logs);
		logsButton.setOnClickListener(this);
		amdroid.logger.addLogListener(new AbstractUserLoggerListener()
		{
			@Override
			public void onLogEntry(UserLogEntry logEntry)
			{
				if (highestLogsSeverity == null || highestLogsSeverity.compareTo(logEntry.severity) < 0) {
					highestLogsSeverity = logEntry.severity;
				}
				switch (highestLogsSeverity) {
					case DEBUG:
						logsButton.setText(".");
						break;
					case INFO:
						logsButton.setText(":");
						break;
					case WARNING:
						logsButton.setText("!");
						break;
					case CRITICAL:
						logsButton.setText("!!");
						break;
					default:
						throw new RuntimeException("Unhandled severity value: " + highestLogsSeverity);
				}
			}
		});

		setActivity(GOTO_HOME);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId()) {
			case (R.id.goto_home):
				setActivity(GOTO_HOME);
				break;

			case (R.id.goto_music):
				setActivity(GOTO_MUSIC);
				break;

			case (R.id.goto_playlists):
				setActivity(GOTO_PLAYLISTS);
				break;

			case (R.id.goto_playing):
				setActivity(GOTO_PLAYING);
				break;

			case (R.id.goto_search):
				setActivity(GOTO_SEARCH);
				break;

			case (R.id.goto_preferences):
				setActivity(GOTO_PREFS);
				break;

			case (R.id.goto_logs):
				setActivity(GOTO_LOGS);
				break;

			default:
				break;
		}

	}

	public void setActivity(String id)
	{
		Fragment newFragment;
		if (GOTO_HOME.equals(id)) {
			newFragment = new DashboardFragment();
		} else if (GOTO_MUSIC.equals(id)) {
			newFragment = new BrowseFragment();
		} else if (GOTO_PLAYLISTS.equals(id)) {
			newFragment = new PlaylistsFragment();
		} else if (GOTO_PLAYING.equals(id)) {
			newFragment = new PlaylistFragment();
		} else if (GOTO_SEARCH.equals(id)) {
			newFragment = new SearchFragment();
		} else if (GOTO_PREFS.equals(id)) {
			newFragment = new PreferencesFragment();
		} else if (GOTO_LOGS.equals(id)) {
			newFragment = new LogsFragment();
			logsButton.setText("");
			highestLogsSeverity = null;
		} else {
			throw new RuntimeException("Unknown activity: " + id);
		}

		if (getFragmentManager() == null) {
			throw new RuntimeException("Cannot get fragment manager.");
		}
		getFragmentManager().beginTransaction()
				.replace(R.id.mainContent, newFragment)
				.commit();
	}
}

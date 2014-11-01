package com.sound.ampache.utility;

/* Copyright (c) 2010 Jacob Alexander < haata@users.sf.net >
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

import android.util.Log;

import com.sound.ampache.objects.Media;

import java.util.ArrayList;
import java.util.Random;

public class Playlist extends ArrayList<Media>
{

	private ArrayList<Integer> shuffleHistory = new ArrayList<Integer>();

	private int currentIndex = 0;

	private boolean shuffleEnabled = false;
	private boolean repeatEnabled = false;

	private final static String LOG_TAG = "Amdroid_Playlist";


	// Constructors **************************************************

	public Playlist()
	{
	}

	public Playlist(int startingIndex)
	{
		setCurrentIndex(startingIndex);
	}

	public Playlist(int startingIndex, boolean shuffle, boolean repeat)
	{
		setCurrentIndex(startingIndex);
		setShufflePlay(shuffle);
		setRepeatPlay(repeat);
	}


	// Gets **********************************************************

	public int getCurrentIndex()
	{
		return currentIndex;
	}

	public Media getCurrentMedia()
	{
		if (size() <= 0) {
			return null;
		}
		return super.get(currentIndex);
	}

	public boolean getShufflePlay()
	{
		return shuffleEnabled;
	}

	public boolean getRepeatPlay()
	{
		return repeatEnabled;
	}


	// Sets **********************************************************

	// Checks bounds before setting, if invalid the index is set to 0; returns the Media at the index
	public Media setCurrentIndex(int index)
	{
		if (index >= super.size() || index < 0)
			currentIndex = 0;
		else
			currentIndex = index;

		return super.get(currentIndex);
	}

	public void setShufflePlay(boolean randomize)
	{
		clearShuffleHistory();
		shuffleEnabled = randomize;
	}

	public void setRepeatPlay(boolean loop)
	{
		repeatEnabled = loop;
	}


	// Functions *****************************************************

	public void clearShuffleHistory()
	{
		shuffleHistory.clear();
	}

	public void clearPlaylist()
	{
		super.clear();
		currentIndex = 0;
	}

	// Returns the next media to play, null if finished the playlist
	public Media next()
	{
		Log.d(LOG_TAG, "Next Media");
		if (shuffleEnabled) {
			// So we don't play a media more than once
			if (!shuffleHistory.contains(currentIndex))
				shuffleHistory.add(currentIndex);

			// Just played the last media, repeat if repeat is enabled, stop otherwise
			if (shuffleHistory.size() >= super.size() && repeatEnabled)
				shuffleHistory.clear();
			else {
				currentIndex = 0;
				return null;
			}

			int next = 0;
			Random rand = new Random();

			// Try random numbers until finding one that is not used
			do {
				next = rand.nextInt(super.size());
			} while (shuffleHistory.contains(next));

			// Set next playing index
			currentIndex = next;
		} else {
			// move to the next media
			if ((currentIndex + 1) >= super.size()) {
				if (repeatEnabled) {
					// cycle back to the start when in repeat mode
					currentIndex = 0;
				} else {
					// nowhere to go
				}
			} else {
				currentIndex++;
			}
		}

		// Index is within the list bounds
		if (currentIndex >= 0 && currentIndex < super.size())
			return super.get(currentIndex);

		return null;
	}

	public Media prev()
	{
		if (shuffleEnabled) {
			int prevIndex = shuffleHistory.indexOf(currentIndex);

			// Call a random next media if this is the first media
			if (shuffleHistory.size() < 1)
				return next();

			// Previous (Current item is not in the shuffle history)
			if (prevIndex == -1) {
				// Set previous media
				currentIndex = (Integer) shuffleHistory.get(shuffleHistory.size() - 1);

				// Remove item, I consider Previous like an undo
				shuffleHistory.remove(shuffleHistory.size() - 1);
			}
			// This shouldn't be possible, but...
			else if (prevIndex > 0) {
				currentIndex = (Integer) shuffleHistory.get(prevIndex - 1);

				shuffleHistory.remove(prevIndex);
			}
		}
		// Do not call previous if it is the first media
		else if (currentIndex > 0) {
			currentIndex--;
		}

		// Index is within the list bounds
		if (currentIndex >= 0 && currentIndex < super.size())
			return super.get(currentIndex);

		return null;
	}
}


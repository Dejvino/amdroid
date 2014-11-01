package com.sound.ampache.service;

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
 * Description: PlayerService status update listener.
 *
 * @author Dejvino
 * @since 2014-09-06
 */
public interface PlayerServiceStatusListener
{
	void onServiceConnected();

	void onServiceDisconnected();

	void onSeek(int position);

	void onBuffering(int buffer);

	void onNewMedia();

	void onPlaylistIndexChanged(int index);

	void onShuffledChanged(int enabled);

	void onRepeatChanged(int enabled);

	void onPlay();

	void onPause();

	void onStop();

	void onVideoSizeChanged(int width, int height);

	void onPlaylistChanged(int size);

	void onError(int what, int extra);
}

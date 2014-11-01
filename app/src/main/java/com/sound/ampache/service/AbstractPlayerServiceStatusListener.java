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

import android.os.Handler;
import android.os.Messenger;

/**
 * Description: Convenience class for implementing the PlayerServiceStatusListener interface.
 *
 * @author Dejvino
 * @since 2014-09-06
 */
public abstract class AbstractPlayerServiceStatusListener extends Handler implements PlayerServiceStatusListener
{
	protected final Messenger messenger = new Messenger(this);

	@Override
	public void onServiceConnected()
	{

	}

	@Override
	public void onServiceDisconnected()
	{

	}

	@Override
	public void onSeek(int position)
	{

	}

	@Override
	public void onBuffering(int buffer)
	{

	}

	@Override
	public void onNewMedia()
	{

	}

	@Override
	public void onPlaylistIndexChanged(int index)
	{

	}

	@Override
	public void onShuffledChanged(int enabled)
	{

	}

	@Override
	public void onRepeatChanged(int enabled)
	{

	}

	@Override
	public void onPlay()
	{

	}

	@Override
	public void onPause()
	{

	}

	@Override
	public void onStop()
	{

	}

	@Override
	public void onVideoSizeChanged(int width, int height)
	{

	}

	@Override
	public void onPlaylistChanged(int size)
	{

	}

	@Override
	public void onError(int what, int extra)
	{

	}
}

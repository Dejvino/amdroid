package com.sound.ampache;

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

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;

import com.sound.ampache.net.NetworkWorker;

/**
 * Description:
 *
 * @author Dejvino
 * @since 2014-08-31
 */
public class GlobalNetworkClient
{
	private Context ctx;
	private NetworkWorker worker;

	public GlobalNetworkClient(Context ctx)
	{
		this.ctx = ctx;
		this.worker = new NetworkWorker(ctx);
		worker.start();
	}

	/**
	 * Performs an authorization request.
	 */
	public void auth()
	{
		// register playback result listener
		Messenger replyTo = new Messenger(new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				super.handleMessage(msg);
				String authToken = msg.getData().getString(NetworkWorker.KEY_AUTH_TOKEN);
				amdroid.playbackControl.setAuthToken(authToken);
			}
		});

		Message msg = new Message();
		msg.getData().putSerializable(NetworkWorker.KEY_OPERATION, NetworkWorker.Operation.AUTH);
		msg.replyTo = replyTo;
		sendMessage(msg);
	}

	/**
	 * Low-level message sending method.
	 *
	 * @param message
	 */
	public void sendMessage(Message message)
	{
		worker.postMessage(message);
	}
}

package com.sound.ampache.net;

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
import android.content.SharedPreferences;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sound.ampache.R;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Description: Worker (thread) responsible for handling network communications.
 *
 * @author Dejvino
 * @since 2014-08-31
 */
public class NetworkWorker
{
	private static final String LOG_TAG = "Ampache_Android_NetworkService";

	public static final String KEY_OPERATION = "operation";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_AUTH_TOKEN = "auth_token";
	public static final String KEY_REQUEST = "original_request_message";

	public static final String KEY_TERMINATE = "network_worker_terminate";

	public enum Operation
	{
		PING,
		AUTH,
		SEND,
	}

	private final Context ctx;

	private SharedPreferences prefs;
	private ampacheCommunicator comm;
	private ampacheCommunicator.ampacheRequestHandler requestHandler;

	private BlockingDeque<Message> messageQueue = new LinkedBlockingDeque<Message>();
	private WorkerThread workerThread;

	public NetworkWorker(Context ctx)
	{
		this.ctx = ctx;

		PreferenceManager.setDefaultValues(ctx, R.xml.preferences, false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

		try {
			comm = new ampacheCommunicator(prefs, ctx);
			requestHandler = comm.new ampacheRequestHandler();
			requestHandler.start();
		} catch (Exception e) {
			Log.d(LOG_TAG, "Network service failed to init.", e);
		}
	}

	/**
	 * Starts the networking worker.
	 * <p/>
	 * This internally forks a new thread to do the actual work in the background.
	 */
	public void start()
	{
		if (workerThread == null || !workerThread.isAlive()) {
			workerThread = new WorkerThread();
			workerThread.start();
		}
	}

	/**
	 * Adds a message to be processed by the worker.
	 *
	 * @param msg
	 */
	public void postMessage(Message msg)
	{
		messageQueue.offer(msg);
	}

	/**
	 * Worker thread for handling all the networking.
	 */
	private class WorkerThread extends Thread
	{
		private WorkerThread()
		{
			super(NetworkWorker.class.getSimpleName() + "." + WorkerThread.class.getSimpleName());
		}

		@Override
		public void run()
		{
			try {
				do {
					Message message = messageQueue.takeFirst();
					if (message == null || KEY_TERMINATE.equals(message.obj)) {
						Log.e(LOG_TAG, "Networking worker thread exiting as commanded.");
						break;
					}
					onHandleMessage(message);
				} while (true);
			} catch (Exception e) {
				Log.e(LOG_TAG, "Networking worker thread died unexpectedly.", e);
			}
		}

		private void onHandleMessage(Message message)
		{
			// determine if we pass the message on or handle it here
			Operation op = Operation.SEND;
			if (message.getData().containsKey(KEY_OPERATION)) {
				op = (Operation) message.getData().getSerializable(KEY_OPERATION);
			}

			Log.d(LOG_TAG, "Network thread processing: " + op.toString() + " (" + message.toString() + ")");
			Message reply = new Message();
			reply.getData().putParcelable(KEY_REQUEST, message);
			switch (op) {
				case AUTH:
					onAuth(message, reply);
					break;
				case SEND:
					onSend(message);
					break;
				default:
					throw new RuntimeException("Unhandled operation type: " + op.toString());
			}
			if (message.replyTo != null) {
				try {
					message.replyTo.send(reply);
				} catch (RemoteException e) {
					Log.d(LOG_TAG, "Failed sending response message.", e);
				}
			}
			Log.d(LOG_TAG, "Network thread finished: " + op.toString());
		}

		private void onAuth(Message request, Message reply)
		{
			// Check if we have a valid session, if not we try to establish one and then check again.
			if (comm.authToken == null || comm.authToken.equals("")) {
				comm.ping();
			}

			if (comm.authToken == null || comm.authToken.equals("")) {
				Log.e(LOG_TAG, "Connection problems: " + comm.lastErr);
			} else {
				Log.d(LOG_TAG, "Connection ok. Auth token: " + comm.authToken);
			}
			reply.getData().putString(KEY_AUTH_TOKEN, comm.authToken);
		}

		private void onSend(Message message)
		{
			requestHandler.incomingRequestHandler.sendMessage(message);
		}
	}
}

package com.sound.ampache.service;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.sound.ampache.objects.UserLogEntry;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
 * Logger utility with messages to be shown to the user.
 */
public class UserLogger
{
	private static final int LOGS_LIMIT = 20;
	private static final String LOG_TAG = "Amdroid_UserLogger";

	private LinkedList<UserLogEntry> logs = new LinkedList<UserLogEntry>();
	private Set<UserLoggerListener> logListeners = new HashSet<UserLoggerListener>();

	public UserLogger()
	{
	}

	public void addLog(UserLogEntry logEntry)
	{
		doAddLog(logEntry);
	}

	public List<UserLogEntry> getLogs()
	{
		return Collections.unmodifiableList(logs);
	}

	public void log(UserLogEntry entry)
	{
		addLog(entry);
	}

	public void log(UserLogEntry.Severity severity, String title)
	{
		log(severity, title, null);
	}

	public void log(UserLogEntry.Severity severity, String title, String details)
	{
		addLog(new UserLogEntry(severity, title, details));
	}

	public int size()
	{
		return logs.size();
	}

	public UserLogEntry get(int position)
	{
		return logs.get(position);
	}

	public void clear()
	{
		logs.clear();
	}

	public void addLogListener(UserLoggerListener logListener)
	{
		this.logListeners.add(logListener);
	}

	public void removeLogListener(UserLoggerListener logListener)
	{
		this.logListeners.remove(logListener);
	}

	/**
	 * Internal method to add log and notify listeners.
	 *
	 * @param logEntry
	 */
	private void doAddLog(UserLogEntry logEntry)
	{
		logs.addFirst(logEntry);
		try {
			Message msg = new Message();
			msg.obj = logEntry;
			mMessenger.send(msg);
		} catch (RemoteException e) {
			Log.d(LOG_TAG, "Local listener seems dead.", e);
		}
		if (logs.size() >= LOGS_LIMIT) {
			logs.removeLast();
		}
	}

	public void logDebug(String title)
	{
		log(UserLogEntry.Severity.DEBUG, title);
	}

	public void logInfo(String title)
	{
		log(UserLogEntry.Severity.INFO, title);
	}

	public void logWarning(String title)
	{
		log(UserLogEntry.Severity.WARNING, title);
	}

	public void logCritical(String title)
	{
		log(UserLogEntry.Severity.CRITICAL, title);
	}

	public void logDebug(String title, String details)
	{
		log(UserLogEntry.Severity.DEBUG, title, details);
	}

	public void logInfo(String title, String details)
	{
		log(UserLogEntry.Severity.INFO, title, details);
	}

	public void logWarning(String title, String details)
	{
		log(UserLogEntry.Severity.WARNING, title, details);
	}

	public void logCritical(String title, String details)
	{
		log(UserLogEntry.Severity.CRITICAL, title, details);
	}

	class IncomingHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			// pass message to listeners
			for (UserLoggerListener listener : logListeners) {
				listener.onLogEntry((UserLogEntry) msg.obj);
			}
		}
	}

	// Target for the incoming messages
	final Messenger mMessenger = new Messenger(new IncomingHandler());
}

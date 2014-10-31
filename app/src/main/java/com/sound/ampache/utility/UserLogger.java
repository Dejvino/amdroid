package com.sound.ampache.utility;

import com.sound.ampache.LogsFragment;
import com.sound.ampache.objects.UserLogEntry;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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

    private LinkedList<UserLogEntry> logs = new LinkedList<UserLogEntry>();
	private UserLoggerListener logListener;

	public UserLogger()
    {
    }

    public void addLog(UserLogEntry logEntry)
    {
        logs.addFirst(logEntry);
	    if (logListener != null) {
		    logListener.onLogEntry(logEntry);
	    }
        if (logs.size() >= LOGS_LIMIT) {
            logs.removeLast();
        }
    }

    public List<UserLogEntry> getLogs()
    {
        return Collections.unmodifiableList(logs);
    }

	public void log(UserLogEntry entry)
	{
		addLog(entry);
	}

    public void log(String title)
    {
        log(title, null);
    }

    public void log(String title, String details)
    {
        addLog(new UserLogEntry(title, details));
    }

    public int size() {
        return logs.size();
    }

    public UserLogEntry get(int position) {
        return logs.get(position);
    }

    public void clear() {
        logs.clear();
    }

	public void setLogListener(UserLoggerListener logListener) {
		this.logListener = logListener;
	}
}

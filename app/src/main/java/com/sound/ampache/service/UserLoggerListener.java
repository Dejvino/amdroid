package com.sound.ampache.service;

import com.sound.ampache.objects.UserLogEntry;

/**
 * Created by dejvino on 31.10.14.
 */
public interface UserLoggerListener
{
	void onLogEntry(UserLogEntry logEntry);
}

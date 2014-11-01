package com.sound.ampache.objects;

import java.util.Date;

/**
 * Created by dejvino on 30.10.14.
 */
public class UserLogEntry
{
	public static enum Severity implements Comparable<Severity>
	{
		DEBUG,
		INFO,
		WARNING,
		CRITICAL
	}

    public final Date timestamp;
	public final Severity severity;
    public final String title;
    public final String details;

	public UserLogEntry(Date timestamp, Severity severity, String title, String details)
	{
		this.timestamp = timestamp;
		this.severity = severity;
        this.title = title;
        this.details = details;
	}

    public UserLogEntry(Severity severity, String title, String details)
    {
        this(new Date(), severity, title, details);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserLogEntry that = (UserLogEntry) o;

        if (details != null ? !details.equals(that.details) : that.details != null) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null)
            return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = timestamp != null ? timestamp.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (details != null ? details.hashCode() : 0);
        return result;
    }
}

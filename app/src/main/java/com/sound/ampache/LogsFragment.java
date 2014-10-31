package com.sound.ampache;

/* Copyright (c) 2008 Kevin James Purdy <purdyk@onid.orst.edu>
 * Copyright (c) 2014 David Hrdina Nemecek <dejvino@gmail.com>
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

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sound.ampache.objects.UserLogEntry;
import com.sound.ampache.utility.UserLoggerListener;

import java.text.SimpleDateFormat;

public class LogsFragment extends Fragment implements AdapterView.OnItemClickListener, UserLoggerListener
{
    private View emptyView;
    private ListView listView;

    private LogsAdapter logsAdapter;

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.logs_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

	    emptyView = view.findViewById(android.R.id.empty);
	    listView = (ListView) view.findViewById(R.id.list);
	    listView.setOnItemClickListener(this);

	    logsAdapter = new LogsAdapter(getActivity());
	    listView.setAdapter(logsAdapter);

	    amdroid.logger.setLogListener(this);

	    // Center the playlist at the current song
	    centerList( 0 );
    }

	private void centerList (int adjust)
	{
		int playlistIndex = amdroid.playbackControl.getPlayingIndex();
		listView.setSelection(playlistIndex + adjust );
		refreshEmptyView();
	}

	@Override
    public void onDestroy()
	{
        super.onDestroy();
    }

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
	{
		// nothing to do
	}

	@Override
	public void onLogEntry(UserLogEntry logEntry)
	{
		logsAdapter.refresh();
	}

	private class LogsAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        public LogsAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return amdroid.logger.size();
        }

        public Object getItem(int position) {
            return amdroid.logger.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public void refresh() {
            notifyDataSetChanged();
        }

        public void clearItems() {
            amdroid.logger.clear();
            notifyDataSetChanged();
        }

        @Override
        public boolean isEmpty()
        {
            return getCount() <= 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            listItem holder;
            UserLogEntry logEntry = amdroid.logger.get(position);

            /* we don't reuse  */
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.log_item, null);
                holder = new listItem();

                holder.timestamp = (TextView) convertView.findViewById(R.id.timestamp);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.details = (TextView) convertView.findViewById(R.id.details);

                convertView.setTag(holder);
            } else {
                holder = (listItem) convertView.getTag();
            }

            holder.timestamp.setText(new SimpleDateFormat().format(logEntry.timestamp));
            holder.title.setText(logEntry.title);
            holder.details.setText(logEntry.details);

            return convertView;
        }
    }

    private static class listItem {
        TextView timestamp;
        TextView title;
        TextView details;
    }

    private void refreshEmptyView()
    {
        if (logsAdapter.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }
}


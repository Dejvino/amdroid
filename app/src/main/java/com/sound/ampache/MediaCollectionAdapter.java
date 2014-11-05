package com.sound.ampache;

/* Copyright (c) 2008-2009 Kevin James Purdy <purdyk@gmail.com>
 * Copyright (c) 2010 Jacob Alexander   < haata@users.sf.net >
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
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sound.ampache.objects.Album;
import com.sound.ampache.objects.ampacheObject;
import com.sound.ampache.utility.ResultCallback;

import java.util.ArrayList;

public final class MediaCollectionAdapter extends ArrayAdapter
{

	private Context mCtx;
	private int resid;
	private LayoutInflater mInflater;

	public MediaCollectionAdapter(Context context, int resid, ArrayList list)
	{
		super(context, resid, list);
		this.resid = resid;
		mCtx = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		bI holder;
		ampacheObject cur = (ampacheObject) getItem(position);

        /* we don't reuse */
		if (convertView == null) {
			convertView = mInflater.inflate(resid, null);
			holder = new bI();

			holder.root = convertView;
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.other = (TextView) convertView.findViewById(R.id.other);
			holder.art = (ImageView) convertView.findViewById(R.id.art);

			convertView.setTag(holder);
		} else {
			holder = (bI) convertView.getTag();
		}

		convertView.setId(position);

		if (cur != null) {
			holder.title.setText(cur.toString());
			holder.other.setText(cur.extraString());
			if (holder.art != null) {
				holder.art.setImageResource(R.drawable.album_loading);
				if (cur instanceof Album) {
					String url = ((Album) cur).art;
					final View requestView = convertView;
					final ImageView requestArtView = holder.art;
					amdroid.albumArtCache.requestBitmap(url, new ResultCallback<Bitmap>()
					{
						@Override
						public void onResultCallback(Bitmap result)
						{
							if (position != requestView.getId()) {
								// reused view, run away!
								return;
							}
							if (result != null) {
								requestArtView.setImageBitmap(result);
							} else {
								requestArtView.setImageResource(R.drawable.album_not_available);
							}
						}
					});
				}
			}
		}
		return convertView;
	}

	public static final class bI
	{
		View root;

		TextView title;
		TextView other;
		ImageView art;
	}
}


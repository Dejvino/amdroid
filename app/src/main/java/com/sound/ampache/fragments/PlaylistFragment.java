package com.sound.ampache.fragments;

/* Copyright (c) 2008 Kevin James Purdy <purdyk@onid.orst.edu>
 * Copyright (c) 2010 Jacob Alexander   < haata@users.sf.net >
 * Copyright (c) 2010 Kristopher Heijari < iix.ftw@gmail.com >
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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sound.ampache.GlobalMediaPlayerControl;
import com.sound.ampache.R;
import com.sound.ampache.amdroid;
import com.sound.ampache.objects.Media;
import com.sound.ampache.objects.Song;
import com.sound.ampache.service.AbstractPlayerServiceStatusListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public final class PlaylistFragment extends Fragment implements OnItemClickListener,
		GlobalMediaPlayerControl.PlayingIndexListener,
		GlobalMediaPlayerControl.PlaylistCurrentListener
{
	private View empty;
	private ListView lv;
	private ImageView artView;

	private PlaylistAdapter pla;

	private Boolean albumArtEnabled = false;
	private PlaybackListener playbackListener = new PlaybackListener();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.playlist_layout, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		empty = view.findViewById(android.R.id.empty);
		lv = (ListView) view.findViewById(R.id.list);
		lv.setOnItemClickListener(this);

		pla = new PlaylistAdapter(getActivity());
		lv.setAdapter(pla);

		// register our adapter to be called when a change to the currentPlaylist ocurrs
		amdroid.playbackControl.setPlayingIndexListener(this);

		//register ourselves to receive callbacks when playing index changes occurr
		amdroid.playbackControl.setPlaylistCurrentListener(this);

		// Setup Album Art View TODO
		artView = (ImageView) view.findViewById(R.id.picview);

		// Load Album Art on Entry, currently SLOOOOOOW so TODO
		//if ( amdroid.playbackControl.getPlaylistCurrent().size() > 0 )
		//    loadAlbumArt();

		// Center the playlist at the current song
		centerList(0);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		amdroid.playbackControl.registerServiceStatusListener(playbackListener);
	}

	@Override
	public void onStop()
	{
		super.onStop();
		amdroid.playbackControl.unregisterServiceStatusListener(playbackListener);
	}

	/*@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.playlist_menu, menu);
        return true;
    }*/

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.pl_clear:
            if (amdroid.playbackControl.isPlaying())
                amdroid.playbackControl.stop();
            amdroid.playbackControl.setPlayingIndex(0);
            pla.clearItems();
            break;

        case R.id.pl_save:
            try {
                FileOutputStream pout = openFileOutput("playlist", 0);
                ObjectOutputStream pos = new ObjectOutputStream(pout);
                pos.writeObject(amdroid.playbackControl.getPlaylistCurrent());
                pout.close();
            } catch (Exception poo) {
                Toast.makeText(this, "Error: " + poo.toString(), Toast.LENGTH_LONG).show();
            }
            break;

        case R.id.pl_load:
            if (amdroid.playbackControl.isPlaying())
                amdroid.playbackControl.stop();
            amdroid.playbackControl.setPlayingIndex(0);
            //mc.setEnabled(false);
            try {
                FileInputStream pin = openFileInput("playlist");
                ObjectInputStream poin = new ObjectInputStream(pin);
                amdroid.playbackControl.addAllPlaylistCurrent( (ArrayList<Media>) poin.readObject() );
                pin.close();
            } catch (Exception poo) {
                Toast.makeText(this, "Error: " + poo.toString(), Toast.LENGTH_LONG).show();
            }
            pla.refresh();
            break;


        case R.id.pl_albumart:
            if (albumArtEnabled) {
                albumArtEnabled = false;
                artView.setVisibility(View.GONE);
            }
            else {
                artView.setVisibility(View.VISIBLE);
                albumArtEnabled = true;
                loadAlbumArt();
            }
            break;
        }
        return true;
    }*/

	private void loadAlbumArt()
	{
		if (amdroid.playbackControl.getPlaylistCurrent().size() <= 0 || !albumArtEnabled) {
			artView.setVisibility(View.GONE);
			return;
		}

		int i = amdroid.playbackControl.getPlayingIndex();
		if (i >= amdroid.playbackControl.getPlaylistCurrent().size())
			return;
		Song chosen = (Song) amdroid.playbackControl.getPlaylistCurrent().get(amdroid.playbackControl.getPlayingIndex());

		Log.i("Amdroid", "Art URL     - " + chosen.art);
		Log.i("Amdroid", "Art URL (C) - " + chosen.liveArt("#TOKEN#"));

		try {
			URL artUrl = new URL(chosen.liveArt("#TOKEN#"));
			Object artContent = artUrl.getContent();
			Drawable albumArt = Drawable.createFromStream((InputStream) artContent, "src");
			artView.setImageDrawable(albumArt);

			if (artView.getDrawable() != null)
				artView.setVisibility(View.VISIBLE);
            /* Something needs to happen here to clear the image view, too lazy atm */
			else {
				artView.setVisibility(View.GONE);
			}


		} catch (MalformedURLException e) {
			Log.i("Amdroid", "Album Art URL sucks! Try something else.");
		} catch (IOException e) {
			Log.i("Amdroid", "Teh interwebs died...");
		}
	}


	/* These functions help with displaying the |> icon next to the currently playing song */
	private void turnOffPlayingView()
	{
        /* TODO we should probably keep track of which song we've displayed a playing icon for. 
         * Looping through all items in the listview will be unneffective for large lists */
		for (int i = 0; i < lv.getChildCount(); i++) {
			View holder = lv.getChildAt(i);
			if (holder != null) {
				ImageView img = (ImageView) holder.findViewById(R.id.art);
				img.setVisibility(View.INVISIBLE);
			}
		}
	}

	private void turnOnPlayingView()
	{
		if (amdroid.playbackControl.getPlayingIndex() >= lv.getFirstVisiblePosition() && amdroid.playbackControl.getPlayingIndex() <= lv.getLastVisiblePosition()) {
			View holder = lv.getChildAt(amdroid.playbackControl.getPlayingIndex() - lv.getFirstVisiblePosition());
			if (holder != null) {
				ImageView img = (ImageView) holder.findViewById(R.id.art);
				img.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView l, View v, int position, long id)
	{
		if (amdroid.playbackControl.prepared) {
			amdroid.playbackControl.setPlayingIndex(position);
			amdroid.playbackControl.play();
		}
	}


	private void centerList(int adjust)
	{
		int playlistIndex = amdroid.playbackControl.getPlayingIndex();
		lv.setSelection(playlistIndex + adjust);
		refreshEmptyView();
	}

	private class PlaylistAdapter extends BaseAdapter
	{
		private LayoutInflater mInflater;

		public PlaylistAdapter(Context context)
		{
			mInflater = LayoutInflater.from(context);
		}

		public int getCount()
		{
			return amdroid.playbackControl.getPlaylistCurrent().size();
		}

		public Object getItem(int position)
		{
			return amdroid.playbackControl.getPlaylistCurrent().get(position);
		}

		public long getItemId(int position)
		{
			return position;
		}

		public void refresh()
		{
			notifyDataSetChanged();
		}

		public void clearItems()
		{
			amdroid.playbackControl.clearPlaylistCurrent();
			notifyDataSetChanged();
		}

		@Override
		public boolean isEmpty()
		{
			return getCount() <= 0;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			plI holder;
			Media cur = amdroid.playbackControl.getPlaylistCurrent().get(position);

            /* we don't reuse  */
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.playlist_item, null);
				holder = new plI();

				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.other = (TextView) convertView.findViewById(R.id.other);
				holder.art = (ImageView) convertView.findViewById(R.id.art);

				convertView.setTag(holder);
			} else {
				holder = (plI) convertView.getTag();
			}

			holder.title.setText(cur.name);
			holder.other.setText(cur.extraString());
			if (amdroid.playbackControl.getPlayingIndex() == position) {
				holder.art.setVisibility(View.VISIBLE);
			} else {
				holder.art.setVisibility(View.INVISIBLE);
			}

			return convertView;
		}
	}

	static class plI
	{
		TextView title;
		TextView other;
		ImageView art;
	}


    /*public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (android.os.Build.VERSION.SDK_INT < 5 && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }*/

	@Override
	public void onPlayingIndexChange()
	{
		turnOffPlayingView();
		centerList(-1);
		turnOnPlayingView();
		loadAlbumArt();
	}

	private void refreshEmptyView()
	{
		if (pla.isEmpty()) {
			empty.setVisibility(View.VISIBLE);
		} else {
			empty.setVisibility(View.GONE);
		}
	}

	@Override
	public void onPlaylistCurrentChange()
	{
		pla.notifyDataSetChanged();
		refreshEmptyView();
	}

	private class PlaybackListener extends AbstractPlayerServiceStatusListener
	{
		@Override
		public void onNewMedia()
		{
			updatePlaylist();
		}

		@Override
		public void onPlaylistIndexChanged(int index)
		{
			updatePlaylist();
		}

		@Override
		public void onPlaylistChanged(int size)
		{
			updatePlaylist();
		}

		private void updatePlaylist()
		{
			onPlayingIndexChange();
		}
	}
}

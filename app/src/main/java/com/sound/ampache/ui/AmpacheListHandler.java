package com.sound.ampache.ui;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.sound.ampache.amdroid;
import com.sound.ampache.net.AmpacheApiAction;
import com.sound.ampache.objects.Directive;
import com.sound.ampache.objects.ampacheObject;

import java.util.ArrayList;

/**
* Created by dejvino on 3.11.14.
*/
class AmpacheListHandler extends Handler
{
	public static final int AMPACHE_INIT_REQUEST = 0x1336;
	public static final int AMPACHE_INC_REQUEST = 0x1337;
	public static final int ENQUEUE_SONG = 0x1339;

	private AmpacheListView ampacheListView;
	public boolean stopIncFetch = false;
	public boolean isFetching = false;
	private Directive lastDirective;

	public AmpacheListHandler(AmpacheListView ampacheListView)
	{
		this.ampacheListView = ampacheListView;
	}

	public void setIsFetching(boolean val)
	{
		if (isFetching != val) {
			isFetching = val;
			ampacheListView.onIsFetchingChange(isFetching);
		}
	}

	public void enqueMessage(int what, Directive directive, int startIndex, boolean addHistory)
	{
		ampacheListView.onEnqueMessage(what, directive, startIndex, addHistory);

		if (isFetching && what == AMPACHE_INIT_REQUEST) {
			stopIncFetch = true;
		}
		if (what != ENQUEUE_SONG) {
			setIsFetching(true);
			lastDirective = directive;
		}

		Message requestMsg = this.obtainMessage();
		requestMsg.arg1 = startIndex;
		requestMsg.obj = directive;
		requestMsg.what = what;
		// tell it how to handle the stuff
		requestMsg.replyTo = new Messenger(this);
		Log.d("AmpacheAmdroidList", "Sending message: " + requestMsg.toString());
		amdroid.networkClient.sendMessage(requestMsg);
	}

	@Override
	public void handleMessage(Message msg)
	{
		Log.d("AmpacheAmdroidList", "Handling message: " + msg.toString());
		if (msg.what == AMPACHE_INIT_REQUEST || msg.what == AMPACHE_INC_REQUEST) {
			if (stopIncFetch && msg.what == AMPACHE_INC_REQUEST) {
				return;
			}

			// Clear the collection adapter in case we have received "leftovers"
			if (msg.what == AMPACHE_INIT_REQUEST) {
				ampacheListView.onClearAmpacheObjects();
				stopIncFetch = false;
			}

			// Update our list with the received data
			ArrayList<ampacheObject> aList = (ArrayList<ampacheObject>) msg.obj;
			ampacheListView.onAddAmpacheObjects(aList);

			// queue up a new inc fetch if we did not receive 100 results. 100 is the limit set
			// in AmpacheApiClient
			if (lastDirective.action != AmpacheApiAction.STATS && aList.size() >= 100) {
				enqueMessage(AMPACHE_INC_REQUEST, lastDirective, msg.arg1 + 100, false);
			} else {
				setIsFetching(false);
			}

		} else if (msg.what == ENQUEUE_SONG) {
			amdroid.playbackControl.addAllPlaylistCurrent((ArrayList) msg.obj);
		} else {
			// Handle error
		}
	}

}

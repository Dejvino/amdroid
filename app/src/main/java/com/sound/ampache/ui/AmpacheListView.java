package com.sound.ampache.ui;

import com.sound.ampache.objects.Directive;
import com.sound.ampache.objects.ampacheObject;

import java.util.ArrayList;

/**
 * Created by dejvino on 3.11.14.
 */
public interface AmpacheListView
{
	void onIsFetchingChange(boolean isFetching);

	void onEnqueMessage(int what, Directive directive, int startIndex, boolean addHistory);

	void onClearAmpacheObjects();

	void onAddAmpacheObjects(ArrayList<ampacheObject> ampacheObjects);

	void enqueRequest(Directive directive);

	interface IsFetchingListener
	{
		public void onIsFetchingChange(boolean isFetching);
	}
}

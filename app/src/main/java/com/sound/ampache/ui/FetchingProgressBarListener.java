package com.sound.ampache.ui;

import android.view.View;

/**
 * Created by dejvino on 3.11.14.
 */
public class FetchingProgressBarListener implements AmpacheListView.IsFetchingListener
{
	private View v;

	public FetchingProgressBarListener(View rootView, int element_id)
	{
		v = rootView.findViewById(element_id);
	}

	@Override
	public void onIsFetchingChange(boolean isFetching)
	{
		v.setVisibility(isFetching ? View.VISIBLE : View.GONE);
	}
}

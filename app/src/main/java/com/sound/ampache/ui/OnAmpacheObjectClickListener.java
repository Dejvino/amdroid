package com.sound.ampache.ui;

import com.sound.ampache.objects.ampacheObject;

/**
 * Click listener for ampache objects.
 */
public interface OnAmpacheObjectClickListener<T extends ampacheObject>
{
	void onAmpacheObjectClick(T object);
}

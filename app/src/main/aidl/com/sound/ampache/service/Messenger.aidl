// AIDL parcelable for ServiceMessenger

package com.sound.ampache.service;

import android.os.Messenger;

parcelable Messenger;

/*
 * --> README <---
 *
 * If you are getting a compilation error pointing to this file about not being able to locate
 * android.os.Messenger, do the following:
 *
 * 1) Open "framework.aidl" located inside "android-sdk-directory/platforms/android-VERSION/"
 * 2) Append this line:
 * parcelable android.os.Messenger;
 *
 * Done!
 */

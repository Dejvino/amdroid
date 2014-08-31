package com.sound.ampache;

/* Copyright (c) 2008 Kevin James Purdy <purdyk@onid.orst.edu>
 * Copyright (c) 2010 Kristopher Heijari < iix.ftw@gmail.com >
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

import com.sound.ampache.ampacheCommunicator;
import com.sound.ampache.ampacheCommunicator.ampacheRequestHandler;

import android.app.Application;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.os.Debug;
import android.os.Bundle;

public final class amdroid extends Application {

    public static ampacheCommunicator comm;
    public static ampacheRequestHandler requestHandler;
    public static SharedPreferences prefs;

	public static int bufferPC;

    public static Boolean playListVisible;
    public static Boolean confChanged;

    protected static Bundle cache;
    private static Boolean mResumeAfterCall = false;
    public static GlobalMediaPlayerControl playbackControl;
    
	// This variable should be set to true once the mediaplayer object has been initialized. I.e. a
	// data source has been set and is prepared. 
	public static boolean mediaplayerInitialized = false;

    public void onCreate() {
        //Debug.waitForDebugger();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

	bufferPC = 0;

        cache = new Bundle();
        
        try {
            comm = new ampacheCommunicator(prefs, this);
            comm.perform_auth_request();
            requestHandler = comm.new ampacheRequestHandler();
            requestHandler.start();
        } catch (Exception poo) {
            
        }
        playbackControl = new GlobalMediaPlayerControl();
	playbackControl.initService( getApplicationContext() );
        
    }

    public void onDestroy() {
    }
}


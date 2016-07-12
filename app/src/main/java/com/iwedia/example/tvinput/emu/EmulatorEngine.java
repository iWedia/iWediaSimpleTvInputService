/*
 * Copyright (C) 2015 iWedia S.A. Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.iwedia.example.tvinput.emu;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;

import com.iwedia.dtv.service.IServiceCallback;
import com.iwedia.dtv.service.ServiceListUpdateData;
import com.iwedia.example.tvinput.TvSession;
import com.iwedia.example.tvinput.engine.DtvManager;
import com.iwedia.example.tvinput.utils.Logger;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class EmulatorEngine implements IServiceCallback {

    private final Logger mLog = new Logger(EmulatorEngine.class.getSimpleName(),
            Logger.ERROR);

    // ! in ms
    public static final int C_SURFACE_GETTER_REFRESH_TIME = 100;

    private Handler mHander;

    protected Object mSurfaceValidLocker = new Object();

    private boolean mSurfaceValid = false;

    private MediaPlayer mPlayer;

    private Uri mVideoFileUri;

    public EmulatorEngine(Context context) {
        mHander = new Handler();
        mPlayer = new MediaPlayer();

        DtvManager.getInstance().getDtvManager().getServiceControl().registerCallback(this);

        String localUri = "http://iwedia_androidx86-10.0.2.15:4567";
        String localIpAddress = getLocalIpAddress();
        if (localIpAddress != null) {
            this.mLog.d("LocalIpAddress is " + localIpAddress);
            localUri = "http://iwedia_androidx86-" + localIpAddress + ":4567";
        }
        mVideoFileUri = Uri.parse(localUri);
        
        try {
            mPlayer.setDataSource(context, mVideoFileUri);
            mHander.postDelayed(mSurfaceGetter, C_SURFACE_GETTER_REFRESH_TIME);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }



    private Runnable mSurfaceGetter = new Runnable() {

        @Override
        public void run() {
            if (TvSession.mVideoSurface == null) {
                mHander.postDelayed(mSurfaceGetter, C_SURFACE_GETTER_REFRESH_TIME);
                return;
            }
            mLog.d("valid surface received!");
            synchronized (TvSession.mVideoSurfaceLocker) {
                mPlayer.setSurface(TvSession.mVideoSurface);
            }
            synchronized (mSurfaceValidLocker) {
                mSurfaceValid = true;
            }
        }
    };
    
    public void stop(){
        mLog.d("[stop][player playing: " + mPlayer.isPlaying() + "]");
        if (mPlayer.isPlaying()) {                
            mPlayer.stop();
        }
    }

    /**
     * Channel changed callback.
     * 
     * @param liveRoute - Identifier of live route.
     */
    public void channelChangeStatus(int liveRoute, boolean channelChanged) {
        mLog.d("[channelChangeStatus][" + channelChanged + "]");
        synchronized (mSurfaceValidLocker) {
            if (!mSurfaceValid) {
                mLog.d("[channelChangeStatus][surface still not valid]");
                return;
            }
        }

        if (channelChanged) {            
            try {
                mPlayer.prepare();
                mPlayer.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }            
        } else {
            mPlayer.stop();
        }
    }

    /**
     * Signal status callback.
     * 
     * @param liveRoute - Identifier of live route.
     * @param signalAvailable - True if signal is available or False if not.
     */
    public void signalStatus(int liveRoute, boolean signalAvailable) {
    }

    /**
     * Service scrambled status callback.
     * 
     * @param liveRoute - Identifier of live route.
     * @param channelScrambled - True if channel is scrambled or False if not.
     */
    public void serviceScrambledStatus(int liveRoute, boolean channelScrambled) {
    }

    /**
     * Service stopped callback.
     * 
     * @param liveRoute - Identifier of live route.
     * @param serviceStopped - True if channel is stopped or False if not.
     */
    public void serviceStopped(int liveRoute, boolean serviceStopped) {
        mLog.d("[serviceStopped][" + serviceStopped + "]");
    }

    /**
     * Service list update callback.
     * 
     * @param serviceListUpdateData - ServiceListUpdateData instance that holds necessary data.
     */
    public void updateServiceList(ServiceListUpdateData serviceListUpdateData) {
    }

    /**
     * Callback for first iFrame.
     * 
     * @param liveRoute - Identifier of live route.
     */
    public void safeToUnblank(int liveRoute) {
    }

}

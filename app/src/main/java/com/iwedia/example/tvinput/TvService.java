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
package com.iwedia.example.tvinput;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputService;
import android.os.RemoteException;
import android.view.WindowManager;

import com.iwedia.dtv.service.IServiceCallback;
import com.iwedia.dtv.service.ServiceListUpdateData;
import com.iwedia.example.tvinput.TvSession.ITvSession;
import com.iwedia.example.tvinput.engine.DtvManager;
import com.iwedia.example.tvinput.utils.Logger;

import java.util.Hashtable;

/**
 * Main class for iWedia TV Input Service
 */
public class TvService extends TvInputService implements
        ITvSession {

    /** App name is used to help with logcat output filtering */
    public static final String APP_NAME = "iWediaTvInput_";
    /** Object used to write to logcat output */
    private final Logger mLog = new Logger(APP_NAME + TvService.class.getSimpleName(),
            Logger.ERROR);
    /** DVB manager instance. */
    protected DtvManager mDtvManager = null;
    /** List of all TVSessions */
    private Hashtable<String, TvSession> mSessionTable;
    private TvSession mCurrentSession = null;
    private IServiceCallback mServiceCallback = new IServiceCallback() {

        @Override
        public void channelChangeStatus(int routeId, boolean channelChanged) {
            mLog.d("[channelChangeStatus]");
            if (mCurrentSession != null) {
                mCurrentSession.updateTracks();
            }
        }

        @Override
        public void safeToUnblank(int routeId) {
            mLog.d("[safeToUnblank]");
        }

        @Override
        public void serviceScrambledStatus(int routeId, boolean serviceScrambled) {
            mLog.d("[serviceScrambledStatus]");
        }

        @Override
        public void serviceStopped(int routeId, boolean serviceStopped) {
            mLog.d("[serviceStopped]");
        }

        @Override
        public void signalStatus(int routeId, boolean signalAvailable) {
            mLog.d("[signalStatus]");
        }

        @Override
        public void updateServiceList(ServiceListUpdateData serviceListUpdateData) {
            mLog.d("[updateServiceList][service list update date: "
                    + serviceListUpdateData + "]");
        }
    };
    private BroadcastReceiver mContentRatingReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            mLog.d("CONTENT SETTINGS CHANGED: " + intent);
            if (mCurrentSession == null) {
                return;
            }
            if (intent.getAction().equals(
                    TvInputManager.ACTION_BLOCKED_RATINGS_CHANGED)) {
                mCurrentSession.checkContentRating();
            } else if (intent.getAction().equals(
                    TvInputManager.ACTION_PARENTAL_CONTROLS_ENABLED_CHANGED)) {
                mCurrentSession.checkContentRating();
            }
        }
    };

    @Override
    public void onCreate() {
        mLog.d("[onCreateService]");
        super.onCreate();
        mSessionTable = new Hashtable<String, TvSession>();
        try {
            DtvManager.instantiate(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mDtvManager = DtvManager.getInstance();
        if (mDtvManager == null) {
            mLog.e("DVBManager is null!");
        }
        mDtvManager.getDtvManager().getServiceControl().registerCallback(mServiceCallback);
        IntentFilter filter = new IntentFilter();
        filter.addAction(TvInputManager.ACTION_BLOCKED_RATINGS_CHANGED);
        filter.addAction(TvInputManager.ACTION_PARENTAL_CONTROLS_ENABLED_CHANGED);
        registerReceiver(mContentRatingReceiver, filter);
    }

    @Override
    public void onDestroy() {
        mLog.d("[onDestroyService]");
        super.onDestroy();
        mDtvManager.getDtvManager().getServiceControl().unregisterCallback(mServiceCallback);
        mDtvManager.deinit();
        unregisterReceiver(mContentRatingReceiver);
    }

    @Override
    public final Session onCreateSession(String inputId) {
        mLog.d("[onCreateSession][" + inputId + "]");
        mCurrentSession = mSessionTable.get(inputId);
        if (mCurrentSession == null) {
            mCurrentSession = onCreateSessionInternal(inputId);
            createOverlay(true);
            mSessionTable.put(inputId, mCurrentSession);
        }
        return mCurrentSession;
    }

    /**
     * Child classes should extend this to change the result of onCreateSession.
     */
    public TvSession onCreateSessionInternal(String inputId) {
        return new TvSession(this, this, inputId);
    }

    @Override
    public void onSessionRelease(TvSession session) {
        mLog.d("[onSessionRelease]");
        mSessionTable.remove(session.getInputID());
        mCurrentSession = null;
    }

    private void createOverlay(boolean tifOverlay) {
        if (tifOverlay) {
            // FIXME: When we inflate a SurfaceView through TvSession#onCreateOverlayView method
            // it is never creating a holding Surface object!
            // A temporary solution for rendering subtitles is to inflate overlay layout to
            // system alert window (code in else block).
            mCurrentSession.setOverlayViewEnabled(true);
        } else {
            // TODO: This is a temporary solution to render subtitles and should be removed!
            // Subtitles are rendered above all other UI elements.
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.OPAQUE);
            manager.addView(mCurrentSession.onCreateOverlayView(), params);
        }
    }
}

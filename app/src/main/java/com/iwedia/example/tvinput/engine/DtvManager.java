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
package com.iwedia.example.tvinput.engine;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.view.WindowManager;

import com.iwedia.dtv.dtvmanager.DTVManager;
import com.iwedia.dtv.dtvmanager.IDTVManager;
import com.iwedia.dtv.epg.IEpgControl;
import com.iwedia.dtv.service.Service;
import com.iwedia.dtv.service.ServiceDescriptor;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.example.tvinput.TvService;
import com.iwedia.example.tvinput.TvSession;
import com.iwedia.example.tvinput.callbacks.EpgCallback;
import com.iwedia.example.tvinput.data.ChannelDescriptor;
import com.iwedia.example.tvinput.engine.epg.EpgFull;
import com.iwedia.example.tvinput.engine.epg.EpgNowNext;
import com.iwedia.example.tvinput.utils.ExampleSwitches;
import com.iwedia.example.tvinput.utils.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Semaphore;

/**
 * Manager for handling MW Components.
 */
public class DtvManager {

    /** Object used to write to logcat output */
    private static final Logger mLog = new Logger(TvService.APP_NAME + DtvManager.class.getSimpleName(),
            Logger.ERROR);

    public enum MwRunningState {
        UNKNOWN, NOT_RUNNING, RUNNING
    };

    public static final String MW_STARTED_SYSTEM_PROP = "iwedia.general.mw_ready";
    public static final String MW_STARTED_YES = "1";
    public static final String MW_STARTED_NO = "0";

    /**
     * CallBack for EPG events.
     */
    public interface IEpgListener {

        /** Update Now Next values. */
        public void updateNowNext(int filterID, int serviceIndex);

        /** Update EPG list */
        public void updateEpgList();
    }

    /** Comedia's Master list index */
    public static final int MASTER_LIST_INDEX = 0;
    /** DtvManager instance */
    private IDTVManager mDtvManager = null;
    /** Subtitle manager instance */
    private SubtitleManager mSubtitleManager;
    /** Audio Track manager instance */
    private AudioManager mAudioManager;
    /** Volume manager instance */
    private android.media.AudioManager mVolumeManager;
    /** Channel manager instance */
    private ChannelManager mChannelManager;
    /** Route manager instance */
    private RouteManager mRouteManager;
    /** Instance of this manager */
    private static DtvManager sInstance = null;
    /** Current active channel */
    private int mCurrentlyActiveChannel = 0;
    /** Thread for handler creation */
    private HandlerThread mHandlerThread;
    /** Logic for acquisition timings */
    private EpgAcquisitionManager mEpgAcquisitionManager;
    /** Handler for adding EPG events */
    private Handler mEpgHandler;
    /** EPG CallBack */
    private EpgCallback mEPGCallBack = null;
    /** Application context */
    private static Context mContext;

    /** Current volume */
    private int mVolume;
    /** EPG manager helper class */
    private EpgManager mEpgManager = null;
    /** Video destination rectangle */
    private final Rect mVideoRect = new Rect();

    private static CheckMiddlewareAsyncTask mCheckMw = new CheckMiddlewareAsyncTask();

    private static MwRunningState mMwRunningState = MwRunningState.UNKNOWN;

    private static Semaphore mMwLocker = new Semaphore(0);

    private static int mMwClientWaitingCounter;

    private static Object mMwClientWaitingCounterLocker = new Object();

    /**
     * Gets an instance of this manager
     *
     * @return Instance of this manager
     */
    public static DtvManager getInstance() {
        return sInstance;
    }

    /**
     * Instantiates this manager
     *
     * @throws RemoteException If something is wrong with initialization of MW API
     */
    public static void instantiate(Context context) {
        mContext = context;
        if (sInstance == null) {
            switch (mMwRunningState) {
                case UNKNOWN:
                    synchronized (mMwClientWaitingCounterLocker) {
                        mMwClientWaitingCounter = 0;
                    }
                    mMwRunningState = MwRunningState.NOT_RUNNING;
                    mCheckMw.execute();
                case NOT_RUNNING:
                    synchronized (mMwClientWaitingCounterLocker) {
                        mMwClientWaitingCounter++;
                    }
                    // task already started, wait for finish
                    try {
                        mLog.d("[instantiate][waiting for client: " + mMwClientWaitingCounter + "]");
                        mMwLocker.acquire();
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    break;
                case RUNNING:
                    //
                    break;
            }
        }
    }

    /**
     * Constructor
     *
     * @throws RemoteException If something is wrong with initialization of MW API
     */
    private DtvManager(Context context) throws RemoteException {
        mContext = context;
        mDtvManager = new DTVManager();
        mVolumeManager = (android.media.AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        mVideoRect.set(0, 0, size.x, size.y);
    }

    /**
     * Gets MW Control handle of EPG
     *
     * @return EpgControl handle
     */
    public IEpgControl getEpgControl() {
        return mDtvManager.getEpgControl();
    }

    public EpgManager getEpgManager() {
        return mEpgManager;
    }

    /**
     * Add ChannelManager instance to this manager
     *
     * @param channelManager ChannelManager instance
     */
    public void setChannelManager(ChannelManager channelManager) {
        mChannelManager = channelManager;
    }

    /**
     * Initialize Service.
     *
     * @throws InternalException
     */
    private void initializeDtvFunctionality() throws RemoteException {
        mRouteManager = new RouteManager();
        mSubtitleManager = new SubtitleManager(mDtvManager.getSubtitleControl());
        mAudioManager = new AudioManager(mDtvManager.getAudioControl());
        mChannelManager = new ChannelManager(sInstance, mContext);
        mChannelManager.init();
        mHandlerThread = new HandlerThread(TvService.class.getSimpleName());
        mHandlerThread.start();
        mEpgAcquisitionManager = new EpgAcquisitionManager(mContext);
        mEpgAcquisitionManager.loadEpgPrefs();
        mEpgHandler = new Handler(mHandlerThread.getLooper());
        mEPGCallBack = new EpgCallback(this);
        mEpgManager = new EpgManager(this);
        mEpgManager.registerCallback(mEPGCallBack);
    }

    public IDTVManager getDtvManager() {
        return mDtvManager;
    }

    public void setVideoRect(Rect rect) {
        mLog.d("[setVideoRect][" + rect.toString() + "]");
        mVideoRect.set(rect);
    }

    /**
     * Stop MW video playback.
     *
     * @throws InternalException if stop is called with wrong parameters
     */
    public void stop() throws InternalException {
        mLog.d("[stop]");
        if (mSubtitleManager.isSubtitleActive()) {
            mSubtitleManager.hideSubtitles();
        }
        mDtvManager.getServiceControl().stopService(mRouteManager.getCurrentLiveRoute());
    }

    /**
     * Change Channel by Number.
     *
     * @param channelNumber
     */
    public boolean start(ChannelDescriptor channel) throws InternalException {
        mLog.d("[start] " + channel);
        switch (channel.getType()) {
            case CAB:
            case TER:
            case SAT:
                return startDvb(channel);
            case IP:
                return startIp(channel);
            case ANALOG:
            case PVR:
            case UNDEFINED:
            default:
                return false;
        }
    }

    private boolean startDvb(ChannelDescriptor channel) throws InternalException {
        mLog.d("[startDvb][" + channel.toString() + "]");
        int route = mRouteManager.getActiveRouteByServiceType(channel.getType());
        if (route == RouteManager.EC_INVALID_ROUTE) {
            mLog.e("[startDvb][unknown source type: " + channel.getType() + "]");
            return false;
        }
        mCurrentlyActiveChannel = channel.getServiceId();
        mRouteManager.updateCurrentLiveRoute(route);
        mDtvManager.getServiceControl().startService(route, MASTER_LIST_INDEX,
                mCurrentlyActiveChannel);
        if (ExampleSwitches.ENABLE_SCALE_FEATURE) {
            mDtvManager.getDisplayControl().scaleWindow(route, 200, 0, 1280, 720);
        } else {
            mLog.d("[startDvb][set rect: " + mVideoRect + "]");
            mDtvManager.getDisplayControl().scaleWindow(route,
                    mVideoRect.left, mVideoRect.top,
                    mVideoRect.width(), mVideoRect.height());
        }
        return true;
    }

    private boolean startIp(ChannelDescriptor channel) throws InternalException {
        mLog.d("[startIp][" + channel.toString() + "]");
        int route = mRouteManager.getLiveRouteIp();
        if (route == RouteManager.EC_INVALID_ROUTE) {
            mLog.e("[startIp][unknown source type: " + channel.getType() + "]");
            return false;
        }
        mRouteManager.updateCurrentLiveRoute(mRouteManager.getLiveRouteIp());
        mDtvManager.getServiceControl().zapURL(mRouteManager.getLiveRouteIp(), channel.getUrl());
        if (ExampleSwitches.ENABLE_SCALE_FEATURE) {
            mDtvManager.getDisplayControl().scaleWindow(route, 0, 200, 640, 480);
        } else {
            mDtvManager.getDisplayControl().scaleWindow(route,
                    mVideoRect.left, mVideoRect.top,
                    mVideoRect.width(), mVideoRect.height());
        }
        return true;
    }

    public int getCurrentServiceIndex() {
        Service service = mDtvManager.getServiceControl().getActiveService(
                mRouteManager.getCurrentLiveRoute());
        return service.getServiceIndex();
    }

    public Long getCurrentTransponder() {
        ServiceDescriptor serviceDescriptor = mDtvManager.getServiceControl().getServiceDescriptor(
                MASTER_LIST_INDEX, getCurrentServiceIndex());
        return (long) serviceDescriptor.getFrequency();
    }

    /**
     * Set Current Volume.
     */
    public void setVolume(double volume) {
        // TODO: live volume if it is active
        try {
            if (isMuted()) {
                setMute();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mVolume = (int) volume;
        mVolumeManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, mVolume, 0);
    }

    public boolean isMuted() throws RemoteException {
        // TODO: live volume if it is active
        boolean throwException = false;
        boolean isMuted = false;
        Method isMasterMute = null;
        try {
            isMasterMute = android.media.AudioManager.class.getDeclaredMethod("isMasterMute",
                    (Class[]) null);
        } catch (NoSuchMethodException e) {
            throwException = true;
            e.printStackTrace();
        }
        if (isMasterMute != null) {
            try {
                isMuted = (Boolean) isMasterMute.invoke(mVolumeManager);
            } catch (IllegalAccessException e) {
                throwException = true;
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                throwException = true;
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                throwException = true;
                e.printStackTrace();
            }
        }
        if (throwException) {
            throw new RemoteException("Failed to reflect isMasterMute method.");
        }
        return isMuted;
    }

    /**
     * Set Volume Mute Status.
     *
     * @throws RemoteException
     */
    public void setMute() throws RemoteException {
        // TODO: live volume if it is active
        boolean throwException = false;
        Method setMasterMute = null;
        try {
            setMasterMute = android.media.AudioManager.class.getDeclaredMethod("setMasterMute",
                    boolean.class, int.class);
        } catch (NoSuchMethodException e) {
            throwException = true;
            e.printStackTrace();
        }
        if (setMasterMute != null) {
            try {
                setMasterMute.invoke(mVolumeManager, !isMuted(), 0);
            } catch (IllegalAccessException e) {
                throwException = true;
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                throwException = true;
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                throwException = true;
                e.printStackTrace();
            }
        }
        if (throwException) {
            throw new RemoteException("Failed to reflect setMasterMute method.");
        }
    }

    /**
     * Called in order to update the full EPG list
     */
    public void updateEpgList() {
        mLog.d("[updateEpgList]");
        mEpgHandler.post(new EpgFull(mContext, mEpgAcquisitionManager, getCurrentServiceIndex(),
                getCurrentTransponder()));
    }

    /**
     * Called in order to update EPG Now and Next events
     */
    public void updateNowNext(int filterID, int serviceIndex) {
        mLog.d("[updateNowNext][filter id: " + filterID + "][service index: " + serviceIndex + "]");
        mEpgHandler.post(new EpgNowNext(mContext, mCurrentlyActiveChannel));
    }

    /**
     * Gets SubtitleManager
     *
     * @return Manager instance
     */
    public SubtitleManager getSubtitleManager() {
        return mSubtitleManager;
    }

    /**
     * Gets Audio Manager
     *
     * @return Manager instance
     */
    public AudioManager getAudioManager() {
        return mAudioManager;
    }

    /**
     * Gets Channel Manager
     *
     * @return Manager instance
     */
    public ChannelManager getChannelManager() {
        return mChannelManager;
    }

    /**
     * Gets Route Manager
     *
     * @return Manager instance
     */
    public RouteManager getRouteManager() {
        return mRouteManager;
    }

    /**
     * Gets Epg Acquisition Manager
     *
     * @return EpgAcquisitionManager instance
     */
    public EpgAcquisitionManager getEpgAcquisitionManager() {
        return mEpgAcquisitionManager;
    }

    /**
     * Deinit DVB manager
     */
    public void deinit() {
        mEpgManager.unregisterCallback(mEPGCallBack);
        try {
            stop();
        } catch (InternalException e) {
            e.printStackTrace();
        }
        sInstance = null;
        mHandlerThread.quit();
        mHandlerThread = null;
    }

    private static class CheckMiddlewareAsyncTask extends AsyncTask<Void, Void, String> {

        // ! in ms
        private int mWaitCycle;

        // ! in wait cycle
        private int mWaitCounter;

        public CheckMiddlewareAsyncTask() {
            super();

            mWaitCycle = 1000;
            mWaitCounter = 10;
        }

        @Override
        protected String doInBackground(Void... params) {
            String isMiddlewareInit = MW_STARTED_NO;

            while (true) {
                isMiddlewareInit = SystemProperties.get(MW_STARTED_SYSTEM_PROP,
                        MW_STARTED_NO);
                if (isMiddlewareInit.equals(MW_STARTED_YES)) {
                    mLog.d("[CheckMiddlewareAsyncTask][doInBackground][mw is started]");
                    break;
                } else {
                    try {
                        Thread.sleep(mWaitCycle);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mWaitCounter == 0) {
                        mLog.d("[CheckMiddlewareAsyncTask][doInBackground][timeout 10 seconds, mw not started]");
                        break;
                    }
                    mWaitCounter--;
                }
            }
            return isMiddlewareInit;
        }

        @Override
        protected void onPostExecute(String result) {
            mLog.d("[CheckMiddlewareAsyncTask][onPostExecute][result: " + result + "]");
            if (result.equals(MW_STARTED_YES)) {
                try {
                    sInstance = new DtvManager();
                    sInstance.initializeDtvFunctionality();

                    mMwRunningState = MwRunningState.RUNNING;

                    int mwClientWaitingCounter;
                    synchronized (mMwClientWaitingCounterLocker) {
                        mwClientWaitingCounter = mMwClientWaitingCounter;
                        mMwClientWaitingCounter = 0;
                    }

                    mLog.d("[onPostExecute][releasing: " + mwClientWaitingCounter + "]");

                    mMwLocker.release(mwClientWaitingCounter);
                } catch (RemoteException re) {
                    re.printStackTrace();
                }
            }
        }
    }
}

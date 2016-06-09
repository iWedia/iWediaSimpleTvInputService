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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArrayMap;

import com.iwedia.example.tvinput.TvService;
import com.iwedia.example.tvinput.engine.epg.EpgFull.IEpgAcquisitionListener;
import com.iwedia.example.tvinput.engine.utils.EpgRunnable;
import com.iwedia.example.tvinput.utils.Logger;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;

public class EpgAcquisitionManager implements IEpgAcquisitionListener {

    /** Object used to write to logcat output */
    private final Logger mLog = new Logger(TvService.APP_NAME + EpgRunnable.class.getSimpleName(),
            Logger.DEBUG);
    private static final Long TWO_MIN_IN_MILLIS = (long) (2 * 60 * 1000);
    private Context mContext;
    private SharedPreferences mEPGPrefs = null;
    private Map<String, Long> mLastAqvisitions = new ArrayMap<String, Long>();
    private boolean mAcquisitionInProgress = false;
    private Long mCurrentAcquisitionFrequency = 0L;

    public EpgAcquisitionManager(Context context) {
        mContext = context;
    }

    @SuppressWarnings("unchecked")
    public void loadEpgPrefs() {
        mEPGPrefs = mContext.getSharedPreferences("EPG_Info", Context.MODE_PRIVATE);
        try {
            mLastAqvisitions = (Map<String, Long>) mEPGPrefs.getAll();
            mLog.d("[loadEpgPrefs][last sqvisitions: " + mLastAqvisitions + "]");
        } catch (NullPointerException e) {
            mLog.d("[loadEpgPrefs][no EPG prefs data]");
        }
    }

    public void saveEpgPrefs(Map<Long, Long> values) {
        SharedPreferences.Editor editor = mEPGPrefs.edit();
        editor.clear();
        if (!mLastAqvisitions.isEmpty()) {
            Set<String> keys = mLastAqvisitions.keySet();
            for (String key : keys) {
                editor.putLong(key, mLastAqvisitions.get(key));
            }
        }
        editor.commit();
    }

    public void updateEPGInfo(Long key) {
        String sKey = String.valueOf(key);
        Long currentTime = Calendar.getInstance().getTimeInMillis();
        mLastAqvisitions.put(sKey, currentTime);
        SharedPreferences.Editor editor = mEPGPrefs.edit();
        editor.putLong(sKey, currentTime);
        editor.commit();
    }

    public boolean shouldStartEPGAquistion() {
        long key = Manager.getInstance().getCurrentTransponder();
        if (mAcquisitionInProgress && mCurrentAcquisitionFrequency == key) {
            mLog.d("[]");
            // Acquisition in progress for current freq
            return false;
        }
        // if acquisition in progress for different freq, then just check time
        // condition
        if (mLastAqvisitions.isEmpty()) {
            return true;
        }
        String sKey = String.valueOf(key);
        Long lastAquisition = mLastAqvisitions.get(sKey);
        if (lastAquisition == null) {
            return true;
        }
        Long currentTime = Calendar.getInstance().getTimeInMillis();
        // More than one day, then we should start
        if (lastAquisition + TWO_MIN_IN_MILLIS <= currentTime) {
            // Old acquisition
            return true;
        } else {
            // up to date acquisition
            return false;
        }
    }

    @Override
    public void epgAcquisitionStarted(Long frequency) {
        mAcquisitionInProgress = true;
        mCurrentAcquisitionFrequency = frequency;
    }

    @Override
    public void epgAcquisitionFinished(Long frequency) {
        mAcquisitionInProgress = false;
        mCurrentAcquisitionFrequency = 0L;
        updateEPGInfo(frequency);
    }
}

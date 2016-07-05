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

import android.net.ParseException;
import android.os.Handler;

import com.iwedia.dtv.epg.EpgEvent;
import com.iwedia.dtv.epg.EpgEventType;
import com.iwedia.dtv.epg.EpgMasterList;
import com.iwedia.dtv.epg.EpgServiceFilter;
import com.iwedia.dtv.epg.EpgTimeFilter;
import com.iwedia.dtv.epg.IEpgCallback;
import com.iwedia.dtv.epg.IEpgControl;
import com.iwedia.dtv.setup.ISetupControl;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.dtv.types.TimeDate;
import com.iwedia.example.tvinput.TvService;
import com.iwedia.example.tvinput.data.ChannelDescriptor;
import com.iwedia.example.tvinput.utils.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class EpgManager {

    /** Object used to write to logcat output */
    private final Logger mLog = new Logger(TvService.APP_NAME + EpgManager.class.getSimpleName(),
            Logger.ERROR);
    private IEpgControl mEpgControl = null;
    private ISetupControl mSetupControl = null;
    private int mEpgClientId = -1;
    private DtvManager mDTVManager = null;
    private TimeDate mEpgStartTime;
    private TimeDate mEpgEndTime;
    private static final int INITIAL_PREPARE_DELAY = 5000;
    private Handler mHandler = new Handler();

    public EpgManager(DtvManager dtvManager) {
        mDTVManager = dtvManager;
        try {
            mEpgControl = dtvManager.getDtvManager().getEpgControl();
            mSetupControl = dtvManager.getDtvManager().getSetupControl();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createWindow(TimeDate startTimeOfAcquisition, int durationInHours) {
        mLog.d("[prepareGetEpgEvents] start=" + startTimeOfAcquisition + " len=" + durationInHours);
        try {
            int dtvServices = mDTVManager.getChannelManager().getDtvChannelListSize(mDTVManager.getCurrentRoutes());
            ArrayList<Integer> masterListIndexes = new ArrayList<Integer>();
            mLog.d("[prepareGetEpgEvents] dtvServices=" + dtvServices);
            for (int i = 0; i < dtvServices; i++) {
                ChannelDescriptor channel = mDTVManager.getChannelManager().getChannelByIndex(i);
                mLog.d("[prepareGetEpgEvents] channel[" + i + "]=" + channel);
                if (channel != null) {
                    masterListIndexes.add(channel.getServiceId());
                }
            }
            EpgMasterList epgMasterList = new EpgMasterList(masterListIndexes);
            mEpgControl.createWindow(epgMasterList, startTimeOfAcquisition, durationInHours);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InternalException e) {
            e.printStackTrace();
        }
    }

    public void prepareGetEpgEvents() {
        mLog.d("[prepareGetEpgEvents]");
        TimeDate lCurrentTime = mSetupControl.getTimeDate();
        Calendar lllCalendar = lCurrentTime.getCalendar();
        mEpgStartTime = new TimeDate(1, 0, 0,
                lllCalendar.get(Calendar.DAY_OF_MONTH),
                lllCalendar.get(Calendar.MONTH) + 1, lllCalendar.get(Calendar.YEAR));
        lllCalendar.add(Calendar.HOUR, 7 * 24);
        mEpgEndTime = new TimeDate(59, 59, 23,
                lllCalendar.get(Calendar.DAY_OF_MONTH),
                lllCalendar.get(Calendar.MONTH) + 1, lllCalendar.get(Calendar.YEAR));
        mLog.d("[createWindow][start]");
        createWindow(mEpgStartTime, 7 * 24);
        mLog.d("[createWindow][end]");
    }

    /**
     * Registers client callback
     *
     * @param callback
     */
    public void registerCallback(IEpgCallback callback) {
        try {
            mEpgClientId = mEpgControl.createEventList();
        } catch (InternalException e) {
            e.printStackTrace();
        }
        mEpgControl.registerCallback(callback, mEpgClientId);
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                prepareGetEpgEvents();
            }
        }, INITIAL_PREPARE_DELAY);
    }

    public void unregisterCallback(IEpgCallback callback) {
        try {
            mEpgControl.stopAcquisition(mEpgClientId);
        } catch (InternalException e) {
            e.printStackTrace();
        }
        try {
            mEpgControl.releaseEventList(mEpgClientId);
        } catch (InternalException e) {
            e.printStackTrace();
        }
        mEpgControl.unregisterCallback(callback, mEpgClientId);
    }

    /**
     * Returns EPG events for desired service index and desired day.
     *
     * @param indexInMasterList Index of desired service in master list.
     * @return List of EPG events.
     */
    public synchronized ArrayList<EpgEvent> getEpgEvents(int indexInMasterList)
            throws ParseException,
            IllegalArgumentException, InternalException {
        ArrayList<EpgEvent> events = new ArrayList<EpgEvent>();
        if (mEpgStartTime == null || mEpgEndTime == null) {
            return events;
        }
        EpgEvent lEvent = null;
        int lEpgEventsSize = 0;
        TimeDate lCurrentTimeFromStream = mSetupControl.getTimeDate();
        Calendar lCalendarTimeFromStream = lCurrentTimeFromStream.getCalendar();
        Calendar lTimeOnDevice = Calendar.getInstance();
        long diffTime = lTimeOnDevice.getTimeInMillis() - lCalendarTimeFromStream.getTimeInMillis();
        final double diffDays = Math.round((float) diffTime / ((float) 1000 * 60 * 60 * 24));
        /**
         * Create Time Filter
         */
        EpgTimeFilter lEpgTimeFilter = new EpgTimeFilter();
        lEpgTimeFilter.setTime(mEpgStartTime, mEpgEndTime);
        mEpgControl.setFilter(mEpgClientId, lEpgTimeFilter);
        /**
         * Set Service Filter.
         */
        EpgServiceFilter lEpgServiceFilter = new EpgServiceFilter();
        lEpgServiceFilter.setServiceIndex(indexInMasterList);
        mEpgControl.setFilter(mEpgClientId, lEpgServiceFilter);
        mEpgControl.startAcquisition(mEpgClientId);
        lEpgEventsSize = mEpgControl.getAvailableEventsNumber(
                mEpgClientId, indexInMasterList);
        for (int eventIndex = 0; eventIndex < lEpgEventsSize; eventIndex++) {
            // if(mDTVManager.getScanManager().isScanStarted()){
            // break;
            // }
            lEvent = mEpgControl.getRequestedEvent(mEpgClientId, indexInMasterList, eventIndex);
            if (lEvent != null) {
                // Logger.log("Event BEFORE: " + lEvent.getName() + ", " +
                // lEvent.getStartTime().toString() + " - " +
                // lEvent.getEndTime());
                changeEventTimes(lEvent, (int) diffDays);
                events.add(lEvent);
                // Logger.log("Event AFTER: " + lEvent.getName() + ", " +
                // lEvent.getStartTime().toString() + " - " + lEvent
                // .getEndTime() + ", diffDays: " + diffDays);
            }
        }
        mEpgControl.stopAcquisition(mEpgClientId);
        return events;
    }

    private void changeEventTimes(EpgEvent event, int dayDifference) {
        TimeDate startTimeDate = event.getStartTime();
        TimeDate endTimeDate = event.getEndTime();
        final Calendar startTime = Calendar.getInstance(TimeZone.getDefault());
        final Calendar endTime = Calendar.getInstance(TimeZone.getDefault());
        // Get time from event
        startTime.setTimeInMillis(startTimeDate.getCalendar().getTimeInMillis());
        endTime.setTimeInMillis(endTimeDate.getCalendar().getTimeInMillis());
        // Add day difference to time
        startTime.add(Calendar.DATE, dayDifference);
        endTime.add(Calendar.DATE, dayDifference);
        // Set new times to event date and time
        setEventDate(startTimeDate, startTime);
        setEventDate(endTimeDate, endTime);
    }

    /**
     * For various demos, time from stream is not current date so we must set date of every EpgEvent
     * to be for current date
     *
     * @param event           EpgEvent that contains start and end times
     * @param day             Day of week for which we are fetching EPG data
     * @param isForDesiredDay If event ends in another day (Guide is fetched for today but EpgEvent
     *                        ends in tomorrow)
     */
    private void changeEventTimes(EpgEvent event, int day, boolean isForDesiredDay) {
        /**
         * We must calculate new start time and end time. If transport stream is not live this will
         * fix its time in EPG events
         */
        boolean changeOnlyEndTime = true;
        Calendar curentTime = Calendar.getInstance();
        final Calendar startTime = Calendar.getInstance(TimeZone.getDefault());
        final Calendar endTime = Calendar.getInstance(TimeZone.getDefault());
        startTime.setTimeInMillis(event.getStartTime().getCalendar().getTimeInMillis());
        endTime.setTimeInMillis(event.getEndTime().getCalendar().getTimeInMillis());
        if (!isForDesiredDay) {
            // Start time is in one day and end time is in different day
            if (startTime.get(Calendar.DAY_OF_MONTH) != endTime
                    .get(Calendar.DAY_OF_MONTH)) {
                changeOnlyEndTime = true;
            } else {
                changeOnlyEndTime = false;
            }
        }
        // start time
        startTime.set(Calendar.YEAR, curentTime.get(Calendar.YEAR));
        startTime.set(Calendar.MONTH, curentTime.get(Calendar.MONTH));
        startTime.set(Calendar.DAY_OF_MONTH,
                curentTime.get(Calendar.DAY_OF_MONTH));
        startTime.set(Calendar.MILLISECOND, 0);
        startTime.set(Calendar.SECOND, 0);
        // End time
        endTime.set(Calendar.YEAR, curentTime.get(Calendar.YEAR));
        endTime.set(Calendar.MONTH, curentTime.get(Calendar.MONTH));
        endTime.set(Calendar.DAY_OF_MONTH,
                curentTime.get(Calendar.DAY_OF_MONTH));
        endTime.set(Calendar.MILLISECOND, 0);
        endTime.set(Calendar.SECOND, 0);
        if (!isForDesiredDay) {
            // Start time is in one day and end time is in different day
            endTime.add(Calendar.DAY_OF_MONTH, 1);
            // change start time too
            if (!changeOnlyEndTime) {
                startTime.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        // add day
        endTime.add(Calendar.DAY_OF_MONTH, day);
        startTime.add(Calendar.DAY_OF_MONTH, day);
        // Set new times to event date and time
        setEventDate(event.getStartTime(), startTime);
        setEventDate(event.getEndTime(), endTime);
    }

    /**
     * Sets desired date to start and end time of event
     */
    private void setEventDate(TimeDate date, Calendar calendar) {
        date.setSec(0);
        date.setMin(calendar.get(Calendar.MINUTE));
        date.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        date.setDay(calendar.get(Calendar.DAY_OF_MONTH));
        date.setMonth(calendar.get(Calendar.MONTH) + 1);
        date.setYear(calendar.get(Calendar.YEAR));
    }

    /**
     * Find event that is running and return its index.
     */
    public int getRunningEventIndex(ArrayList<EpgEvent> events) {
        TimeDate lCurrentTime;
        try {
            lCurrentTime = mDTVManager.getDtvManager().getSetupControl()
                    .getTimeDate();
            Calendar calendarCurrent = lCurrentTime.getCalendar();
            for (int i = 0; i < events.size(); i++) {
                EpgEvent event = events.get(i);
                if (calendarCurrent.after(event.getStartTime().getCalendar())
                        && calendarCurrent.before(event.getEndTime()
                        .getCalendar())) {
                    return i;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getEpgClientId() {
        return mEpgClientId;
    }

    public EpgEvent getPresentFollowingEvent(int serviceIndex,
            EpgEventType epgEventType) {
        return null;
    }

    /**
     * Get Current Time and Date.
     */
    public TimeDate getCurrentTimeDate() {
        return mDTVManager.getDtvManager().getSetupControl().getTimeDate();
    }

    public String getEventExtendedDescription(int eventId, int serviceIndex) {
        return mEpgControl.getEventExtendedDescription(mEpgClientId, eventId,
                serviceIndex);
    }

    public TimeDate getWindowStartTime() {
        return mEpgStartTime;
    }

    public TimeDate getWindowEndTime() {
        return mEpgEndTime;
    }

    public int getEpgFilterID() {
        return mEpgClientId;
    }
}

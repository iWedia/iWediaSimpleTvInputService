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

package com.iwedia.example.tvinput.engine.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.net.Uri;

import com.iwedia.dtv.epg.EpgEvent;
import com.iwedia.dtv.epg.IEpgControl;
import com.iwedia.example.tvinput.TvService;
import com.iwedia.example.tvinput.a4tvbal.AlEpgEvent;
import com.iwedia.example.tvinput.data.ChannelDescriptor;
import com.iwedia.example.tvinput.data.EpgProgram;
import com.iwedia.example.tvinput.engine.ChannelManager;
import com.iwedia.example.tvinput.engine.Manager;
import com.iwedia.example.tvinput.utils.Logger;

import java.util.ArrayList;

/**
 * Abstract class that contains mutual methods for EPG runnable classes
 */
public abstract class EpgRunnable implements Runnable {

    /** Object used to write to logcat output */
    private final Logger mLog = new Logger(TvService.APP_NAME + EpgRunnable.class.getSimpleName(),
            Logger.ERROR);
    /** Domain used for content rating */
    private static final String DOMAIN = "com.android.tv";
    /** Content rating system */
    private static final String RATING_SYSTEM = "DVB";
    /** Projection for DB filling */
    private static final String[] projection = {
        TvContract.Programs.COLUMN_TITLE
    };
    protected int mServiceIndex;
    protected Long mFrequency;
    /** Application context */
    protected final Context mContext;
    /** DvbManager for accessing middleware API */
    protected Manager mDtvManager;
    /** Channel Manager */
    private ChannelManager mChannelManager;

    /**
     * Contructor
     * 
     * @param context Application context
     * @param channelId Channel id for Now/Next event
     */
    protected EpgRunnable(Context context) {
        mContext = context;
        mDtvManager = Manager.getInstance();
        mChannelManager = mDtvManager.getChannelManager();
    }

    /**
     * Convert DVB rating from middleware values to predefined String constants
     * 
     * @param rate DVB rate of the current program
     * @return Converted rate to String constant
     */
    public static String convertDVBRating(int rate) {
        switch (rate) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                return "DVB_4";
            case 5:
                return "DVB_5";
            case 6:
                return "DVB_6";
            case 7:
                return "DVB_7";
            case 8:
                return "DVB_8";
            case 9:
                return "DVB_9";
            case 10:
                return "DVB_10";
            case 11:
                return "DVB_11";
            case 12:
                return "DVB_12";
            case 13:
                return "DVB_13";
            case 14:
                return "DVB_14";
            case 15:
                return "DVB_15";
            case 16:
                return "DVB_16";
            case 17:
                return "DVB_17";
            case 18:
                return "DVB_18";
            default:
                return "DVB_4";
        }
    }

    /**
     * Convert program genre from middleware values to predifined constants
     * 
     * @param genre Genre of the program
     * @return String value of the program
     */
    public static String convertDVBGenre(int genre) {
        switch (genre) {
            case 0x1:
                return "MOVIES";
            case 0x2:
                return "NEWS";
            case 0x3:
                // epg_genre_show_game_show;
                return "GAMING";
            case 0x4:
                return "SPORTS";
            case 0x5:
                return "FAMILY_KIDS";
            case 0x6:
                // epg_genre_music_ballet_dance;
                return "DRAMA";
            case 0x7:
                // epg_genre_arts_culture;
                return "EDUCATION";
            case 0x8:
                // epg_genre_social_political_issues;
                return "NEWS";
            case 0x9:
                return "EDUCATION";
            case 0xA:
                // epg_genre_leisure_hobbies;
                return "TRAVEL";
            default:
                return "ANIMAL_WILDLIFE";
        }
    }

    private ContentValues makeProgramContentValues(EpgEvent event, int channelIndex) {
        mLog.d("[makeProgramContentValues] " + channelIndex);
        mLog.d("[makeProgramContentValues] " + event);
        long startTimeMilis, endTimeMilis, dirationMilis;
        String genre;
        TvContentRating rating;
        TvContentRating[] contentRatings;
        EpgProgram tempProg = null;
        String longDesc = "";
        IEpgControl epgControl = mDtvManager.getEpgControl();
        ChannelDescriptor channel = mChannelManager.getChannelByIndex(channelIndex - 1);
        if (channel == null) {
            mLog.e("[makeProgramContentValues][channel not found]");
            return null;
        }
        if (event == null) {
            mLog.e("[makeProgramContentValues][event is null]");
            return null;
        }
        startTimeMilis = event.getStartTime().getCalendar().getTimeInMillis();
        endTimeMilis = event.getEndTime().getCalendar().getTimeInMillis();
        dirationMilis = endTimeMilis - startTimeMilis;
        if (dirationMilis <= 0) {
            mLog.e("[makeProgramContentValues][duration value is invalid]");
            return null;
        }
        if (checkifExist(channel.getChannelId(), startTimeMilis, endTimeMilis)) {
            mLog.w("[makeProgramContentValues][program exist]");
            return null;
        }
        rating = TvContentRating.createRating(DOMAIN, RATING_SYSTEM,
                convertDVBRating(event.getParentalRate()));
        contentRatings = new TvContentRating[] {
            rating
        };
        genre = convertDVBGenre(event.getGenre());
        longDesc = epgControl.getEventExtendedDescription(mDtvManager.getEpgManager()
                .getEpgFilterID(), ((AlEpgEvent) event).getEventId(), channelIndex);
        tempProg = new EpgProgram.Builder().setChannelId(channel.getChannelId())
                .setTitle(event.getName()).setCanonicalGenres(genre)
                .setDescription(event.getDescription()).setLongDescription(longDesc)
                .setStartTimeUtcMillis(startTimeMilis).setEndTimeUtcMillis(endTimeMilis)
                .setContentRatings(contentRatings).build();
        return tempProg.toContentValues();
    }

    protected boolean addProgram(EpgEvent event, int channelIndex) {
        ContentValues values = makeProgramContentValues(event, channelIndex);
        if (values == null) {
            return false;
        }
        mLog.d("[addProgram][begin]");
        Uri uri = mContext.getContentResolver().insert(TvContract.Programs.CONTENT_URI, values);
        mLog.d("[addProgram][end] " + uri);
        return true;
    }

    protected void addPrograms(ArrayList<EpgEvent> events, int channelIndex) {
        ArrayList<ContentValues> list = new ArrayList<ContentValues>();
        for (EpgEvent event : events) {
            ContentValues values = makeProgramContentValues(event, channelIndex);
            if (values != null) {
                list.add(values);
            }
        }
        ContentValues array[] = new ContentValues[list.size()];
        list.toArray(array);
        mLog.d("[addPrograms][begin]");
        mContext.getContentResolver().bulkInsert(TvContract.Programs.CONTENT_URI, array);
        mLog.d("[addPrograms][end]");
    }

    /**
     * This method is used to check if the current event is already present in
     * the DB
     * 
     * @param program Program to check in DB
     * @return True if the program is present in the DB, false otherwise
     */
    protected boolean checkifExist(long channelID, long startTime, long endTime) {
        Uri uri = TvContract.buildProgramsUriForChannel(channelID, startTime, endTime);
        Cursor cursor = mContext.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null || cursor.getCount() == 0) {
            cursor.close();
            mLog.w("[checkifExist][item does not exist in DB][[uri: " + uri.toString() + "]");
            return false;
        } else {
            cursor.close();
            return true;
        }
        /**
         * TODO Implement program update
         */
    }

    protected void dumpEvent(EpgEvent event) {
        mLog.d("Event ID: " + ((AlEpgEvent) event).getEventId());
        mLog.d("Event Desc: " + event.getDescription());
        mLog.d("Event StartTime: " + event.getStartTime().toString());
        mLog.d("Event EndTime: " + event.getEndTime().toString());
    }
}

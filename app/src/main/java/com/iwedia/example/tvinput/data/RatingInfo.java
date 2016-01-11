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
package com.iwedia.example.tvinput.data;

import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.media.tv.TvContract.Programs;
import android.net.Uri;

import com.iwedia.example.tvinput.TvService;
import com.iwedia.example.tvinput.utils.Logger;

/**
 * A convenience class for building content rating information.
 */
public class RatingInfo {

    /** Object used to write to logcat output */
    private final static Logger mLog = new Logger(TvService.APP_NAME
            + RatingInfo.class.getSimpleName(), Logger.ERROR);
    public final TvContentRating rating;
    public final long expires;

    private RatingInfo(TvContentRating rating, long expires) {
        this.rating = rating;
        this.expires = expires;
    }

    public static RatingInfo buildRatingInfo(Context context, ChannelDescriptor channel) {
        if (channel == null) {
            return null;
        }
        Uri uri = TvContract.buildProgramsUriForChannel(channel.getChannelId());
        String time = String.valueOf(System.currentTimeMillis());
        String selection = Programs.COLUMN_START_TIME_UTC_MILLIS + "<=? AND "
                + Programs.COLUMN_END_TIME_UTC_MILLIS + ">?";
        String[] args = {
                time, time
        };
        Cursor cursor = null;
        //Sometimes TIF service will throw exception when trying to use selection on programs table
        try {
            cursor = context.getContentResolver().query(uri, null, selection, args, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long eventEndTime = 0;
        String eventRating = null;
        mLog.d("CRR - id=" + channel.getChannelId());
        mLog.d("CRR - time=" + time);
        if (cursor == null) {
            mLog.d("CRR - no cursor!");
        } else {
            mLog.d("CRR - count=" + cursor.getCount());
            if (cursor.moveToFirst()) {
                eventEndTime = cursor.getLong(cursor.getColumnIndex(
                        Programs.COLUMN_END_TIME_UTC_MILLIS));
                eventRating = cursor.getString(cursor.getColumnIndex(
                        Programs.COLUMN_CONTENT_RATING));
                mLog.d("CRR - event.title="
                        + cursor.getString(cursor.getColumnIndex(Programs.COLUMN_TITLE)));
                mLog.d("CRR - event.rating=" + eventRating);
                mLog.d("CRR - event.endTime=" + eventEndTime);
                return new RatingInfo(
                        (eventRating == null)
                                ? null
                                : TvContentRating.unflattenFromString(eventRating),
                        eventEndTime);
            }
            cursor.close();
        }
        return null;
    }
}

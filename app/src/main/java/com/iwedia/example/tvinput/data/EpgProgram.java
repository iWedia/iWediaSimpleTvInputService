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

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.os.Build;

/**
 * A convenience class to create and insert program information entries into the database.
 */
public final class EpgProgram {

    public static final long INVALID_ID = -1;
    private long mChannelId;
    private String mTitle;
    private long mStartTimeUtcMillis;
    private long mEndTimeUtcMillis;
    private String mDescription;
    private String mLongDescription;
    private String mCanonicalGenres;
    private TvContentRating[] mContentRatings;

    private EpgProgram() {
        // Do nothing.
    }

    public long getChannelId() {
        return mChannelId;
    }

    public String getTitle() {
        return mTitle;
    }

    public long getStartTimeUtcMillis() {
        return mStartTimeUtcMillis;
    }

    public long getEndTimeUtcMillis() {
        return mEndTimeUtcMillis;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getLongDescription() {
        return mLongDescription;
    }

    public TvContentRating[] getContentRatings() {
        return mContentRatings;
    }

    public String[] getCanonicalGenres() {
        if (mCanonicalGenres == null) {
            return null;
        }
        return TvContract.Programs.Genres.decode(mCanonicalGenres);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(TvContract.Programs.COLUMN_CHANNEL_ID, mChannelId);
        values.put(TvContract.Programs.COLUMN_TITLE, mTitle);
        values.put(TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS, mStartTimeUtcMillis);
        values.put(TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS, mEndTimeUtcMillis);
        values.put(TvContract.Programs.COLUMN_SHORT_DESCRIPTION, mDescription);
        values.put(TvContract.Programs.COLUMN_LONG_DESCRIPTION, mLongDescription);
        values.put(TvContract.Programs.COLUMN_CANONICAL_GENRE, mCanonicalGenres);
        values.put(TvContract.Programs.COLUMN_CONTENT_RATING,
                contentRatingsToString(mContentRatings));
        return values;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("Program{")
                .append(", channelId=").append(mChannelId)
                .append(", title=").append(mTitle)
                .append(", startTimeUtcSec=").append(mStartTimeUtcMillis)
                .append(", endTimeUtcSec=").append(mEndTimeUtcMillis)
                .append(", description=").append(mDescription)
                .append(", longDescription=").append(mLongDescription)
                .append(", canonicalGenres=").append(mCanonicalGenres)
                .append(", contentRatings=").append(contentRatingsToString(mContentRatings))
                .append("}")
                .toString();
    }

    public void copyFrom(EpgProgram other) {
        if (this == other) {
            return;
        }
        mChannelId = other.mChannelId;
        mTitle = other.mTitle;
        mStartTimeUtcMillis = other.mStartTimeUtcMillis;
        mEndTimeUtcMillis = other.mEndTimeUtcMillis;
        mDescription = other.mDescription;
        mLongDescription = other.mLongDescription;
        mCanonicalGenres = other.mCanonicalGenres;
    }

    public static final class Builder {

        private final EpgProgram mProgram;

        public Builder() {
            mProgram = new EpgProgram();
            // Fill initial data.
            mProgram.mChannelId = -1;
            mProgram.mTitle = "title";
            mProgram.mStartTimeUtcMillis = -1;
            mProgram.mEndTimeUtcMillis = -1;
            mProgram.mDescription = "description";
            mProgram.mLongDescription = "long_description";
        }

        public Builder(EpgProgram other) {
            mProgram = new EpgProgram();
            mProgram.copyFrom(other);
        }

        public Builder setChannelId(long channelId) {
            mProgram.mChannelId = channelId;
            return this;
        }

        public Builder setTitle(String title) {
            mProgram.mTitle = title;
            return this;
        }

        public Builder setStartTimeUtcMillis(long startTimeUtcMillis) {
            mProgram.mStartTimeUtcMillis = startTimeUtcMillis;
            return this;
        }

        public Builder setEndTimeUtcMillis(long endTimeUtcMillis) {
            mProgram.mEndTimeUtcMillis = endTimeUtcMillis;
            return this;
        }

        public Builder setDescription(String description) {
            mProgram.mDescription = description;
            return this;
        }

        public Builder setLongDescription(String longDescription) {
            mProgram.mLongDescription = longDescription;
            return this;
        }

        public Builder setContentRatings(TvContentRating[] contentRatings) {
            mProgram.mContentRatings = contentRatings;
            return this;
        }

        public Builder setCanonicalGenres(String genres) {
            mProgram.mCanonicalGenres = genres;
            return this;
        }

        public EpgProgram build() {
            return mProgram;
        }
    }

    private String contentRatingsToString(TvContentRating[] contentRatings) {
        if (contentRatings == null || contentRatings.length == 0) {
            return null;
        }
        final String DELIMITER = ",";
        StringBuilder ratings = new StringBuilder(contentRatings[0].flattenToString());
        for (int i = 1; i < contentRatings.length; ++i) {
            ratings.append(DELIMITER);
            ratings.append(contentRatings[i].flattenToString());
        }
        return ratings.toString();
    }
}

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

import android.content.ContentValues;
import android.database.Cursor;
import android.media.tv.TvContract.Channels;
import android.net.Uri;

import com.iwedia.dtv.service.ServiceType;
import com.iwedia.dtv.service.SourceType;

/**
 * A convenience class for storing channel description
 */
public class ChannelDescriptor {

    // @SystemApi : Channels.COLUMN_BROWSABLE not accessible from application
    public static final String COLUMN_BROWSABLE = "browsable";
    public static final int EC_NOT_AVAILABLE = -1;
    /** Channel ID == service_id in database */
    private long mId;
    /** Channel number in "01" format */
    private final String mDisplayNumber;
    /** Channel name for displaying */
    private final String mName;
    /** Channel descriptor type */
    private final SourceType mType;
    /** Channel URL if describing IP channel */
    private final String mUrl;
    /** ID for channels playing from MW */
    private int mServiceId;
    /** Type of service  [TV,RADIO ...] */
    private final ServiceType mServiceType;

    /**
     * Constructor for DVB channel
     *
     * @param displayNumber Channel number
     * @param name          Channel name
     */
    public ChannelDescriptor(String displayNumber, String name, int serviceId,
            SourceType type, ServiceType serviceType) {
        mDisplayNumber = displayNumber;
        mName = name;
        mServiceId = serviceId;
        mUrl = "";
        mType = type;
        mServiceType = serviceType;
    }

    /**
     * Constructor for IP channel
     *
     * @param displayNumber Channel number
     * @param url           Channel url
     * @param name          Channel name
     */
    public ChannelDescriptor(String displayNumber, String url) {
        mDisplayNumber = displayNumber;
        mName = "";
        mUrl = url;
        mType = SourceType.IP;
        mServiceId = EC_NOT_AVAILABLE;
        mServiceType = ServiceType.DIG_TV;
    }

    public ChannelDescriptor(Cursor cursor) {
        mId = cursor.getInt(cursor.getColumnIndex(Channels._ID));
        mDisplayNumber = cursor.getString(cursor
                .getColumnIndex(Channels.COLUMN_DISPLAY_NUMBER));
        mType = convertTifTypeToSourceType(cursor.getString(cursor
                .getColumnIndex(Channels.COLUMN_TYPE)));
        mServiceType = convertTifServiceTypeToServiceType(cursor.getString(cursor
                .getColumnIndex(Channels.COLUMN_SERVICE_TYPE)));
        if (mType == SourceType.IP) {
            mUrl = cursor.getString(cursor
                    .getColumnIndex(Channels.COLUMN_DISPLAY_NAME));
            mName = "";
        } else {
            mName = cursor.getString(cursor
                    .getColumnIndex(Channels.COLUMN_DISPLAY_NAME));
            mUrl = "";
        }
        mServiceId = cursor.getInt(cursor
                .getColumnIndex(Channels.COLUMN_SERVICE_ID));
    }

    public ContentValues getContentValues(String inputId) {
        ContentValues ret = new ContentValues();
        ret.put(Channels.COLUMN_DISPLAY_NUMBER, mDisplayNumber);
        if (mType == SourceType.IP) {
            ret.put(Channels.COLUMN_DISPLAY_NAME, mUrl);
        } else {
            ret.put(Channels.COLUMN_DISPLAY_NAME, mName);
        }
        ret.put(Channels.COLUMN_TYPE, convertSourceTypeToTifType(mType));
        ret.put(Channels.COLUMN_SERVICE_ID, mServiceId);
        ret.put(Channels.COLUMN_INPUT_ID, inputId);
        ret.put(Channels.COLUMN_ORIGINAL_NETWORK_ID, mId);
        ret.put(Channels.COLUMN_SERVICE_TYPE, convertServiceTypeToTifServiceType(mServiceType));
        ret.put(Channels.COLUMN_SEARCHABLE, 1);
        ret.put(COLUMN_BROWSABLE, 1);
        return ret;
    }

    private String convertSourceTypeToTifType(SourceType sourceType) {
        switch (sourceType) {
            case TER:
                return Channels.TYPE_DVB_T;
            case CAB:
                return Channels.TYPE_DVB_C;
            case SAT:
                return Channels.TYPE_DVB_S;
            default:
                return Channels.TYPE_OTHER;
        }
    }

    private SourceType convertTifTypeToSourceType(String type) {
        switch (type) {
            case Channels.TYPE_DVB_T2:
            case Channels.TYPE_DVB_T:
                return SourceType.TER;
            case Channels.TYPE_DVB_C2:
            case Channels.TYPE_DVB_C:
                return SourceType.CAB;
            case Channels.TYPE_DVB_S2:
            case Channels.TYPE_DVB_S:
                return SourceType.SAT;
            default:
                return SourceType.IP;
        }
    }

    private String convertServiceTypeToTifServiceType(ServiceType serviceType) {
        switch (serviceType) {
            case DIG_RAD:
                return Channels.SERVICE_TYPE_AUDIO;
            case DIG_TV:
                return Channels.SERVICE_TYPE_AUDIO_VIDEO;
            default:
                return Channels.SERVICE_TYPE_OTHER;
        }
    }

    private ServiceType convertTifServiceTypeToServiceType(String type) {
        switch (type) {
            case Channels.SERVICE_TYPE_AUDIO_VIDEO:
                return ServiceType.DIG_TV;
            case Channels.SERVICE_TYPE_AUDIO:
                return ServiceType.DIG_RAD;
            default:
                return ServiceType.UNDEFINED;
        }
    }

    /**
     * Gets the channel number
     *
     * @return the mNumber
     */
    public String getDisplayNumber() {
        return mDisplayNumber;
    }

    /**
     * Gets the channel name
     *
     * @return the mName
     */
    public String getName() {
        return mName;
    }

    /**
     * Gets the channel Url
     *
     * @return the mUrl
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * Get service ID. This id can be used for tune DVB operation.
     *
     * @return service ID in * channel list
     */
    public int getServiceId() {
        return mServiceId;
    }

    /**
     * Gets the channel type
     *
     * @return the mType
     */
    public SourceType getType() {
        return mType;
    }

    /**
     * Gets the channel ID
     *
     * @return the mId
     */
    public long getChannelId() {
        return mId;
    }

    /**
     * Gets the channel Uri
     *
     * @return the mId
     */
    public Uri getChannelUri() {
        return Uri.parse(String.valueOf(mId));
    }

    /**
     * Sets the channel ID
     *
     * @arg channelID new channel ID
     */
    public void setId(long channelID) {
        mId = channelID;
    }

    public ServiceType getServiceType() {
        return mServiceType;
    }

    @Override
    public String toString() {
        if (mType == SourceType.IP) {
            return "url: " + mUrl + ", diplay number: " + mDisplayNumber
                    + ", id: " + mId;
        } else {
            return "name: " + mName + ", display number: " + mDisplayNumber
                    + ", id: " + mId + ", service id: " + mServiceId;
        }
    }
}

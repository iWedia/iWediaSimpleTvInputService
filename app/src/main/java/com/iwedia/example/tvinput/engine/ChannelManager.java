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

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.media.tv.TvContract.Channels;
import android.net.Uri;

import com.iwedia.dtv.dtvmanager.IDTVManager;
import com.iwedia.dtv.route.broadcast.IBroadcastRouteControl;
import com.iwedia.dtv.route.broadcast.RouteFrontendType;
import com.iwedia.dtv.route.broadcast.RouteInstallSettings;
import com.iwedia.dtv.scan.FecType;
import com.iwedia.dtv.scan.IScanControl;
import com.iwedia.dtv.scan.Modulation;
import com.iwedia.dtv.scan.Polarization;
import com.iwedia.dtv.scan.RollOff;
import com.iwedia.dtv.service.IServiceControl;
import com.iwedia.dtv.service.ServiceDescriptor;
import com.iwedia.dtv.service.SourceType;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.example.tvinput.TvService;
import com.iwedia.example.tvinput.data.ChannelDescriptor;
import com.iwedia.example.tvinput.utils.ChannelUtils;
import com.iwedia.example.tvinput.utils.ExampleSwitches;
import com.iwedia.example.tvinput.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Manager for all channel related operations
 */
public class ChannelManager {

    /**
     * Object used to write to logcat output
     */
    private final Logger mLog = new Logger(TvService.APP_NAME
            + ChannelManager.class.getSimpleName(), Logger.ERROR);
    /**
     * Column flag to allow browsing channels from Google TV App
     */
    public static final int EC_ID_NOT_FOUND = -1;
    /**
     * All channels
     */
    private ArrayList<ChannelDescriptor> mAllChannels;
    /**
     * List of IP channels. Key is Channel ID
     */
    private ArrayList<ChannelDescriptor> mIpOnlyChannels = null;
    /**
     * ID of TV Input
     */
    private String mInputId;
    /**
     * Application context
     */
    private Context mContext;
    /**
     * DVB manager, entry point for MW
     */
    private DtvManager mDvbManager;
    private IDTVManager mDTVManger;
    private RouteManager mRouteManager;
    private IScanControl mScanControl;

    private IBroadcastRouteControl mBroadcastRouteControl;

    /**
     * Constructor
     *
     * @param context Application context
     */
    public ChannelManager(DtvManager dvbManager, Context context) {
        mContext = context;
        mDvbManager = dvbManager;
        mDTVManger = mDvbManager.getDtvManager();
        mBroadcastRouteControl = mDTVManger.getBroadcastRouteControl();
        mRouteManager = mDvbManager.getRouteManager();
        mScanControl = mDvbManager.getDtvManager().getScanControl();
        mInputId = TvContract.buildInputId(new ComponentName(mContext,
                TvService.class));
        mIpOnlyChannels = new ArrayList<ChannelDescriptor>();
    }

    /**
     * Initialize channel list
     */
    public void init() {
        mLog.v("initialize ChannelManager");
        mAllChannels = loadChannels(mInputId);
        if (ExampleSwitches.ENABLE_DEFAULT_HLS_CHANNELS) {
            ChannelUtils.initIpChannels(mContext);
            ChannelUtils.readIpChannels(mContext, mIpOnlyChannels);
        }
        if (mAllChannels.isEmpty()) {
            mLog.i("[initialize][first time initialization]");
            refreshChannelList();
        }
        print(mAllChannels);
    }

    /**
     * Gets channel by given uri
     *
     * @param channelUri
     * @param isRetry
     * @return
     */
    public ChannelDescriptor getChannelById(long id) {
        mLog.d("[getChannelByUri][" + id + "]");
        for (ChannelDescriptor cd : mAllChannels) {
            if (cd.getChannelId() == id) {
                return cd;
            }
        }
        return null;
    }

    public ChannelDescriptor getChannelByIndex(int channelIndex) {
        mLog.d("[getChannelByIndex][" + channelIndex + "]");
        try {
            return mAllChannels.get(channelIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<ChannelDescriptor> loadChannels(String inputId) {
        ArrayList<ChannelDescriptor> ret = new ArrayList<ChannelDescriptor>();
        final String[] projection = {
                Channels._ID,
                Channels.COLUMN_DISPLAY_NAME, Channels.COLUMN_DISPLAY_NUMBER,
                Channels.COLUMN_SERVICE_ID, Channels.COLUMN_TYPE, Channels.COLUMN_SERVICE_TYPE
        };
        Cursor cursor = mContext.getContentResolver().query(
                TvContract.buildChannelsUriForInput(mInputId), projection,
                null, null, null);
        if (cursor == null) {
            return ret;
        }
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ChannelDescriptor cd = new ChannelDescriptor(cursor);
            mLog.d("[loadChannels] index=" + cd.getChannelId() + " info=" + cd);
            ret.add(cd);
            cursor.moveToNext();
        }
        cursor.close();
        return ret;
    }

    /**
     * Inserts channels into TvProvider database
     *
     * @param context  of a service
     * @param inputId  this TV input service
     * @param channels to be inserted into a TVProvider database
     */
    private void storeChannels(String inputId, List<ChannelDescriptor> channels) {
        final String[] projection = {
                TvContract.Channels._ID,
                TvContract.Channels.COLUMN_DISPLAY_NAME,
                TvContract.Channels.COLUMN_DISPLAY_NUMBER,
        };
        Cursor cursor = mContext.getContentResolver().query(
                TvContract.buildChannelsUriForInput(mInputId), projection,
                null, null, null);
        if (cursor == null) {
            mLog.e("[storeChannels][cursor is null");
            return;
        }
        for (ChannelDescriptor channel : channels) {
            Uri retUri = mContext.getContentResolver().insert(
                    TvContract.Channels.CONTENT_URI,
                    channel.getContentValues(inputId));
            if (retUri == null) {
                mLog.e("[storeChannels][error adding channel to the database");
            } else {
                channel.setId(ContentUris.parseId(retUri));
                mLog.i("[storeChannels][add channel][" + channel + "]");
            }
        }
        cursor.close();
    }

    public void refreshChannelList() {
        int displayNumber = 1;
        String formattedChannelNumber = "";
        List<ChannelDescriptor> channels = new ArrayList<ChannelDescriptor>();
        IServiceControl serviceControl = mDTVManger.getServiceControl();
        mAllChannels = new ArrayList<ChannelDescriptor>();
        // 1) Delete all channels from TV provider database
        mContext.getContentResolver().delete(
                TvContract.buildChannelsUriForInput(mInputId), null, null);
        mContext.getContentResolver().delete(TvContract.Programs.CONTENT_URI,
                null, null);
        // 2) Add DVB channels founded from scan
        int channelListSize = getChannelListSize();
        if (mRouteManager.getLiveRouteIp() != RouteManager.EC_INVALID_ROUTE) {
            channelListSize -= mIpOnlyChannels.size();
        }
        // ! Limitation: support only 1 DVB route in this example
        SourceType type = SourceType.UNDEFINED;
        if (mRouteManager.getInstallRouteCab() != RouteManager.EC_INVALID_ROUTE) {
            type = SourceType.CAB;
        } else if (mRouteManager.getInstallRouteTer() != RouteManager.EC_INVALID_ROUTE) {
            type = SourceType.TER;
        } else if (mRouteManager.getInstallRouteSat() != RouteManager.EC_INVALID_ROUTE) {
            type = SourceType.SAT;
        }
        for (int i = 0; i < channelListSize; i++) {
            // ! If there is IP first element in service list (use case with
            // Hybrid tuner) it's a DUMMY channel
            int properIndex = i;
            if (mRouteManager.getLiveRouteIp() != RouteManager.EC_INVALID_ROUTE) {
                properIndex++;
            }
            ServiceDescriptor servDesc = serviceControl.getServiceDescriptor(
                    DtvManager.MASTER_LIST_INDEX, properIndex);
            if(ExampleSwitches.ENABLE_IWEDIA_EMU){
                // Accept only video channels
                switch(servDesc.getServiceType()){
                    case DIG_RAD:
                    case ADV_CODEC_DIG_RAD:
                        continue;
                    default:                    
                }
            }
            formattedChannelNumber = String.format(Locale.ENGLISH, "%02d", displayNumber);
            channels.add(new ChannelDescriptor(formattedChannelNumber, servDesc
                    .getName(), servDesc.getMasterIndex(), type, servDesc.getServiceType()));
            displayNumber++;
        }
        // Add IP channels to list
        for (ChannelDescriptor ipService : mIpOnlyChannels) {
            formattedChannelNumber = String.format(Locale.ENGLISH, "%02d",
                    displayNumber);
            channels.add(new ChannelDescriptor(formattedChannelNumber,
                    ipService.getUrl()));
            displayNumber++;
        }
        print(channels);
        // Save channels to TV provider database
        storeChannels(mInputId, channels);
        // Load channels to TIF memory
        mAllChannels = loadChannels(mInputId);
    }

    private void print(HashMap<Long, ChannelDescriptor> channels) {
        Iterator<Entry<Long, ChannelDescriptor>> it = channels.entrySet()
                .iterator();
        while (it.hasNext()) {
            Map.Entry<Long, ChannelDescriptor> pair = (Entry<Long, ChannelDescriptor>) it
                    .next();
            mLog.d(pair.getValue().toString());
        }
    }

    private void print(List<ChannelDescriptor> channels) {
        for (ChannelDescriptor channel : channels) {
            mLog.d(channel.toString());
        }
    }

    public void startScan() throws InternalException {
        if (mRouteManager.getInstallRouteCab() != ChannelManager.EC_ID_NOT_FOUND) {
            mScanControl.autoScan(mDvbManager.getRouteManager().getInstallRouteCab());
            mRouteManager.updateCurrentInstallRoute(mRouteManager.getInstallRouteCab());
        } else if (mRouteManager.getInstallRouteTer() != ChannelManager.EC_ID_NOT_FOUND) {
            RouteInstallSettings settings = new RouteInstallSettings();
            settings.setFrontendType(RouteFrontendType.TER);
            mBroadcastRouteControl.configureInstallRoute(mDvbManager.getRouteManager()
                    .getInstallRouteTer(), settings);
            mScanControl.autoScan(mDvbManager.getRouteManager().getInstallRouteTer());
            mRouteManager.updateCurrentInstallRoute(mRouteManager.getInstallRouteTer());
        } else if (mRouteManager.getInstallRouteSat() != ChannelManager.EC_ID_NOT_FOUND) {
            mScanControl.setFrequency(11156);
            mScanControl.setSymbolRate(12000);
            mScanControl.setPolarization(Polarization.HORIZONTAL);
            mScanControl.setModulation(Modulation.MODULATION_QPSK);
            mScanControl.setFecType(FecType.FEC_3_4);
            mScanControl.setRollOff(RollOff.ROLL_OFF_35);
            mScanControl.manualScan(mRouteManager.getInstallRouteSat());
            mRouteManager.updateCurrentInstallRoute(mRouteManager.getInstallRouteSat());
        } else if (mRouteManager.getInstallRouteIp() != ChannelManager.EC_ID_NOT_FOUND) {
            // Usually operator specific scan goes here
        }
    }

    public void stopScan() throws InternalException {
        mScanControl.abortScan(mRouteManager.getCurrentInstallRoute());
    }

    /**
     * Get Size of Channel List.
     */
    public int getChannelListSize() {
        int serviceCount = mDTVManger.getServiceControl().getServiceListCount(
                DtvManager.MASTER_LIST_INDEX);
        if (mRouteManager.getLiveRouteIp() != RouteManager.EC_INVALID_ROUTE) {
            serviceCount += mIpOnlyChannels.size();
            // for Dummy channel which is used for MEDIA playback
            serviceCount--;
        }
        return serviceCount;
    }

    public int getDtvChannelListSize() {
        int serviceCount = mDTVManger.getServiceControl().getServiceListCount(
                DtvManager.MASTER_LIST_INDEX);
        if (mRouteManager.getLiveRouteIp() != RouteManager.EC_INVALID_ROUTE) {
            // for Dummy channel which is used for MEDIA playback
            serviceCount--;
        }
        return serviceCount;
    }
}

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


import android.os.RemoteException;

import com.iwedia.example.tvinput.TvService;
import com.iwedia.example.tvinput.utils.Logger;
import com.iwedia.dtv.dtvmanager.IDTVManager;
import com.iwedia.dtv.route.broadcast.RouteComponentType;
import com.iwedia.dtv.route.broadcast.RouteDemuxDescriptor;
import com.iwedia.dtv.route.broadcast.RouteFrontendDescriptor;
import com.iwedia.dtv.route.broadcast.RouteFrontendType;
import com.iwedia.dtv.route.broadcast.RouteLiveSettings;
import com.iwedia.dtv.route.broadcast.RouteMassStorageDescriptor;
import com.iwedia.dtv.route.broadcast.routemanager.InstallRoutes;
import com.iwedia.dtv.route.broadcast.routemanager.LiveRoutes;
import com.iwedia.dtv.route.broadcast.routemanager.PlaybackRoutes;
import com.iwedia.dtv.route.broadcast.routemanager.RecordRoutes;
import com.iwedia.dtv.route.broadcast.routemanager.Routes;
import com.iwedia.dtv.route.common.RouteDecoderDescriptor;
import com.iwedia.dtv.route.common.RouteInputOutputDescriptor;
import com.iwedia.dtv.types.VideoPosition;
import com.iwedia.dtv.service.SourceType;

import java.util.EnumSet;

public class RouteManager {

    private static RouteManager instance;

    private final Logger mLog = new Logger(TvService.APP_NAME + RouteManager.class.getSimpleName(),
            Logger.ERROR);

    public static final int DEMUX_ID_NOT_USED_WITH_COMEDIA = 0;

    private Routes mIpPrimaryRoutes = null;
    private Routes mIpSecondaryRoutes = null;
    private Routes mIpPipRoutes = null;
    private Routes mTerLiveRoutes = null;

    private PlaybackRoutes mPlaybackMainRoute = null;
    private PlaybackRoutes mPlaybackPipRoute = null;

    private IDTVManager mDtvManager = null;

    private InstallRoutes mInstallRoutes[];
    private LiveRoutes mLiveRoutes[];
    private RecordRoutes mRecordRoutes[];
    private PlaybackRoutes mPlaybackRoutes[];

    private final int IP_FRONTEND = 1;

    /**
     * Initialize RouteManager.
     */
    public RouteManager(IDTVManager dtvManager) {
        try {
            mDtvManager = dtvManager;
            initializeRouteIds();
        } catch (RemoteException e) {
            mLog.e("Error initializing route manager");
            e.printStackTrace();
        }

        mLog.i("Route manager initialized");
    }

    /**
     * Initialize routes.
     * 
     * @return true if routes initialized correctly, false otherwise
     * @throws RemoteException
     */
    public boolean initializeRouteIds() throws RemoteException {
        mLog.d("[initializeRouteIds]");

        // 1) Get number of components
        long feNum = 0;
        feNum = mDtvManager.getBroadcastRouteControl().getFrontendNumber();

        long storageNum = 0;
        storageNum = mDtvManager.getBroadcastRouteControl().getMassStorageNumber();

        long decNum = 0;
        decNum = mDtvManager.getCommonRouteControl().getDecoderNumber();

        long inputOutputNum = 0;
        inputOutputNum = mDtvManager.getCommonRouteControl().getInputOutputNumber();

        mLog.d("[initializeRouteIds][" + feNum + ", " + storageNum
                + ", " + decNum + ", "
                + inputOutputNum + ", " + "]");

        // 2) allocate memory
        int installNum = 0, liveNum = 0, recordNum = 0, playbackNum = 0;

        mInstallRoutes = null;
        installNum = (int) feNum;
        if (installNum > 0) {
            mInstallRoutes = new InstallRoutes[installNum];
        }

        mLiveRoutes = null;
        liveNum = (int) feNum * (int) decNum * (int) inputOutputNum;
        if (liveNum > 0) {
            mLiveRoutes = new LiveRoutes[liveNum];
        }

        mRecordRoutes = null;
        recordNum = (int) feNum * (int) storageNum;
        if (recordNum > 0) {
            mRecordRoutes = new RecordRoutes[recordNum];
        }

        mPlaybackRoutes = null;
        playbackNum = (int) storageNum * (int) decNum
                * (int) inputOutputNum;
        if (playbackNum > 0) {
            mPlaybackRoutes = new PlaybackRoutes[playbackNum];
        }

        // 3) Install routes
        int installIndex = 0;

        mLog.d("[initializeRouteIds][install routes]");
        for (long frontendLoop = 0; frontendLoop < feNum; frontendLoop++) {
            RouteFrontendDescriptor frontedDesc;
            frontedDesc = mDtvManager.getBroadcastRouteControl().getFrontendDescriptor(
                    (int) frontendLoop);

            mInstallRoutes[installIndex] = new InstallRoutes();
            mInstallRoutes[installIndex].route = mDtvManager.getBroadcastRouteControl()
                    .getInstallRoute(frontedDesc.getFrontendId(),
                            DEMUX_ID_NOT_USED_WITH_COMEDIA);
            mLog.d("[initializeRouteIds][GetInstallRoute] route: "
                    + mInstallRoutes[installIndex].route);

            mInstallRoutes[installIndex].frontend = frontedDesc;

            // TODO
            mInstallRoutes[installIndex].demux.setDemuxId(DEMUX_ID_NOT_USED_WITH_COMEDIA);

            mLog.d("[initializeRouteIds][frontend descriptior " + frontendLoop + "/"
                    + feNum + "][" + frontedDesc.getFrontendType() + "]");

            installIndex++;
        }

        // 4) Live routes
        int liveIndex = 0;
        mLog.d("[initializeRouteIds][live routes]");
        for (long frontendLoop = 0; frontendLoop < feNum; frontendLoop++) {
            RouteFrontendDescriptor frontedDesc;
            frontedDesc = mDtvManager.getBroadcastRouteControl().getFrontendDescriptor(
                    (int) frontendLoop);

            for (long decoderLoop = 0; decoderLoop < decNum; decoderLoop++) {
                RouteDecoderDescriptor decoderDesc;
                decoderDesc = mDtvManager.getCommonRouteControl().getDecoderDescriptor(
                        (int) decoderLoop);

                for (long outputLoop = 0; outputLoop < inputOutputNum; outputLoop++) {
                    RouteInputOutputDescriptor outputDesc;
                    outputDesc = mDtvManager.getCommonRouteControl().getInputOutputDescriptor(
                            (int) outputLoop);

                    mLiveRoutes[liveIndex] = new LiveRoutes();
                    mLiveRoutes[liveIndex].route = mDtvManager.getBroadcastRouteControl()
                            .getLiveRoute(
                                    frontedDesc.getFrontendId(),
                                    DEMUX_ID_NOT_USED_WITH_COMEDIA, decoderDesc.getDecoderId());
                    mLiveRoutes[liveIndex].frontend = frontedDesc;
                    mLiveRoutes[liveIndex].demux = new RouteDemuxDescriptor(
                            DEMUX_ID_NOT_USED_WITH_COMEDIA);
                    mLiveRoutes[liveIndex].decoder = decoderDesc;
                    mLiveRoutes[liveIndex].output = outputDesc;

                    mLog.d("[initializeRouteIds][Adding live route:" + frontedDesc.getFrontendId()
                            + ", de:"
                            + decoderDesc.getDecoderId() + ", out:" + outputDesc.getInputOutputId()
                            + ", ro:" + mLiveRoutes[liveIndex].route + "]");

                    liveIndex++;
                }
            }
        }

        // 5. Record routes
        int recordIndex = 0;
        mLog.d("[initializeRouteIds][record routes]");
        // iterate through all frontends
        for (long frontendLoop = 0; frontendLoop < feNum; frontendLoop++) {

            // create frontend descriptor
            RouteFrontendDescriptor frontendDesc;
            frontendDesc = mDtvManager.getBroadcastRouteControl()
                    .getFrontendDescriptor((int) frontendLoop);

            // iterate through all mass storages
            for (long storageLoop = 0; storageLoop < storageNum; storageLoop++) {

                // create mass storage descriptor
                RouteMassStorageDescriptor massStorageDesc;
                massStorageDesc = mDtvManager.getBroadcastRouteControl().getMassStorageDescriptor(
                        (int) storageLoop
                        );

                // create record route
                mRecordRoutes[recordIndex] = new RecordRoutes();
                mRecordRoutes[recordIndex].route = mDtvManager.getBroadcastRouteControl()
                        .getRecordRoute(
                                frontendDesc.getFrontendId(),
                                DEMUX_ID_NOT_USED_WITH_COMEDIA,
                                massStorageDesc.getMassStorageId());

                mRecordRoutes[recordIndex].frontend = frontendDesc;
                mRecordRoutes[recordIndex].demux = new RouteDemuxDescriptor(
                        DEMUX_ID_NOT_USED_WITH_COMEDIA);
                mRecordRoutes[recordIndex].storage = massStorageDesc;

                mLog.d("[initializeRouteIds][Adding record route ["
                        + mRecordRoutes[recordIndex].route + "]:\n"
                        + "\tfrontend=" + frontendDesc.getFrontendId() + "\n"
                        + "\tmassStorage=" + massStorageDesc.getMassStorageId() + "\n"
                        + "\tdemuxId=" + DEMUX_ID_NOT_USED_WITH_COMEDIA);

                recordIndex++;
            }
        }

        // 6. Playback routes
        // create all posible playback route

        int playbackIndex = 0;
        mLog.d("[initializeRouteIds][playback routes]");
        // iterate through all mass storages
        for (long storageLoop = 0; storageLoop < storageNum; storageLoop++) {

            // create mass storage (needed as input)
            RouteMassStorageDescriptor massStorageDesc;
            massStorageDesc = mDtvManager.getBroadcastRouteControl().getMassStorageDescriptor(
                    (int) storageLoop);

            // iterate througl all decoders
            for (long decoderLoop = 0; decoderLoop < decNum; decoderLoop++) {

                // create decoder
                RouteDecoderDescriptor decoderDesc;
                decoderDesc = mDtvManager.getCommonRouteControl().getDecoderDescriptor(
                        (int) decoderLoop);

                // iterate through all outputs
                for (long outputLoop = 0; outputLoop < inputOutputNum; outputLoop++) {

                    // create output
                    RouteInputOutputDescriptor outputDesc;
                    outputDesc = mDtvManager.getCommonRouteControl().getInputOutputDescriptor(
                            (int) outputLoop);

                    // create playback route
                    mPlaybackRoutes[playbackIndex] = new PlaybackRoutes();
                    mPlaybackRoutes[playbackIndex].route = mDtvManager.getBroadcastRouteControl()
                            .getPlaybackRoute(
                                    massStorageDesc.getMassStorageId(),
                                    DEMUX_ID_NOT_USED_WITH_COMEDIA,
                                    decoderDesc.getDecoderId());

                    mPlaybackRoutes[playbackIndex].storage = massStorageDesc;
                    mPlaybackRoutes[playbackIndex].demux = new RouteDemuxDescriptor(
                            DEMUX_ID_NOT_USED_WITH_COMEDIA);
                    mPlaybackRoutes[playbackIndex].decoder = decoderDesc;
                    mPlaybackRoutes[playbackIndex].output = outputDesc;

                    mLog.d("[initializeRouteIds][Adding playback route ["
                            + mPlaybackRoutes[playbackIndex].route + "]:\n"
                            + "\toutput: " + outputDesc.getInputOutputId() + "\n"
                            + "\tmassStorage: " + massStorageDesc.getMassStorageId() + "\n"
                            + "\tdecoderId: " + decoderDesc.getDecoderId() + "\n"
                            + "\tdemuxId: " + DEMUX_ID_NOT_USED_WITH_COMEDIA);

                    playbackIndex++;
                }
            }
        }

        InstallRoutes ipInstall = null, terInstall = null, ipPipInstall = null;
        for (InstallRoutes install : mInstallRoutes) {
            if (install.frontend.getFrontendType().contains(RouteFrontendType.TER)) {
                terInstall = install;
                continue;
            }
            if (install.frontend.getFrontendType().contains(RouteFrontendType.IP)) {
                if (install.frontend.getFrontendId() == IP_FRONTEND) {
                    ipInstall = install;
                } else {
                    ipPipInstall = install;
                }
                continue;
            }
        }

        LiveRoutes ipPrimaryLive = null, terLive = null, ipPipLive = null, ipsecondaryLive = null;
        for (LiveRoutes live : mLiveRoutes) {
            if (terLive == null
                    && live.frontend.getFrontendType().contains(RouteFrontendType.TER)) {
                terLive = live;
                continue;
            }

            if (live.frontend.getFrontendType().contains(RouteFrontendType.IP)) {
                if (ipPrimaryLive == null) {
                    ipPrimaryLive = live;
                    continue;
                } else {
                    if (ipPrimaryLive.frontend.getFrontendId() != live.frontend.getFrontendId()
                            && ipPrimaryLive.decoder.getDecoderId() != live.decoder.getDecoderId()) {
                        if (ipPipLive == null) {
                            ipPipLive = live;
                            continue;
                        } else if (ipsecondaryLive == null) {
                            ipsecondaryLive = live;
                            continue;
                        }
                    }
                }
            }
        }

        RecordRoutes ipPrimaryRecord = null, terRecord = null, ipPipRecord = null, ipsecondaryRecord = null;
        for (RecordRoutes record : mRecordRoutes) {
            if (terRecord == null
                    && record.frontend.getFrontendType().contains(RouteFrontendType.TER)) {
                terRecord = record;
                continue;
            }

            if (record.frontend.getFrontendType().contains(RouteFrontendType.IP)) {
                if (ipPrimaryRecord == null) {
                    ipPrimaryRecord = record;
                    continue;
                } else {
                    if (ipPrimaryRecord.frontend.getFrontendId() != record.frontend.getFrontendId()) {
                        if (ipPipRecord == null) {
                            ipPipRecord = record;
                            continue;
                        } else if (ipsecondaryRecord == null) {
                            ipsecondaryRecord = record;
                            continue;
                        }
                    }
                }
            }
        }

        PlaybackRoutes mainPlayback = null, pipPlayback = null;
        for (PlaybackRoutes playback : mPlaybackRoutes) {
            if (mainPlayback == null) {
                mainPlayback = playback;
                continue;
            } else {
                if (pipPlayback == null && mainPlayback.decoder != playback.decoder) {
                    pipPlayback = playback;
                }
            }
        }

        // Merge live and scan routes
        if (ipPrimaryLive == null || ipInstall == null || ipPrimaryRecord == null) {
            mLog.e("[initializeRouteIds][IP primary live, scan or record routes are not found!]");
        } else {
            mLog.d("[initializeRouteIds][IP primary live, scan and recort routes are found.]["
                    + ipPrimaryLive.route + "][" + ipInstall.route
                    + "][" + ipPrimaryRecord.route + "]");
        }
        mIpPrimaryRoutes = new Routes(ipPrimaryLive, ipInstall, ipPrimaryRecord);

        if (ipsecondaryLive == null || ipsecondaryRecord == null) {
            mLog.e("[initializeRouteIds][IP secondary live or record routes are not found!]");
        } else {
            mLog.d("[initializeRouteIds][IP secondary live and recort routes are found.]["
                    + ipsecondaryLive.route + "]["
                    + ipsecondaryRecord.route + "]");
        }
        mIpSecondaryRoutes = new Routes(ipsecondaryLive, null, ipsecondaryRecord);

        if (ipPipLive == null || ipPipInstall == null || ipPipRecord == null) {
            mLog.e("[initializeRouteIds][IP PIP live, scan or record routes are not found!]");
            mIpPipRoutes = new Routes(ipPipLive, ipPipInstall, ipPipRecord);
        } else {
            mLog.d("[initializeRouteIds][IP PIP live, scan and record routes found.]["
                    + ipPipLive.route + "][" + ipPipInstall.route
                    + "][" + ipPipRecord.route + "]");
            mIpPipRoutes = new Routes(ipPipLive, ipPipInstall, ipPipRecord);
            RouteLiveSettings settings = new RouteLiveSettings();
            // TODO May break things
            EnumSet<RouteComponentType> esVideo = EnumSet.noneOf(RouteComponentType.class);
            esVideo.add(RouteComponentType.VIDEO);
            settings.setComponentSettings(esVideo);
            // TODO Get current video position?
            settings.setVideoPosition(new VideoPosition());
            mDtvManager.getBroadcastRouteControl().configureLiveRoute((int) ipPipLive.route,
                    settings);
        }

        if (terLive == null || terInstall == null || terRecord == null) {
            mLog.e("[initializeRouteIds][TER live(" + terLive + "), scan (" + terInstall
                    + ") or record (" + terRecord + ") routes are not found!]");
        } else {
            mLog.d("[initializeRouteIds][TER live, scan and record routes are found.]["
                    + terLive.route + "][" + terInstall.route + "]["
                    + terRecord.route + "]");
        }
        mTerLiveRoutes = new Routes(terLive, terInstall, terRecord);

        // Merge playback routes
        if (mainPlayback == null) {
            mLog.e("[initializeRouteIds][Playback main routes not found!]");
        } else {
            mLog.d("[initializeRouteIds][Playback main routes found.]["
                    + mainPlayback.route + "]");
            mPlaybackMainRoute = mainPlayback;
        }

        if (pipPlayback == null) {
            mLog.e("[initializeRouteIds][Playback PIP routes not found!]");
        } else {
            mLog.d("[initializeRouteIds][Playback PIP routes found.]["
                    + pipPlayback.route + "]");
            mPlaybackPipRoute = pipPlayback;
        }

        return true;
    }

    /**
     * Return route by service type.
     *
     * @param sourceType Service type to check.
     * @return Desired route, or 0 if service type is undefined.
     */
    public Routes getRouteByServiceType(SourceType sourceType) {
        switch (sourceType) {
            case TER:
                return getTerRoute();
            case IP:
                return getIpPrimaryRoute();
            case ANALOG:
            case PVR:
            case SAT:
            case CAB:
            case UNDEFINED:
            default:
                return null;
        }
    }


    public Routes getIpPrimaryRoute() {
        return mIpPrimaryRoutes;
    }

    public Routes getIpSecondaryRoute() {
        return mIpSecondaryRoutes;
    }

    public Routes getTerRoute() {
        return mTerLiveRoutes;
    }

    public Routes getIpPipRoute() {
        return mIpPipRoutes;
    }

    public PlaybackRoutes getPlaybackMainRoute() {
        return mPlaybackMainRoute;
    }

    public PlaybackRoutes getPlaybackPipRoute() {
        return mPlaybackPipRoute;
    }
}

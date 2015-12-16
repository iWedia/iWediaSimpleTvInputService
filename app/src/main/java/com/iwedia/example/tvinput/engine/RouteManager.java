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

import com.iwedia.dtv.dtvmanager.IDTVManager;
import com.iwedia.dtv.route.broadcast.IBroadcastRouteControl;
import com.iwedia.dtv.route.broadcast.RouteDemuxDescriptor;
import com.iwedia.dtv.route.broadcast.RouteFrontendDescriptor;
import com.iwedia.dtv.route.broadcast.RouteFrontendType;
import com.iwedia.dtv.route.common.ICommonRouteControl;
import com.iwedia.dtv.route.common.RouteDecoderDescriptor;
import com.iwedia.dtv.route.common.RouteInputOutputDescriptor;
import com.iwedia.dtv.service.SourceType;
import com.iwedia.example.tvinput.TvService;
import com.iwedia.example.tvinput.utils.Logger;

import java.util.EnumSet;

/**
 * A simple class that is used for Route handling
 */
public class RouteManager {

    /**
     * Object used to write to logcat output
     */
    private final Logger mLog = new Logger(TvService.APP_NAME + RouteManager.class.getSimpleName(),
            Logger.ERROR);
    public static final int EC_INVALID_ROUTE = -1;
    /** Middleware routes */
    private int mCurrentLiveRoute = EC_INVALID_ROUTE;
    private int mLiveRouteSat = EC_INVALID_ROUTE;
    private int mLiveRouteTer = EC_INVALID_ROUTE;
    private int mLiveRouteCab = EC_INVALID_ROUTE;
    private int mLiveRouteIp = EC_INVALID_ROUTE;
    private int mCurrentInstallRoute = EC_INVALID_ROUTE;
    private int mInstallRouteTer = EC_INVALID_ROUTE;
    private int mInstallRouteIp = EC_INVALID_ROUTE;
    private int mInstallRouteCab = EC_INVALID_ROUTE;
    private int mInstallRouteSat = EC_INVALID_ROUTE;
    /** Flag for determining board tuner configuration */
    private boolean isHybridTuner = false;
    /** DTV manager instance */
    private IDTVManager mDtvManager;

    /**
     * Constructor
     */
    public RouteManager() {
        mDtvManager = DtvManager.getInstance().getDtvManager();
        initialize();
    }

    /**
     * Initialize MW Routes
     */
    private void initialize() {
        IBroadcastRouteControl broadcastRouteControl = null;
        broadcastRouteControl = mDtvManager.getBroadcastRouteControl();
        ICommonRouteControl commonRouteControl = null;
        commonRouteControl = mDtvManager.getCommonRouteControl();
        // Retrieve demux descriptor.
        RouteDemuxDescriptor demuxDescriptor = null;
        demuxDescriptor = broadcastRouteControl.getDemuxDescriptor(0);
        // Retrieve decoder descriptor.
        RouteDecoderDescriptor decoderDescriptor = null;
        decoderDescriptor = commonRouteControl.getDecoderDescriptor(0);
        // Retrieve output descriptor.
        RouteInputOutputDescriptor outputDescriptor = null;
        outputDescriptor = commonRouteControl.getInputOutputDescriptor(0);
        // get number if frontends
        int numberOfFrontends = 0;
        numberOfFrontends = broadcastRouteControl.getFrontendNumber();
        // Find DVB and IP front-end descriptors.
        EnumSet<RouteFrontendType> frontendTypes = null;
        for (int i = 0; i < numberOfFrontends; i++) {
            RouteFrontendDescriptor frontendDescriptor = null;
            frontendDescriptor = broadcastRouteControl.getFrontendDescriptor(i);
            frontendTypes = frontendDescriptor.getFrontendType();
            for (RouteFrontendType frontendType : frontendTypes) {
                switch (frontendType) {
                    case SAT: {
                        if (mLiveRouteSat == EC_INVALID_ROUTE) {
                            mLiveRouteSat = getLiveRouteId(frontendDescriptor, demuxDescriptor,
                                    decoderDescriptor, outputDescriptor, broadcastRouteControl);
                            mLog.v("mLiveRouteSat: " + mLiveRouteSat);
                        }
                        if (mInstallRouteSat == -1) {
                            mInstallRouteSat = getInstallRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor, outputDescriptor,
                                    broadcastRouteControl);
                            mCurrentInstallRoute = mInstallRouteSat;
                            mLog.v("mInstallRouteSat: " + mInstallRouteSat);
                        }
                        break;
                    }
                    case CAB: {
                        if (mLiveRouteCab == EC_INVALID_ROUTE) {
                            mLiveRouteCab = getLiveRouteId(frontendDescriptor, demuxDescriptor,
                                    decoderDescriptor, outputDescriptor, broadcastRouteControl);
                            mLog.v("mLiveRouteCab: " + mLiveRouteCab);
                        }
                        if (mInstallRouteCab == -1) {
                            mInstallRouteCab = getInstallRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor, outputDescriptor,
                                    broadcastRouteControl);
                            mCurrentInstallRoute = mInstallRouteCab;
                            mLog.v("mInstallRouteCab: " + mInstallRouteCab);
                        }
                        break;
                    }
                    case TER: {
                        if (mLiveRouteTer == EC_INVALID_ROUTE) {
                            mLiveRouteTer = getLiveRouteId(frontendDescriptor, demuxDescriptor,
                                    decoderDescriptor, outputDescriptor, broadcastRouteControl);
                            mLog.v("mLiveRouteTer: " + mLiveRouteTer);
                        }
                        if (mInstallRouteTer == -1) {
                            mInstallRouteTer = getInstallRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor, outputDescriptor,
                                    broadcastRouteControl);
                            mCurrentInstallRoute = mInstallRouteTer;
                            mLog.v("mInstallRouteTer: " + mInstallRouteTer);
                        }
                        break;
                    }
                    case IP: {
                        if (mLiveRouteIp == EC_INVALID_ROUTE) {
                            mLiveRouteIp = getLiveRouteId(frontendDescriptor, demuxDescriptor,
                                    decoderDescriptor, outputDescriptor, broadcastRouteControl);
                            mLog.v("mLiveRouteIp: " + mLiveRouteIp);
                        }
                        if (mInstallRouteIp == -1) {
                            mInstallRouteIp = getInstallRouteId(frontendDescriptor,
                                    demuxDescriptor, decoderDescriptor, outputDescriptor,
                                    broadcastRouteControl);
                            mCurrentInstallRoute = mInstallRouteIp;
                            mLog.v("mInstallRouteIp: " + mInstallRouteIp);
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        if (mLiveRouteIp != EC_INVALID_ROUTE) {
            if ((mLiveRouteCab != EC_INVALID_ROUTE || mLiveRouteSat != EC_INVALID_ROUTE
                    || mLiveRouteTer != EC_INVALID_ROUTE)) {
                isHybridTuner = true;
            }
        }
    }

    public int getCurrentInstallRoute() {
        return mCurrentInstallRoute;
    }

    /**
     * Gets the current live route
     *
     * @return Current live route
     */
    public int getCurrentLiveRoute() {
        return mCurrentLiveRoute;
    }

    /**
     * Gets live route for IP tuner
     *
     * @return IP live route
     */
    public int getLiveRouteIp() {
        return mLiveRouteIp;
    }

    /**
     * Gets live route for CAB tuner
     *
     * @return CAB live route
     */
    public int getLiveRouteCab() {
        return mLiveRouteCab;
    }

    /**
     * Gets live route for TER tuner
     *
     * @return TER live route
     */
    public int getLiveRouteTer() {
        return mLiveRouteTer;
    }

    /**
     * Gets live route for SAT tuner
     *
     * @return SAT live route
     */
    public int getLiveRouteSat() {
        return mLiveRouteSat;
    }

    /**
     * Gets install route for IP tuner
     *
     * @return IP live route
     */
    public int getInstallRouteIp() {
        return mInstallRouteIp;
    }

    /**
     * Gets install route for CAB tuner
     *
     * @return CAB install route
     */
    public int getInstallRouteCab() {
        return mInstallRouteCab;
    }

    /**
     * Gets install route for TER tuner
     *
     * @return TER install route
     */
    public int getInstallRouteTer() {
        return mInstallRouteTer;
    }

    /**
     * Gets install route for SAT tuner
     *
     * @return SAT install route
     */
    public int getInstallRouteSat() {
        return mInstallRouteSat;
    }

    /**
     * Updates current live route
     *
     * @param route new live route
     */
    public void updateCurrentLiveRoute(int route) {
        mCurrentLiveRoute = route;
    }

    /**
     * Updates current live route
     *
     * @param route new live route
     */
    public void updateCurrentInstallRoute(int route) {
        mCurrentInstallRoute = route;
    }

    /**
     * Return route by service type.
     *
     * @param sourceType Service type to check.
     * @return Desired route, or 0 if service type is undefined.
     */
    public int getActiveRouteByServiceType(SourceType sourceType) {
        switch (sourceType) {
            case CAB:
                return mLiveRouteCab;
            case TER:
                return mLiveRouteTer;
            case SAT:
                return mLiveRouteSat;
            case IP:
                return mLiveRouteIp;
            case ANALOG:
            case PVR:
            case UNDEFINED:
            default:
                return EC_INVALID_ROUTE;
        }
    }

    /**
     * Is IP and some other Tuner
     *
     * @return True if tuner type is IP and some other, false otherwise.
     */
    public boolean isHybridTuner() {
        return isHybridTuner;
    }

    public String getInstallRouteDescription(int route) {
        if (route == EC_INVALID_ROUTE) {
            return "unknown route " + route;
        }
        if (route == mInstallRouteCab) {
            return "cable " + route;
        } else if (route == mInstallRouteTer) {
            return "terrestrial " + route;
        } else if (route == mInstallRouteSat) {
            return "satellite " + route;
        } else if (route == mInstallRouteIp) {
            return "ip " + route;
        } else {
            return "unknown route " + route;
        }
    }

    public String getLiveRouteDescription(int route) {
        if (route == EC_INVALID_ROUTE) {
            return "unknown route " + route;
        }
        if (route == mLiveRouteCab) {
            return "cable " + route;
        } else if (route == mLiveRouteTer) {
            return "terrestrial " + route;
        } else if (route == mLiveRouteSat) {
            return "satellite " + route;
        } else if (route == mLiveRouteIp) {
            return "ip " + route;
        } else {
            return "unknown route " + route;
        }
    }

    private int getLiveRouteId(RouteFrontendDescriptor fDescriptor,
            RouteDemuxDescriptor mDemuxDescriptor, RouteDecoderDescriptor mDecoderDescriptor,
            RouteInputOutputDescriptor mOutputDescriptor, IBroadcastRouteControl routeControl) {
        return routeControl.getLiveRoute(fDescriptor.getFrontendId(),
                mDemuxDescriptor.getDemuxId(), mDecoderDescriptor.getDecoderId());
    }

    private int getInstallRouteId(RouteFrontendDescriptor fDescriptor,
            RouteDemuxDescriptor mDemuxDescriptor, RouteDecoderDescriptor mDecoderDescriptor,
            RouteInputOutputDescriptor mOutputDescriptor, IBroadcastRouteControl routeControl) {
        return routeControl.getInstallRoute(fDescriptor.getFrontendId(),
                mDemuxDescriptor.getDemuxId());
    }
}

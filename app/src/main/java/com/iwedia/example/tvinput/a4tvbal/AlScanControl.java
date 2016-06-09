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

package com.iwedia.example.tvinput.a4tvbal;

import android.os.RemoteException;

import com.bytel.dtv.pvr.IpQuality;
import com.bytel.dtv.service.ExtendedServiceInfo;
import com.bytel.dtv.service.ServicesMediumType;
import com.iwedia.dtv.dtvmanager.DTVManager;
import com.iwedia.dtv.framework.service.ITvTunerListener;
import com.iwedia.dtv.scan.IScanCallback;
import com.iwedia.dtv.scan.ScanControl;
import com.iwedia.example.tvinput.TvService;
import com.iwedia.example.tvinput.engine.RouteManager;
import com.iwedia.example.tvinput.utils.Logger;

public class AlScanControl extends ScanControl {
    private final Logger mLog = new Logger(
            TvService.APP_NAME + AlScanControl.class.getSimpleName(), Logger.ERROR);

    public static final int TUNER_EVENT_INSTALL_STATUS = 0;
    public static final int TUNER_EVENT_INSTALL_TS_INFO_FREQUENCY = 1;
    public static final int TUNER_EVENT_INSTALL_COMPLETE = 2;
    public static final int TUNER_EVENT_INSTALL_SERVICE_TV_NAME = 3;
    public static final int TUNER_EVENT_INSTALL_SERVICE_RADIO_NAME = 4;
    public static final int TUNER_EVENT_INSTALL_SERVICE_DATA_NAME = 5;
    public static final int TUNER_EVENT_INSTALL_SERVICE_TV_NUMBER = 6;
    public static final int TUNER_EVENT_INSTALL_SERVICE_RADIO_NUMBER = 7;
    public static final int TUNER_EVENT_INSTALL_SERVICE_DATA_NUMBER = 8;
    public static final int TUNER_EVENT_INSTALL_SERVICE_TV_AK = 9;
    public static final int TUNER_EVENT_INSTALL_SERVICE_RADIO_AK = 10;
    public static final int TUNER_EVENT_INSTALL_SERVICE_STATUS = 11;
    public static final int TUNER_EVENT_TRIGGER_STATUS = 12;
    public static final int TUNER_EVENT_INSTALL_PROGRESS = 13;
    public static final int TUNER_EVENT_INSTALL_SIGNAL_LEVEL = 14;
    public static final int TUNER_EVENT_INSTALL_SIGNAL_QUALITY = 15;
    public static final int TUNER_EVENT_INSTALL_SIGNAL_BER = 16;
    public static final int TUNER_EVENT_SIGNAL_STATUS = 17;
    public static final int TUNER_EVENT_SATIP_SERVER_DROPPED = 18;
    public static final int TUNER_EVENT_INSTALL_NO_MORE_SERVICE_SPACE = 19;
    public static final int TUNER_EVENT_INSTALL_SIGNAL_ALL_INFO = 20;
    public static final int TUNER_EVENT_UPDATE_DATABASE = 21;

    private IScanCallback mCallback;

    private RouteManager mRouteManager;

    public void registerCallback(IScanCallback callback) {
        mCallback = callback;

        // ! It's safe to call here, since this function is called after service
        // is created
        mRouteManager = new RouteManager();

        // Bind directly to MW callback
        try {
            DTVManager.getDvbService().registerTunerListener(mTvTunerListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private ITvTunerListener mTvTunerListener = new ITvTunerListener.Stub() {
        @Override
        public void tunerCallback(int event, int arg1, int arg2, int arg3, int arg4, String arg5) {

            switch (event) {
                case TUNER_EVENT_INSTALL_STATUS:
                    mLog.d("TUNER_EVENT_INSTALL_STATUS");
                    mCallback.installStatus(null);
                    break;
                case TUNER_EVENT_INSTALL_TS_INFO_FREQUENCY: {
                    mLog.d("TUNER_EVENT_INSTALL_TS_INFO_FREQUENCY");
                    int frequency = arg1;
                    mCallback.scanTunFrequency(mRouteManager.getCurrentInstallRoute(), frequency);
                    break;
                }
                case TUNER_EVENT_INSTALL_SERVICE_TV_NAME: {
                    mLog.d("TUNER_EVENT_INSTALL_SERVICE_TV_NAME");
                    String name = arg5;
                    mCallback.installServiceTVName(mRouteManager.getCurrentInstallRoute(), name);
                    break;
                }
                case TUNER_EVENT_INSTALL_SERVICE_RADIO_NAME: {
                    mLog.d("TUNER_EVENT_INSTALL_SERVICE_RADIO_NAME");
                    String name = arg5;
                    mCallback.installServiceRADIOName(mRouteManager.getCurrentInstallRoute(), name);
                    break;
                }
                case TUNER_EVENT_INSTALL_SERVICE_DATA_NAME: {
                    mLog.d("TUNER_EVENT_INSTALL_SERVICE_DATA_NAME");
                    String name = arg5;
                    mCallback.installServiceDATAName(mRouteManager.getCurrentInstallRoute(), name);
                    break;
                }
                case TUNER_EVENT_INSTALL_SERVICE_TV_NUMBER: {
                    mLog.d("TUNER_EVENT_INSTALL_SERVICE_TV_NUMBER");
                    int number = arg1;
                    mCallback
                            .installServiceTVNumber(mRouteManager.getCurrentInstallRoute(), number);
                    break;
                }
                case TUNER_EVENT_INSTALL_SERVICE_RADIO_NUMBER: {
                    mLog.d("TUNER_EVENT_INSTALL_SERVICE_RADIO_NUMBER");
                    int number = arg1;
                    mCallback.installServiceRADIONumber(mRouteManager.getCurrentInstallRoute(),
                            number);
                    break;
                }
                case TUNER_EVENT_INSTALL_SERVICE_DATA_NUMBER: {
                    mLog.d("TUNER_EVENT_INSTALL_SERVICE_DATA_NUMBER");
                    int number = arg1;
                    mCallback.installServiceDATANumber(mRouteManager.getCurrentInstallRoute(),
                            number);
                    break;
                }
                case TUNER_EVENT_INSTALL_SERVICE_TV_AK:
                    mLog.d("TUNER_EVENT_INSTALL_SERVICE_TV_AK");
                    break;
                case TUNER_EVENT_INSTALL_SERVICE_RADIO_AK:
                    mLog.d("TUNER_EVENT_INSTALL_SERVICE_RADIO_AK");
                    break;
                case TUNER_EVENT_INSTALL_SERVICE_STATUS: {
                    mLog.d("TUNER_EVENT_INSTALL_SERVICE_STATUS");
                    int networkId = arg1;
                    mCallback.networkChanged(networkId);
                    break;
                }
                case TUNER_EVENT_TRIGGER_STATUS:
                    mLog.d("TUNER_EVENT_TRIGGER_STATUS");
                    mCallback.scanNoServiceSpace(mRouteManager.getCurrentInstallRoute());
                    break;
                case TUNER_EVENT_INSTALL_PROGRESS:
                    mLog.d("TUNER_EVENT_INSTALL_PROGRESS");
                    int progress = arg1;
                    mCallback.scanProgressChanged(mRouteManager.getCurrentInstallRoute(), progress);
                    if (progress == 100) {
                        mCallback.scanFinished(mRouteManager.getCurrentInstallRoute());
                    }
                    break;
                case TUNER_EVENT_INSTALL_SIGNAL_LEVEL: {
                    mLog.d("TUNER_EVENT_INSTALL_SIGNAL_LEVEL");
                    int signalLevel = arg1;
                    mCallback.signalStrength(mRouteManager.getCurrentInstallRoute(), signalLevel);
                    break;
                }
                case TUNER_EVENT_INSTALL_SIGNAL_QUALITY: {
                    mLog.d("TUNER_EVENT_INSTALL_SIGNAL_QUALITY");
                    int quality = arg1;
                    mCallback.signalQuality(mRouteManager.getCurrentInstallRoute(), quality);
                    break;
                }
                case TUNER_EVENT_INSTALL_SIGNAL_BER: {
                    mLog.d("TUNER_EVENT_INSTALL_SIGNAL_BER");
                    int ber = arg1;
                    mCallback.signalBer(mRouteManager.getCurrentInstallRoute(), ber);
                    break;
                }
                case TUNER_EVENT_SATIP_SERVER_DROPPED:
                    mLog.d("TUNER_EVENT_SATIP_SERVER_DROPPED");
                    break;
                case TUNER_EVENT_INSTALL_NO_MORE_SERVICE_SPACE:
                    mLog.d("TUNER_EVENT_INSTALL_NO_MORE_SERVICE_SPACE");
                    mCallback.scanNoServiceSpace(-1);
                    break;
                case TUNER_EVENT_INSTALL_SIGNAL_ALL_INFO: {
                    break;
                }
                case TUNER_EVENT_UPDATE_DATABASE:
                    int percent = arg1;
                    mLog.d("[UPDATE_DATABASE][" + percent + "]");
                    break;
            }
        }
    };

    @Override
    public boolean deleteServices(ServicesMediumType servicesMediumType) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isScanRunning() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ExtendedServiceInfo getExtendedServiceInfo(int routeID, int serviceIndex,
            int serviceListIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean autoScanEx(int routeID) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean abortScanEx(int routeID) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ExtendedServiceInfo getExtendedServiceInfoByQuality(int routeID, int serviceIndex,
            int serviceListIndex, IpQuality quality) {
        // TODO Auto-generated method stub
        return null;
    }
}

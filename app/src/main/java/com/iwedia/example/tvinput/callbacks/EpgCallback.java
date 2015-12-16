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
package com.iwedia.example.tvinput.callbacks;

import com.iwedia.dtv.epg.IEpgCallback;
import com.iwedia.example.tvinput.TvService;
import com.iwedia.example.tvinput.engine.DtvManager;
import com.iwedia.example.tvinput.utils.Logger;

/**
 * Callback class for receiving events from middleware
 */
public class EpgCallback implements IEpgCallback {

    /** Object used to write to logcat output */
    private final Logger mLog = new Logger(TvService.APP_NAME + EpgCallback.class.getSimpleName(),
            Logger.DEBUG);
    /** Instance of DVB Manager */
    private DtvManager mDvbManager = null;

    /**
     * Constructor
     *
     * @param dvbManager Instance of DVB Manager
     */
    public EpgCallback(DtvManager dvbManager) {
        mDvbManager = dvbManager;
    }

    /**
     * Inform the user about the changes about schedule information.
     *
     * @param filterID     - Filter ID.
     * @param serviceIndex - Index of service.
     */
    @Override
    public void scEventChanged(int filterID, int serviceIndex) {
        mLog.d("[epg_callback][scEventChanged][filter ID: " + filterID + "][service index: "
                + serviceIndex + "]");
    }

    /**
     * Schedule information obtained entirely.
     *
     * @param filterID     - Filter ID.
     * @param serviceIndex - Index of service.
     */
    @Override
    public void scAcquisitionFinished(int filterID, int serviceIndex) {
        mLog.d("[epg_callback][scAcquisitionFinished][filter ID" + filterID + "][service index: "
                + serviceIndex
                + "]");
        mDvbManager.updateEpgList();
    }

    /**
     * Inform the user about the changes about present following information.
     *
     * @param filterID     - Filter ID.
     * @param serviceIndex - Index of service.
     */
    @Override
    public void pfEventChanged(int filterID, int serviceIndex) {
        mLog.d("[epg_callback][pfEventChanged][filder ID" + filterID + "][service index"
                + serviceIndex + "]");
    }

    /**
     * Present-Following information obtained entirely.
     *
     * @param filterID     - Filter ID.
     * @param serviceIndex - Index of service.
     */
    @Override
    public void pfAcquisitionFinished(int filterID, int serviceIndex) {
        mLog.d("[epg_callback][pfAcquisitionFinished][filder ID" + filterID + "][service index: "
                + serviceIndex
                + "]");
        mDvbManager.updateNowNext(filterID, serviceIndex);
    }

    @Override
    public void tdtChanged(int filterID, int serviceIndex) {
        mLog.d("[epg_callback][tdtChanged][filder ID" + filterID + "][service index: "
                + serviceIndex + "]");
        mDvbManager.getEpgManager().prepareGetEpgEvents();
    }
}

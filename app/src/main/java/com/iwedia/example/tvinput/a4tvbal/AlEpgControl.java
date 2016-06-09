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

import com.iwedia.dtv.dtvmanager.IDTVManager;
import com.iwedia.dtv.epg.EpgControl;
import com.iwedia.dtv.epg.IEpgCallback;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.dtv.types.TimeDate;
import com.iwedia.example.tvinput.TvService;
import com.iwedia.example.tvinput.utils.Logger;

public class AlEpgControl extends EpgControl {
    private final Logger mLog = new Logger(TvService.APP_NAME + AlEpgControl.class.getSimpleName(),
            Logger.ERROR);

    public int createWindow(TimeDate windowStartTime, int windowDuration) throws InternalException {
        /**
         * Beta API doesn't have this API. Normal code is commented bellow.
         */
        // EpgMasterList epgMasterList = new EpgMasterList(masterListIndexes);
        // mEpgControl.createWindow(epgMasterList, startTimeOfAcquisition,
        // durationInHours);
        return 0;
    }

    public void registerCallback(IEpgCallback callback, int filterID) {
        registerCallback(callback, filterID, null);
    }
}

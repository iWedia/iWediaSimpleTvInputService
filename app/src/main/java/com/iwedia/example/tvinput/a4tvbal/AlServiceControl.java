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

import com.bytel.dtv.pvr.IpQuality;
import com.bytel.dtv.service.BytelServiceControl;
import com.bytel.dtv.service.TvProfile;
import com.iwedia.dtv.service.IServiceCallback;
import com.iwedia.dtv.service.ServiceControl;
import com.iwedia.example.tvinput.TvService;
import com.iwedia.example.tvinput.utils.Logger;

public class AlServiceControl extends ServiceControl {
    private final Logger mLog = new Logger(TvService.APP_NAME
            + AlServiceControl.class.getSimpleName(), Logger.ERROR);

    public void registerCallback(IServiceCallback callback) {
        registerCallback(callback, null);
    }

    @Override
    public void setTvProfile(TvProfile streamProfile) {
        // TODO Auto-generated method stub

    }

    @Override
    public TvProfile getTvProfile() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setMaxSimultaneousPiP(int maxSimultaneousPiP) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getMaxSimultaneousPiP() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean startServiceEx(int routeID, int listIndex, int serviceIndex, IpQuality quality) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getActiveListIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean stopServiceEx(int routeID) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean zapURLEx(int routeID, String url) {
        // TODO Auto-generated method stub
        return false;
    }
}

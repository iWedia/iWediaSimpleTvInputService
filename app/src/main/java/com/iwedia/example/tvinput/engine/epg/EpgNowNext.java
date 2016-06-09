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

package com.iwedia.example.tvinput.engine.epg;

import android.content.Context;

import com.iwedia.dtv.epg.EpgEventType;
import com.iwedia.dtv.epg.IEpgControl;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.example.tvinput.a4tvbal.AlEpgControl;
import com.iwedia.example.tvinput.a4tvbal.AlEpgEvent;
import com.iwedia.example.tvinput.engine.utils.EpgRunnable;

/**
 * Runnable class for inserting Now/Next EPG data into program Database
 */
public class EpgNowNext extends EpgRunnable {

    /** Now/Next events origin channel index */
    private final int mChannelIndex;

    /**
     * Contructor
     * 
     * @param context Application context
     * @param channelId Channel id for Now/Next event
     */
    public EpgNowNext(Context context, int channelIndex) {
        super(context);
        mChannelIndex = channelIndex;
    }

    @Override
    public void run() {
        AlEpgEvent now = null;
        AlEpgEvent next = null;
        AlEpgControl epgControl = (AlEpgControl) mDtvManager.getEpgControl();
        try {
            // Get Present Event
            now = (AlEpgEvent) epgControl.getPresentFollowingEvent(mDtvManager.getEpgManager()
                    .getEpgFilterID(), (int) mChannelIndex, EpgEventType.PRESENT_EVENT);
            // Get Next Event
            next = (AlEpgEvent) epgControl.getPresentFollowingEvent(mDtvManager.getEpgManager()
                    .getEpgFilterID(), (int) mChannelIndex, EpgEventType.FOLLOWING_EVENT);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
        addProgram(now, mChannelIndex);
        addProgram(next, mChannelIndex);
    }
}

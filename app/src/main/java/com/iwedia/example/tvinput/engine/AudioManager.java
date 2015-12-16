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

import com.iwedia.dtv.audio.AudioTrack;
import com.iwedia.dtv.audio.IAudioControl;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.example.tvinput.engine.utils.TrackManager;

/**
 * Manager for handling Teletext, Subtitle and Audio tracks.
 */
public class AudioManager extends TrackManager<AudioTrack> {

    /** Audio control object */
    private IAudioControl mAudioControl;
    private RouteManager mRouteManager;

    /**
     * Constructor
     *
     * @param audioControl Object through which audio control is achieved
     */
    public AudioManager(IAudioControl audioControl) {
        this.mAudioControl = audioControl;
        mRouteManager = DtvManager.getInstance().getRouteManager();
    }

    /**
     * Returns number of audio tracks for current channel.
     */
    @Override
    public int getTrackCount() {
        return mAudioControl.getAudioTrackCount(mRouteManager.getCurrentLiveRoute());
    }

    /**
     * Returns audio track by index.
     */
    @Override
    public AudioTrack getTrack(int index) {
        return mAudioControl.getAudioTrack(mRouteManager.getCurrentLiveRoute(), index);
    }

    /**
     * Sets audio track with desired index as active.
     */
    public void setAudioTrack(int index) throws InternalException {
        mAudioControl.setCurrentAudioTrack(mRouteManager.getCurrentLiveRoute(), index);
    }
}

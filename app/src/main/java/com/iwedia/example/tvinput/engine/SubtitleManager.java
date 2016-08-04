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

import com.iwedia.dtv.route.broadcast.routemanager.Routes;
import com.iwedia.dtv.subtitle.ISubtitleControl;
import com.iwedia.dtv.subtitle.SubtitleMode;
import com.iwedia.dtv.subtitle.SubtitleTrack;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.example.tvinput.engine.utils.TrackManager;

/**
 * Manager for handling subtitle tracks.
 */
public class SubtitleManager extends TrackManager<SubtitleTrack> {

    /** Subtitle control object */
    private ISubtitleControl mSubtitleControl;
    /** Flags that shows teletext and subtitle status */
    private boolean mSubtitleActive = false;

    /**
     * Constructor
     *
     * @param mSubtitleControl Object through which subtitle control is achieved
     */
    public SubtitleManager(ISubtitleControl subtitleControl) {
        mSubtitleControl = subtitleControl;
    }

    /**
     * Convert subtitle track mode to human readable format.
     *
     * @param type Subtitle track mode.
     * @return Converted string.
     */
    public String convertSubtitleTrackModeToHumanReadableFormat(int modeIndex) {
        SubtitleMode mode = SubtitleMode.getFromValue(modeIndex);
        switch (mode) {
            case HEARING_IMPAIRED:
                return "HOH";
            case TRANSLATION:
            default:
                return "NORMAL";
        }
    }

    /**
     * Show subtitles on screen.
     *
     * @param trackIndex Subtitle track to show.
     * @return True if subtitle is started, false otherwise.
     * @throws InternalException
     */
    public boolean showSubtitles(int routeId, int trackIndex) throws InternalException {
        mSubtitleControl.setCurrentSubtitleTrack(routeId, trackIndex);
        if (mSubtitleControl
                .getCurrentSubtitleTrackIndex(routeId) >= 0) {
            mSubtitleActive = true;
        }
        return mSubtitleActive;
    }

    /**
     * Hide started subtitle.
     *
     * @throws InternalException
     */
    public void hideSubtitles(int routeId) throws InternalException {
        mSubtitleControl.deselectCurrentSubtitleTrack(routeId);
        if (mSubtitleControl
                .getCurrentSubtitleTrackIndex(routeId) < 0) {
            mSubtitleActive = false;
        }
    }

    /**
     * Returns subtitle track by index.
     */
    @Override
    public SubtitleTrack getTrack(int routeId, int index) {
        return mSubtitleControl.getSubtitleTrack(routeId, index);
    }

    /**
     * Get subtitle track count.
     *
     * @return Number of subtitle tracks.
     */
    @Override
    public int getTrackCount(int routeId) {
        return mSubtitleControl.getSubtitleTrackCount(routeId);
    }

    /**
     * Returns TRUE if subtitle is active, FALSE otherwise.
     */
    public boolean isSubtitleActive(int routeId) {
        mSubtitleActive = mSubtitleControl.getCurrentSubtitleTrackIndex(routeId) >= 0;
        return mSubtitleActive;
    }
}

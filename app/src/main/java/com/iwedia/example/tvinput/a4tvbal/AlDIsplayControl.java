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

/**
 * This one is using to abstract Beta.
 */

import com.iwedia.dtv.display.DisplayControl;
import com.iwedia.dtv.display.SurfaceBundle;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.example.tvinput.TvService;
import com.iwedia.example.tvinput.utils.Logger;

public class AlDIsplayControl extends DisplayControl {

    private final Logger mLog = new Logger(TvService.APP_NAME
            + AlDIsplayControl.class.getSimpleName(), Logger.ERROR);

    public void setVideoLayerSurface(int layer, SurfaceBundle surfaceBundle)
            throws InternalException {
        setVideoLayerSurface(layer, surfaceBundle);
    }

    // ! API from master have route argument
    public void scaleWindow(int route, int x, int y, int width, int height)
            throws InternalException {
        // Beta API
        // mDisplayControl.scaleWindow(x, y, width, height);
        mLog.w("Scale window in Beta is not supported");
    }
}

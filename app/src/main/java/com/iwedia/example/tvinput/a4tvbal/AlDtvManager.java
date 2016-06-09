
package com.iwedia.example.tvinput.a4tvbal;

import com.iwedia.debug.Logger;
import com.iwedia.dtv.audio.IAudioControl;
import com.iwedia.dtv.display.IDisplayControl;
import com.iwedia.dtv.dtvmanager.DTVManager;
import com.iwedia.dtv.epg.IEpgControl;
import com.iwedia.dtv.pvr.IPvrControl;
import com.iwedia.dtv.route.broadcast.IBroadcastRouteControl;
import com.iwedia.dtv.scan.IScanControl;
import com.iwedia.dtv.service.IServiceControl;
import com.iwedia.dtv.setup.ISetupControl;
import com.iwedia.dtv.subtitle.ISubtitleControl;
import com.iwedia.dtv.swupdate.ISoftwareUpdateControl;
import com.iwedia.dtv.teletext.ITeletextControl;
import com.iwedia.dtv.video.IVideoControl;

public class AlDtvManager extends DTVManager {
    private static Logger mLog = Logger.create(AlDtvManager.class.getSimpleName());

    private IServiceControl mServiceControl;
    private IAudioControl mAudioControl;
    private ISubtitleControl mSubtitleControl;
    private IPvrControl mPvrControl;
    private ISetupControl mSetupControl;
    private IScanControl mScanControl;
    private IBroadcastRouteControl mBroadcastRouteControl;
    private IEpgControl mEpgControl;
    private IDisplayControl mDisplayControl;

    public AlDtvManager() {
        super();
        mServiceControl = new AlServiceControl();
        mAudioControl = new AlAudioControl();
        mSubtitleControl = new AlSubtitleControl();
        mPvrControl = new AlPvrControl();
        mSetupControl = new AlSetupControl();
        mScanControl = new AlScanControl();
        mBroadcastRouteControl = new AlBroadcastRouteControl();
        mEpgControl = new AlEpgControl();
        mDisplayControl = new AlDIsplayControl();
    }

    @Override
    public IServiceControl getServiceControl() {
        return mServiceControl;
    }

    @Override
    public IAudioControl getAudioControl() {
        return mAudioControl;
    }

    @Override
    public IVideoControl getVideoControl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ISubtitleControl getSubtitleControl() {
        return mSubtitleControl;
    }

    @Override
    public IPvrControl getPvrControl() {
        return mPvrControl;
    }

    @Override
    public ISetupControl getSetupControl() {
        return mSetupControl;
    }

    @Override
    public ITeletextControl getTeletextControl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IScanControl getScanControl() {
        return mScanControl;
    }

    @Override
    public ISoftwareUpdateControl getSoftwareUpdateControl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IBroadcastRouteControl getBroadcastRouteControl() {
        return mBroadcastRouteControl;
    }

    @Override
    public IEpgControl getEpgControl() {
        return mEpgControl;
    }

    @Override
    public IDisplayControl getDisplayControl() {
        return mDisplayControl;
    }

}

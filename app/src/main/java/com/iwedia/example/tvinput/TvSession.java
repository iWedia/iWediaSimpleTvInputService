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
package com.iwedia.example.tvinput;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.PixelFormat;
import android.media.tv.TvContentRating;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputService;
import android.media.tv.TvTrackInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.CaptioningManager;
import android.widget.ImageView;

import com.iwedia.dtv.audio.AudioTrack;
import com.iwedia.dtv.display.SurfaceBundle;
import com.iwedia.dtv.service.ServiceType;
import com.iwedia.dtv.subtitle.SubtitleTrack;
import com.iwedia.dtv.types.InternalException;
import com.iwedia.example.tvinput.data.ChannelDescriptor;
import com.iwedia.example.tvinput.data.RatingInfo;
import com.iwedia.example.tvinput.engine.AudioManager;
import com.iwedia.example.tvinput.engine.ChannelManager;
import com.iwedia.example.tvinput.engine.DtvManager;
import com.iwedia.example.tvinput.engine.SubtitleManager;
import com.iwedia.example.tvinput.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;

class TvSession extends TvInputService.Session {

    /** Object used to write to logcat output */
    private final Logger mLog = new Logger(TvService.APP_NAME
            + TvSession.class.getSimpleName(), Logger.ERROR);
    /** Uri of the currently active channel */
    private ChannelDescriptor mCurrentChannel = null;
    /** Stores tracks acquired from Comedia MW */
    private ArrayList<TvTrackInfo> mTracks = new ArrayList<TvTrackInfo>();
    /** Stores real Comedia MW tracks indexes */
    private HashMap<String, Integer> mTracksIndices = new HashMap<String, Integer>();
    /** Flag that is used to determine weather subtitles are enabled */
    private boolean mIsSubtitleEnabled;
    /** DvbManager for accessing MW API */
    private DtvManager mDtvManager;
    /** Subtitle track manager */
    private SubtitleManager mSubtitleManager;
    /** Audio track manager */
    private AudioManager mAudioManager;
    /** Application context */
    private Context mContext;
    /** Listener for session events */
    private ITvSession mSessionListener;
    /** Channel manager object */
    private ChannelManager mChannelManager;
    /** Input ID for TV session */
    private String mInputID;
    /** Android TIF manager */
    private TvInputManager mTvManager;
    /** Video playback surface returned by TIF */
    private Surface mVideoSurface = null;
    /** Overview layout omposition */
    private ViewGroup mOverlayView = null;
    /** SurfaceView for rendering subtitles, ovned by overlay view */
    private SurfaceView mSubtitleSurfaceView = null;
    private ImageView mImageViewRadio;
    /** Is current content block by parental control */
    private boolean mContentIsBlocked = false;
    private static final int MSG_UPDATE_RATING = 1;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_RATING:
                    checkContentRating();
                    break;
            }
        }
    };

    /**
     * Interface used for session event reporting
     */
    public interface ITvSession {

        public void onSessionRelease(TvSession session);
    }

    /**
     * Constructor
     *
     * @param sessionListener Listener through which reporting when session onRelease() is called.
     */
    public TvSession(Context context, ITvSession sessionListener, String inputID) {
        super(context);
        mLog.d("[TvSession][Started!]");
        mTvManager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
        mInputID = inputID;
        mSessionListener = sessionListener;
        mContext = context;
        mIsSubtitleEnabled = ((CaptioningManager) mContext
                .getSystemService(Context.CAPTIONING_SERVICE)).isEnabled();
        mDtvManager = DtvManager.getInstance();
        mChannelManager = mDtvManager.getChannelManager();
        mSubtitleManager = mDtvManager.getSubtitleManager();
        mAudioManager = mDtvManager.getAudioManager();
    }

    @Override
    public void onRelease() {
        mLog.d("[onRelease]");
        resetTracks();
        stopPlayback();
        mCurrentChannel = null;
        mContentIsBlocked = false;
        mSessionListener.onSessionRelease(this);
    }

    @Override
    public boolean onSetSurface(Surface surface) {
        mLog.d("[onSetSurface][" + surface + "]");
        mVideoSurface = surface;
        return true;
    }

    @Override
    public View onCreateOverlayView() {
        mLog.d("[onCreateOverlayView]");
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mOverlayView = (ViewGroup) inflater.inflate(R.layout.overlay_view, null);
        mImageViewRadio = (ImageView) mOverlayView.findViewById(R.id.imageViewRadio);
        mSubtitleSurfaceView = (SurfaceView) mOverlayView.findViewById(R.id.subtitleSurfaceView);
        mSubtitleSurfaceView.setVisibility(View.VISIBLE);
        mSubtitleSurfaceView.setZOrderMediaOverlay(true);
        mSubtitleSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        mSubtitleSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mLog.d("[onCreateOverlayView][surfaceDestroyed]");
                try {
                    mDtvManager.getDtvManager().getDisplayControl().setVideoLayerSurface(0, null);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InternalException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mLog.d("[onCreateOverlayView][surfaceCreated]");
                SurfaceBundle bundle = new SurfaceBundle(holder.getSurface());
                try {
                    mDtvManager.getDtvManager().getDisplayControl().setVideoLayerSurface(0, bundle);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InternalException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mLog.d("[onCreateOverlayView][surfaceChanged] " + width + "x" + height + "x"
                        + format);
            }
        });
        mLog.d("[onCreateOverlayView] view=" + mOverlayView);
        return mOverlayView;
    }

    @Override
    public void onSetStreamVolume(float volume) {
        // FIXME: round to 1 decimal, higher precision is not needed
        mLog.d("[onSetStreamVolume][volume: " + volume + "]");
        if (volume == 0.0f) {
            try {
                mDtvManager.setMute();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            mDtvManager.setVolume(volume * 100);
        }
    }

    @Override
    public boolean onTune(Uri channelUri) {
        mLog.d("[onTune][uri: " + channelUri + "]");
        // reset audio and subtitle tracks
        resetTracks();
        long id = ContentUris.parseId(channelUri);
        mCurrentChannel = mChannelManager.getChannelById(id);
        if (mCurrentChannel == null) {
            mLog.d("[onTune][channel not fount][uri: " + channelUri + "]");
            mContentIsBlocked = false;
            return false;
        }
        // check parental and start playback
        checkContentRating();
        return true;
    }

    @Override
    public void onSetCaptionEnabled(boolean enabled) {
        mLog.d("[onSetCaptionEnabled][enabled: " + enabled + "]");
        mIsSubtitleEnabled = enabled;
    }

    @Override
    public boolean onSelectTrack(int type, String trackId) {
        mLog.d("[onSelectTrack][type: " + type + "][track Id:" + trackId + "]");
        switch (type) {
            case TvTrackInfo.TYPE_SUBTITLE:
                if (!mIsSubtitleEnabled) {
                    return false;
                }
                try {
                    if (trackId == null) {
                        mSubtitleManager.hideSubtitles();
                    } else {
                        mSubtitleManager.showSubtitles(mTracksIndices.get(trackId));
                    }
                } catch (InternalException e) {
                    e.printStackTrace();
                }
                notifyTrackSelected(type, trackId);
                return true;
            case TvTrackInfo.TYPE_AUDIO:
                try {
                    mAudioManager.setAudioTrack(mTracksIndices.get(trackId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                notifyTrackSelected(type, trackId);
                return true;
            case TvTrackInfo.TYPE_VIDEO:
                // This feature is not supported by Comedia MW
        }
        return false;
    }

    @Override
    public void onUnblockContent(TvContentRating rating) {
        mLog.d("[onUnblockContent][rating: " + rating + "]");
        if (mCurrentChannel != null && mContentIsBlocked) {
            mContentIsBlocked = false;
            startPlayback();
        }
    }

    public String getInputID() {
        return mInputID;
    }

    /**
     * Check parental control content rating for currently selected channel and start playback
     * if channel is not blocked or stop playback otherwise.
     */
    void checkContentRating() {
        final RatingInfo info = RatingInfo.buildRatingInfo(mContext, mCurrentChannel);
        mContentIsBlocked = info != null && info.rating != null
                && mTvManager.isParentalControlsEnabled()
                && mTvManager.isRatingBlocked(info.rating);
        if (mContentIsBlocked) {
            notifyContentBlocked(info.rating);
            stopPlayback();
        } else {
            notifyContentAllowed();
            startPlayback();
        }
        if (info != null) {
            mLog.d("Next rating update: " + info.expires);
            // Schedule next parental control check when current event expires
            mHandler.removeMessages(MSG_UPDATE_RATING);
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_RATING,
                    info.expires - System.currentTimeMillis());
        }
    }

    /**
     * Update audio and subtitle tracks information for currently selected channel.
     */
    void updateTracks() {
        String firstAudioTrack = null;
        mTracks.clear();
        mTracksIndices.clear();
        if (mCurrentChannel != null) {
            // Audio tracks
            int audioTrackCount = mAudioManager.getTrackCount();
            for (int trackIndex = 0; trackIndex < audioTrackCount; trackIndex++) {
                AudioTrack audioTrack = mAudioManager.getTrack(trackIndex);
                String trackId = mTracks.size()
                        + "_" + audioTrack.getName()
                        + "_" + audioTrack.getLanguage();
                mTracks.add(new TvTrackInfo.Builder(TvTrackInfo.TYPE_AUDIO, trackId)
                        .setLanguage(audioTrack.getLanguage())
                        .build());
                mTracksIndices.put(trackId, audioTrack.getIndex());
                if (firstAudioTrack == null) {
                    firstAudioTrack = trackId;
                }
                trackIndex++;
            }
            // Subtitle tracks
            int subtitlesTrackCount = mSubtitleManager.getTrackCount();
            for (int trackIndex = 0; trackIndex < subtitlesTrackCount; trackIndex++) {
                SubtitleTrack subtitleTrack = mSubtitleManager.getTrack(trackIndex);
                String trackId = mTracks.size()
                        + "_" + subtitleTrack.getName()
                        + "_" + subtitleTrack.getLanguage();
                mTracks.add(new TvTrackInfo.Builder(TvTrackInfo.TYPE_SUBTITLE, trackId)
                        .setLanguage(subtitleTrack.getLanguage())
                        .build());
                mTracksIndices.put(trackId, subtitleTrack.getIndex());
                trackIndex++;
            }
        }
        // Notify tracks update
        notifyTracksChanged(mTracks);
        if (firstAudioTrack != null) {
            notifyTrackSelected(TvTrackInfo.TYPE_AUDIO, firstAudioTrack);
        }
        notifyTrackSelected(TvTrackInfo.TYPE_SUBTITLE, null);
    }

    private void startPlayback() {
        if (mCurrentChannel != null) {
            notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING);
            try {
                mDtvManager.start(mCurrentChannel);
            } catch (InternalException e) {
                e.printStackTrace();
            }
            mLog.d("[startPlayback] mImageViewRadio SHOW: " + (mCurrentChannel.getServiceType() ==
                    ServiceType.DIG_RAD));
            if (mImageViewRadio != null) {
                mImageViewRadio.setVisibility(mCurrentChannel.getServiceType() == ServiceType
                        .DIG_RAD ? View.VISIBLE : View.GONE);
            }
            notifyVideoAvailable();
        }
    }

    private void stopPlayback() {
        try {
            mDtvManager.stop();
        } catch (InternalException e) {
            e.printStackTrace();
        }
    }

    private void resetTracks() {
        mTracks.clear();
        mTracksIndices.clear();
        notifyTracksChanged(mTracks);
        notifyTrackSelected(TvTrackInfo.TYPE_AUDIO, null);
        notifyTrackSelected(TvTrackInfo.TYPE_VIDEO, null);
        notifyTrackSelected(TvTrackInfo.TYPE_SUBTITLE, null);
    }
}

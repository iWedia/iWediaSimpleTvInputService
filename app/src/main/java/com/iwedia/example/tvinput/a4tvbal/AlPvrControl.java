package com.iwedia.example.tvinput.a4tvbal;

import com.bytel.dtv.pvr.IpQuality;
import com.bytel.dtv.pvr.PvrErrorCode;
import com.bytel.dtv.pvr.PvrThresholds;
import com.iwedia.dtv.pvr.PvrControl;
import com.iwedia.dtv.pvr.TimerCreateParams;
import com.iwedia.dtv.types.InternalException;

public class AlPvrControl extends PvrControl{

    @Override
    public PvrErrorCode createOnTouchRecordEx(int routeID, int serviceID, IpQuality quality,
            int recordID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PvrErrorCode createTimerRecordEx(int routeID, TimerCreateParams startDate,
            IpQuality quality, int recordID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PvrThresholds getThresholds() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PvrErrorCode updateRecord(int handle, TimerCreateParams timeParams, IpQuality quality,
            int recordID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRecordHandle(int recordIndex) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean destroyRecordEx(int handle) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean startPlaybackEx(int routeID, int index) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean stopPlaybackEx(int routeID) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean controlSpeedEx(int routeID, int speed) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean jumpEx(int routeID, int position, boolean relative) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deleteMediaEx(int index) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean startTimeshiftEx(int routeID) throws InternalException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean stopTimeshiftEx(int routeID, boolean resume) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean startPlaybackFrom(int routeID, int index, int positionMs) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getNextScheduledEventTime() {
        // TODO Auto-generated method stub
        return 0;
    }

}

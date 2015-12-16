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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.iwedia.example.tvinput.utils.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Receiver that is used to auto-start this service when Android device boots up
 */
public class BootUpReceiver extends BroadcastReceiver {

    /** Object used to write to logcat output */
    private final Logger mLog = new Logger(TvService.APP_NAME
            + BootUpReceiver.class.getSimpleName(), Logger.DEBUG);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            boolean isTvPlayerRunning = false;
            while (!isTvPlayerRunning) {
                try {
                    Process p = Runtime.getRuntime().exec("top");
                    String line;
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            p.getInputStream()));
                    while ((line = in.readLine()) != null) {
                        if (line.contains("tvplayer_service")) {
                            isTvPlayerRunning = true;
                            break;
                        }
                    }
                    mLog.d("[onReceive][isTvPlayerRunning: " + isTvPlayerRunning + "]");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // Start service
            context.startService(new Intent(context, TvService.class));
        }
    }
}

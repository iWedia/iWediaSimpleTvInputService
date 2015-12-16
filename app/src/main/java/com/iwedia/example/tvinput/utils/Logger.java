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
package com.iwedia.example.tvinput.utils;

import android.util.Log;

/**
 * Simple class that is used for simplified logging
 */
public class Logger {

    /**
     * Priority constant for the println method; use Log.v.
     */
    public static final int VERBOSE = 2;
    /**
     * Priority constant for the println method; use Log.d.
     */
    public static final int DEBUG = 3;
    /**
     * Priority constant for the println method; use Log.i.
     */
    public static final int INFO = 4;
    /**
     * Priority constant for the println method; use Log.w.
     */
    public static final int WARN = 5;
    /**
     * Priority constant for the println method; use Log.e.
     */
    public static final int ERROR = 6;
    /** TAG used for logging */
    private String mTag;
    /** Output level for logging */
    private int mOutputLevel;

    /**
     * Constructor
     *
     * @param tag         Log tag
     * @param outputLevel Output level for logging. Possible values: VERBOSE = 2; DEBUG = 3; INFO =
     *                    4; WARN = 5; ERROR = 6;
     */
    public Logger(String tag, int outputLevel) {
        mTag = tag;
        mOutputLevel = outputLevel;
    }

    /**
     * Debug level log
     *
     * @param text Text to print
     */
    public void d(String text) {
        if (mOutputLevel >= DEBUG)
            Log.d(mTag, text);
    }

    /**
     * Error level log
     *
     * @param text Text to print
     */
    public void e(String text) {
        if (mOutputLevel >= ERROR)
            Log.e(mTag, text);
    }

    /**
     * Info level log
     *
     * @param text Text to print
     */
    public void i(String text) {
        if (mOutputLevel >= INFO)
            Log.i(mTag, text);
    }

    /**
     * Verbose level log
     *
     * @param text Text to print
     */
    public void v(String text) {
        if (mOutputLevel >= VERBOSE)
            Log.v(mTag, text);
    }

    /**
     * Warning level log
     *
     * @param text Text to print
     */
    public void w(String text) {
        if (mOutputLevel >= WARN)
            Log.w(mTag, text);
    }
}

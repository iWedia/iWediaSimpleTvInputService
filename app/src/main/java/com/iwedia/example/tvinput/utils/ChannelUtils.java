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

import android.content.Context;
import android.content.ContextWrapper;

import com.iwedia.example.tvinput.data.ChannelDescriptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Utility class used for IP channel list management
 */
public class ChannelUtils {

    /** Filename of IP service list located in assets directory */
    private static final String IP_CHANNELS = "ip_service_list.txt";
    /** Application context */
    private static Context mContext;

    /**
     * Initialize IP channels
     */
    public static void initIpChannels(Context ctx) {
        mContext = ctx;
        copyFile(IP_CHANNELS);
    }

    /**
     * Read IP channels from file
     *
     * @param ctx        Application context
     * @param ipChannels Array in which to store read files
     */
    public static void readIpChannels(Context ctx, ArrayList<ChannelDescriptor> ipChannels) {
        mContext = ctx;
        ContextWrapper contextWrapper = new ContextWrapper(ctx);
        String path = contextWrapper.getFilesDir() + "/" + IP_CHANNELS;
        readFile(ctx, path, ipChannels);
    }

    /**
     * Copy IP channel list
     *
     * @param filename File name of IP channel list in assets directory
     */
    private static void copyFile(String filename) {
        ContextWrapper contextWrapper = new ContextWrapper(mContext);
        String file = contextWrapper.getFilesDir().getPath() + "/" + filename;
        File fl = new File(file);
        if (!fl.exists())
            copyAssetToData(fl);
    }

    /**
     * Copy configuration file from assets to data folder.
     *
     * @param file File to copy
     */
    private static void copyAssetToData(File file) {
        try {
            InputStream myInput = mContext.getAssets().open(file.getName());
            String outFileName = file.getPath();
            OutputStream myOutput = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Read the configuration file with built-in application which will be displayed in Content
     * list.
     *
     * @param ctx       Application context
     * @param filePath  IP channel list file path
     * @param arrayList Array in which to store read channels
     */
    public static void readFile(Context ctx, String filePath,
            ArrayList<ChannelDescriptor> arrayList) {
        File file = new File(filePath);
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            String[] separated = new String[2];
            while ((line = br.readLine()) != null) {
                separated = line.split("#");
                if (arrayList == null)
                    arrayList = new ArrayList<ChannelDescriptor>();
                arrayList.add(new ChannelDescriptor("0", separated[1]));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        br = null;
    }
}

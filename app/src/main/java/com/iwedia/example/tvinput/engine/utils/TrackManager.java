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
package com.iwedia.example.tvinput.engine.utils;

import java.util.Locale;

/**
 * Simple class for abstracting Managers that holds Tracks, e.g. Audio Tracks, Subtitle Tracks,
 * Teletext Tracks, Video tracks
 *
 * @param <T> Track Type
 */
public abstract class TrackManager<T> {

    /**
     * Gets the current managers track count
     *
     * @return Track count of the current track type
     */
    public abstract int getTrackCount(int routeId);

    /**
     * Gets a specified track from the manager
     *
     * @param index Index of a track
     * @return Track
     */
    public abstract T getTrack(int routeId, int index);

    /**
     * Convert trigrams for the whole language
     *
     * @param language trigram to convert
     * @return converted trigram
     */
    public String convertTrigramsToLanguage(String language) {
        String languageToDisplay;
        languageToDisplay = checkTrigrams(language);
        if (languageToDisplay.contains(" ")) {
            int indexOfSecondWord = languageToDisplay.indexOf(" ") + 1;
            languageToDisplay = languageToDisplay.substring(0, 1).toUpperCase(
                    new Locale(languageToDisplay));
            languageToDisplay += languageToDisplay.substring(1, indexOfSecondWord);
            languageToDisplay += languageToDisplay.substring(indexOfSecondWord,
                    indexOfSecondWord + 1).toUpperCase(Locale.ENGLISH);
            languageToDisplay += languageToDisplay.substring(indexOfSecondWord + 1);
        } else {
            languageToDisplay = languageToDisplay.substring(0, 1).toUpperCase(Locale.ENGLISH)
                    + languageToDisplay.substring(1);
        }
        return languageToDisplay;
    }

    /**
     * Checks trigrams for the given language. This is used in order to allign MW trigrams with
     * Andorids
     *
     * @param language trigram from MW
     * @return corrected trigram
     */
    protected String checkTrigrams(String language) {
        if (language.equals("fre")) {
            language = "fra";
        } else if (language.equals("sve")) {
            language = "swe";
        } else if (language.equals("dut") || language.equals("nla")) {
            language = "nl";
        } else if (language.equals("ger")) {
            language = "deu";
        } else if (language.equals("alb")) {
            language = "sqi";
        } else if (language.equals("arm")) {
            language = "hye";
        } else if (language.equals("baq")) {
            language = "eus";
        } else if (language.equals("chi")) {
            language = "zho";
        } else if (language.equals("cze")) {
            language = "ces";
        } else if (language.equals("per")) {
            language = "fas";
        } else if (language.equals("gae")) {
            language = "gla";
        } else if (language.equals("geo")) {
            language = "kat";
        } else if (language.equals("gre")) {
            language = "ell";
        } else if (language.equals("ice")) {
            language = "isl";
        } else if (language.equals("ice")) {
            language = "isl";
        } else if (language.equals("mac") || language.equals("mak")) {
            language = "mk";
        } else if (language.equals("may")) {
            language = "msa";
        } else if (language.equals("rum")) {
            language = "ron";
        } else if (language.equals("scr")) {
            language = "sr";
        } else if (language.equals("slo")) {
            language = "slk";
        } else if (language.equals("esl") || language.equals("esp")) {
            language = "spa";
        } else if (language.equals("wel")) {
            language = "cym";
        }
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        String languageToDisplay = Locale.getDefault().getDisplayLanguage();
        if (languageToDisplay.equals("qaa")) {
            languageToDisplay = "Original";
        }
        if (languageToDisplay.equals("mul")) {
            languageToDisplay = "Multiple";
        }
        if (languageToDisplay.equals("und")) {
            languageToDisplay = "Undefined";
        }
        return languageToDisplay;
    }
}

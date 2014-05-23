package com.airwhip.sphinx.getters;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.airwhip.sphinx.misc.XmlHelper;
import com.airwhip.sphinx.parser.Characteristic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Whiplash on 05.03.14.
 */
public class MusicInformation {

    private static final String IS_MUSIC = MediaStore.Audio.Media.IS_MUSIC + " != 0";
    private static final String[] PROJECTION = {MediaStore.Audio.Media.ARTIST};

    private static final String MAIN_TAG_BEGIN = "<music>\n";
    private static final String MAIN_TAG_END = "</music>\n";

    private static final String ITEM_TAG_BEGIN = "\t<item>\n";
    private static final String ITEM_TAG_END = "\t</item>\n";

    private static final String NAME_TAG_BEGIN = "\t\t<name>";
    private static final String NAME_TAG_END = "</name>\n";

    private static final String COUNT_TAG_BEGIN = "\t\t<count>";
    private static final String COUNT_TAG_END = "</count>\n";

    public static int size(Context context) {
        try {
            Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION, IS_MUSIC, null, null);
            return (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) ? cursor.getCount() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public static StringBuilder get(Context context) {
        StringBuilder result = new StringBuilder(MAIN_TAG_BEGIN);

        List<String> artistsList = new ArrayList<>();
        Map<String, Integer> artistsStorage = new HashMap<>();

        try {
            Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION, IS_MUSIC, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    try {
                        String artist = cursor.getString(0);
                        if (!artistsStorage.containsKey(artist)) {
                            artistsList.add(artist);
                            artistsStorage.put(artist, 1);
                        } else {
                            int count = artistsStorage.remove(artist);
                            artistsStorage.put(artist, count + 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Characteristic.updateProgress();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String artist : artistsList) {
            result.append(ITEM_TAG_BEGIN);
            result.append(NAME_TAG_BEGIN + XmlHelper.removeXmlBadSymbols(artist) + NAME_TAG_END);
            result.append(COUNT_TAG_BEGIN + artistsStorage.get(artist) + COUNT_TAG_END);
            result.append(ITEM_TAG_END);
        }

        return result.append(MAIN_TAG_END);
    }

}

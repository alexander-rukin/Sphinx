package com.airwhip.sphinx.getters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.airwhip.sphinx.misc.XmlHelper;

import java.util.List;

/**
 * Created by Whiplash on 05.03.14.
 */
public class ApplicationInformation {

    private static final String MAIN_TAG_BEGIN = "<application>\n";
    private static final String MAIN_TAG_END = "</application>\n";

    private static final String ITEM_TAG_BEGIN = "\t<item>";
    private static final String ITEM_TAG_END = "</item>\n";

    public static StringBuilder get(Context context) {
        StringBuilder result = new StringBuilder(MAIN_TAG_BEGIN);

        final PackageManager pm = context.getPackageManager();
        try {
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo packageInfo : packages) {
                result.append(ITEM_TAG_BEGIN + XmlHelper.removeXmlBadSymbols(packageInfo.packageName) + ITEM_TAG_END);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result.append(MAIN_TAG_END);
    }

}

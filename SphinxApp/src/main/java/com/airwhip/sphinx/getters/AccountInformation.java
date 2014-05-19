package com.airwhip.sphinx.getters;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import com.airwhip.sphinx.misc.Constants;
import com.airwhip.sphinx.misc.Names;
import com.airwhip.sphinx.misc.XmlHelper;
import com.airwhip.sphinx.parser.Characteristic;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Whiplash on 06.03.14.
 */
public class AccountInformation {

    private static final String MAIN_TAG_BEGIN = "<account>\n";
    private static final String MAIN_TAG_END = "</account>\n";

    private static final String ITEM_TAG_BEGIN = "\t<item>\n";
    private static final String ITEM_TAG_END = "\t</item>\n";

    private static final String NAME_TAG_BEGIN = "\t\t<name>";
    private static final String NAME_TAG_END = "</name>\n";

    private static final String TYPE_TAG_BEGIN = "\t\t<type>";
    private static final String TYPE_TAG_END = "</type>\n";

    public static StringBuilder get(Context context) {
        StringBuilder result = new StringBuilder(MAIN_TAG_BEGIN);

        Set<String> emailStorage = new HashSet<>();

        AccountManager am = AccountManager.get(context);
        if (am != null) {
            Account[] accounts = am.getAccounts();
            for (Account ac : accounts) {
                result.append(ITEM_TAG_BEGIN);
                result.append(NAME_TAG_BEGIN + XmlHelper.removeXmlBadSymbols(ac.name) + NAME_TAG_END);
                result.append(TYPE_TAG_BEGIN + XmlHelper.removeXmlBadSymbols(ac.type) + TYPE_TAG_END);
                result.append(ITEM_TAG_END);

                if (ac.name.contains("@")) {
                    emailStorage.add(ac.name.split("@")[0]);
                }

                if (ac.type.equals("com.vkontakte.account") || ac.type.equals("com.tripadvisor.tripadvisor")) {
                    for (String name : ac.name.split(" ")) {
                        for (String translit : Names.getRussianWords(name)) {
                            boolean isMaleName = Names.isMale(translit);
                            boolean isFemaleName = Names.isFemale(translit);
                            if (isMaleName && !isFemaleName) {
                                Characteristic.addMale(Constants.BIG_WEIGHT);
                            }
                            if (!isMaleName && isFemaleName) {
                                Characteristic.addFemale(Constants.BIG_WEIGHT);
                            }
                        }
                    }
                }
            }
        }

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (String email : emailStorage) {
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(email);
            while (matcher.find()) {
                try {
                    int year = Integer.parseInt(matcher.group());
                    if (year < 100 && year >= 70) year += 1900;
                    if (year >= 1950 && year <= currentYear - 10) {
                        Characteristic.addAges((currentYear - year) * Constants.BIG_WEIGHT, Constants.BIG_WEIGHT);
                    }
                } catch (Exception e) {
                    Log.e(Constants.ERROR_TAG, "strange email");
                }
            }
        }

        return result.append(MAIN_TAG_END);
    }

}

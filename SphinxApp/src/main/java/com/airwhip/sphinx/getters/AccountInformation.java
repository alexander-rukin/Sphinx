package com.airwhip.sphinx.getters;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.airwhip.sphinx.misc.Names;
import com.airwhip.sphinx.misc.XmlHelper;
import com.airwhip.sphinx.parser.Characteristic;

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

        AccountManager am = AccountManager.get(context);
        if (am != null) {
            Account[] accounts = am.getAccounts();
            for (Account ac : accounts) {
                result.append(ITEM_TAG_BEGIN);
                result.append(NAME_TAG_BEGIN + XmlHelper.removeXmlBadSymbols(ac.name) + NAME_TAG_END);
                result.append(TYPE_TAG_BEGIN + XmlHelper.removeXmlBadSymbols(ac.type) + TYPE_TAG_END);
                result.append(ITEM_TAG_END);

                if (ac.type.equals("com.vkontakte.account")) {
                    for (String name : ac.name.split(" ")) {
                        boolean isMaleName = Names.isMale(name);
                        boolean isFemaleName = Names.isFemale(name);
                        if (isMaleName && !isFemaleName) {
                            Characteristic.addMale(1000);
                        }
                        if (!isMaleName && isFemaleName) {
                            Characteristic.addFemale(1000);
                        }

                        for (String i : Names.getRussianWords(name)) {
                            isMaleName = Names.isMale(i);
                            isFemaleName = Names.isFemale(i);
                            if (isMaleName && !isFemaleName) {
                                Characteristic.addMale(1000);
                            }
                            if (!isMaleName && isFemaleName) {
                                Characteristic.addFemale(1000);
                            }
                        }
                    }
                }
            }
        }

        return result.append(MAIN_TAG_END);
    }

}

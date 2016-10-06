package edu.cmu.privacy.privacyfirewall;

import java.util.HashMap;

/**
 * Created by ddcv on 10/5/16.
 */


public class Privacyapp extends HashMap<String, String> {
    public String appname;
    public String detail;

    public static String KEY_APPNAME = "appname";
    public static String KEY_DETAIL = "detail";

    public Privacyapp(String appname, String detail) {
        this.appname = appname;
        this.detail = detail;
    }

    @Override
    public String get(Object k) {
        String key = (String) k;
        if (KEY_APPNAME.equals(key))
            return appname;
        else if (KEY_DETAIL.equals(key))
            return detail;
        return null;
    }
}
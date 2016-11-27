package edu.cmu.infosec.privacyfirewall.entity;

import edu.cmu.infosec.privacyfirewall.ConnectionDatabase;

/**
 * Created by YunfanW on 11/27/2016.
 */

public class ConInfo implements Comparable<Object> {
    private String ip;
    private String recipient;
    private String country;
    private String sensitive;
    private int appId;
    private int action;

    public ConInfo(String _ip, String _recipient, String _country, String _sensitive, int _appId,
                   int _action) {
        ip = _ip;
        recipient = _recipient;
        country = _country;
        sensitive = _sensitive;
        appId = _appId;
        action = _action;
    }

    public String getIp() {
        return ip;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getCountry() {
        return country;
    }

    public String getSensitive() {
        return sensitive;
    }

    public int getAppId() {
        return appId;
    }

    public int getAction() {
        return action;
    }

    @Override
    public int compareTo(Object o) {
        ConInfo f = (ConInfo) o;
        if (!getSensitive().equals(ConnectionDatabase.CONTENT_DEFAULT) &&
                f.getSensitive().equals(ConnectionDatabase.CONTENT_DEFAULT)) {
            return -1;
        } else if (getAction() == ConnectionDatabase.ACTION_DENY &&
                    f.getAction() == ConnectionDatabase.ACTION_ALOW) {
            return -1;
        } else {
            return getIp().compareTo(f.getIp());
        }
    }
}

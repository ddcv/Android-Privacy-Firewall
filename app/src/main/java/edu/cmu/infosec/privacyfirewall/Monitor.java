package edu.cmu.infosec.privacyfirewall;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Patterns;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by YunfanW on 9/30/2016.
 */

public class Monitor extends AsyncTask<Void, Void, Void> {
    public static DatabaseInterface db;
    public final static String MONITOR_TAG = "Monitor";
    private IPPacket p;

    public Monitor(IPPacket _p) {
        super();
        p = _p;
    }

    public void filter() {
        int rId;
        int contentSize = p.contentBuffer.remaining();
        byte[] bytes = new byte[contentSize];

        Cursor c;

        p.contentBuffer.get(bytes);

        /** Filter packet */
        c = db.getRuleCursorByAdd(p.ip4Header.destinationAddress.getHostAddress());
        if (c.getCount() < 1) {
            db.insertRule(p.ip4Header.destinationAddress.getHostAddress(), RuleDatabase.ORG_DEFAULT,
                    RuleDatabase.COUNTRY_DEFAULT);
            c = db.getRuleCursorByAdd(p.ip4Header.destinationAddress.getHostAddress());
        }

        /** Get action & rId */
        c.moveToFirst();
        rId = c.getInt(c.getColumnIndex(RuleDatabase.FIELD_ID));

        int port = -1;
        int uid = -1;
        if (p.isTCP()) {
            port = p.tcpHeader.sourcePort;
        } else if (p.isUDP()) {
            port = p.udpHeader.sourcePort;
        }

        if (port != -1) {
            uid = NetUtils.readProcFile(port);
        }

        if (uid != -1) {
            c = db.getApplicationCursorById(uid);
            c.moveToFirst();
            if (c.getCount() > 0) {
                c.moveToFirst();
                ContentValues appVal = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(c, appVal);
                String appName = appVal.getAsString(ApplicationDatabase.FIELD_NAME);

                String plaintext_8 = new String(bytes, StandardCharsets.UTF_8);

                if (!plaintext_8.equals("")) {
                    Log.i(MONITOR_TAG, "size: " + String.valueOf(contentSize));
                    Log.i(MONITOR_TAG, "UTF_8: " + plaintext_8);
                }

                /** Create Connection */
                c = db.getConnectionCursorByAppId(uid);
                boolean exist = false;
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    DatabaseUtils.cursorRowToContentValues(c, appVal);
                    if (appVal.getAsInteger(ConnectionDatabase.FIELD_RULE) == rId) {
                        exist = true;
                        String sensitiveContent = scanSensitive(plaintext_8,
                                appVal.getAsString(ConnectionDatabase.FIELD_CONTENT));
                        Monitor.db.updateSensitive(
                                appVal.getAsInteger(ConnectionDatabase.FIELD_APP),
                                appVal.getAsInteger(ConnectionDatabase.FIELD_RULE),
                                sensitiveContent);
                        break;
                    }
                }

                Log.d(MONITOR_TAG, "exist = " + exist);
                if (!exist) {
                    String sensitiveContent = scanSensitive(plaintext_8,
                            ConnectionDatabase.CONTENT_DEFAULT);
                    int sensitive = sensitiveContent.equals(ConnectionDatabase.CONTENT_DEFAULT) ?
                            ConnectionDatabase.NON_SENSITIVE :
                            ConnectionDatabase.SENSITIVE;
                    db.insertConnection(uid, rId, ConnectionDatabase.ACTION_ALOW,
                            sensitiveContent, sensitive);
                    Log.d(MONITOR_TAG, "uid = " + uid);
                    Log.d(MONITOR_TAG, "rId = " + rId);
                    Log.d(MONITOR_TAG, "AppName = " + appName);
                    Log.d(MONITOR_TAG, "DestAddr = " +
                            p.ip4Header.destinationAddress.getHostAddress());
                    Log.d(MONITOR_TAG, "plaintext = " + plaintext_8);
                }
            }
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        filter();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }

    public static boolean checkIPAddr(String ipaddr) {
        return Patterns.IP_ADDRESS.matcher(ipaddr).matches();
    }

    final static Pattern pattern_phone_general =
            Pattern.compile("(?:.*)(\\(?([1]?[2-9]\\d{2})\\)?[\\s-]?([2-9]\\d{2})[\\s-]?([\\d]{4}))(?:.*)");
    final static Pattern pattern_phone =
            Pattern.compile("(?:.*)(%28(\\d{3})%29(\\d{3})-(\\d{4}))(?:.*)");
//    final static Pattern pattern_email =
//            Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");

    private static String scanSensitive(String plaintext, String sensitive_org) {
        String result = sensitive_org;

        Matcher m = pattern_phone.matcher(plaintext);
        if (m.find() && !sensitive_org.contains(m.group(1))) {
            String append = ConnectionDatabase.CONTENT_PHONE + "(\"" + m.group(1) + "\")";
            append = append.replace("%28", "(");
            append = append.replace("%29", ")");
            if (result.equals(ConnectionDatabase.CONTENT_DEFAULT)) {
                result = append;
            } else {
                result = result + ", " + append;
            }
        }

//        m = pattern_email.matcher(plaintext);
//        if (m.find() && !sensitive_org.contains(m.group(1))) {
//            String append = ConnectionDatabase.CONTENT_EMAIL + "(\"" + m.group(1) + "\")";
//            if (result.equals(ConnectionDatabase.CONTENT_DEFAULT)) {
//                result = append;
//            } else {
//                result = result + ", " + append;
//            }
//        }

        return result;
    }
}

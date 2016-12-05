package edu.cmu.infosec.privacyfirewall;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
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

    public static boolean scan_general_phone = false;
    public static boolean scan_strict_phone = true;
    public static boolean scan_email = false;
    public static boolean scan_ssn = false;
    public static boolean scan_credit_card = false;

    private static int notificationID = 0;

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

//                if (!plaintext_8.equals("")) {
//                    Log.i(MONITOR_TAG, "size: " + String.valueOf(contentSize));
//                    Log.i(MONITOR_TAG, "UTF_8: " + plaintext_8);
//                }

                /** Create Connection */
                c = db.getConnectionCursorByAppId(uid);
                boolean exist = false;
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    DatabaseUtils.cursorRowToContentValues(c, appVal);
                    if (appVal.getAsInteger(ConnectionDatabase.FIELD_RULE) == rId) {
                        exist = true;
                        String sensitiveContent = scanSensitive(plaintext_8,
                                appVal.getAsString(ConnectionDatabase.FIELD_CONTENT), appName);
                        Monitor.db.updateSensitive(
                                appVal.getAsInteger(ConnectionDatabase.FIELD_APP),
                                appVal.getAsInteger(ConnectionDatabase.FIELD_RULE),
                                sensitiveContent);
                        break;
                    }
                }

//                Log.d(MONITOR_TAG, "exist = " + exist);
                if (!exist) {
                    String sensitiveContent = scanSensitive(plaintext_8,
                            ConnectionDatabase.CONTENT_DEFAULT, appName);
                    int sensitive = sensitiveContent.equals(ConnectionDatabase.CONTENT_DEFAULT) ?
                            ConnectionDatabase.NON_SENSITIVE :
                            ConnectionDatabase.SENSITIVE;
                    db.insertConnection(uid, rId, ConnectionDatabase.ACTION_ALOW,
                            sensitiveContent, sensitive);
//                    Log.d(MONITOR_TAG, "uid = " + uid);
//                    Log.d(MONITOR_TAG, "rId = " + rId);
//                    Log.d(MONITOR_TAG, "AppName = " + appName);
//                    Log.d(MONITOR_TAG, "DestAddr = " +
//                            p.ip4Header.destinationAddress.getHostAddress());
//                    Log.d(MONITOR_TAG, "plaintext = " + plaintext_8);
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

    final static Pattern pattern_general_phone =
            Pattern.compile("(?:.*)(\\(?([1]?[2-9]\\d{2})\\)?[\\s-]?([2-9]\\d{2})[\\s-]?([\\d]{4}))(?:.*)");
    final static Pattern pattern_url_phone =
            Pattern.compile("(?:.*)(%28(\\d{3})%29(\\d{3})-(\\d{4}))(?:.*)");
    final static Pattern pattern_email =
            Pattern.compile("(?:.*)([_a-zA-Z0-9-]+@([_a-zA-Z0-9-]+\\.)+[_a-zA-Z0-9-]{2,4})(?:.*)");
    final static Pattern pattern_url_email =
            Pattern.compile("(?:=)([_a-zA-Z0-9-]+(@|%40)([_a-zA-Z0-9-]+\\.)+[_a-zA-Z0-9-]{2,4})(?:[&\\n])");
    final static Pattern pattern_ssn =
            Pattern.compile("(?:.*)(\\d{3}-\\d{2}-\\d{4})(?:.*)");
    final static Pattern pattern_credit_card =
            Pattern.compile("(?:.*)(((4\\d{3})|(5[1-5]\\d{2})|(6011))-?\\d{4}-?\\d{4}-?\\d{4}|3[4,7]\\d{13})(?:.*)");

    private static String scanSensitive(String plaintext, String sensitive_org, String appName) {
        String result = sensitive_org;
        Matcher m;

        NotificationManager mNotificationManager =
                (NotificationManager) ContextUtil.getInstance()
                                                    .getSystemService(Context.NOTIFICATION_SERVICE);

        if (scan_strict_phone) {
            m = pattern_url_phone.matcher(plaintext);
            if (m.find()) {
                String append = m.group(1);
                append = append.replace("%28", "(");
                append = append.replace("%29", ")");

                /** Anonymity */
                append = append.substring(0, 5) + "***-" + append.substring(9, 13);

                append = ConnectionDatabase.CONTENT_PHONE + "(\"" + append + "\")";

                if (result.contains(append)) {
                    ;
                } else {
                    sendNotification(append, appName, mNotificationManager);

                    if (result.equals(ConnectionDatabase.CONTENT_DEFAULT)) {
                        result = append;
                    } else {
                        result = result + ", \n" + append;
                    }
                }
            }
        }

        if (scan_general_phone) {
            m = pattern_general_phone.matcher(plaintext);
            if (m.find() && !sensitive_org.contains(m.group(1))) {
                String append = m.group(1);

                /** Anonymity */
                append = append.substring(0, 3) + "***-" + append.substring(6, 10);

                append = ConnectionDatabase.CONTENT_PHONE + "(\"" + append + "\")";

                if (result.contains(append)) {
                    ;
                } else {
                    sendNotification(append, appName, mNotificationManager);

                    if (result.equals(ConnectionDatabase.CONTENT_DEFAULT)) {
                        result = append;
                    } else {
                        result = result + ", \n" + append;
                    }
                }
            }
        }

        if (scan_email) {
            m = pattern_url_email.matcher(plaintext);
            if (m.find() && !sensitive_org.contains(m.group(1))) {
                String append = m.group(1);
                append = append.replace("%40", "@");

                /** Anonymity */
                int index = append.indexOf('@');
                if (index > 2) {
                    String repeated = new String(new char[index - 2]).replace("\0", "*");
                    append = repeated + append.substring(index - 2);
                }

                append = ConnectionDatabase.CONTENT_EMAIL + "(\"" + append + "\")";

                if (result.contains(append)) {
                    ;
                } else {
                    sendNotification(append, appName, mNotificationManager);

                    if (result.equals(ConnectionDatabase.CONTENT_DEFAULT)) {
                        result = append;
                    } else {
                        result = result + ", \n" + append;
                    }
                }
            }
        }

        if (scan_ssn) {
            m = pattern_ssn.matcher(plaintext);
            if (m.find() && !sensitive_org.contains(m.group(1))) {
                String append = m.group(1);

                /** Anonymity */
                append = "***-**-" + append.substring(7, 11);

                append = ConnectionDatabase.CONTENT_SSN + "(\"" + append + "\")";

                if (result.contains(append)) {
                    ;
                } else {
                    sendNotification(append, appName, mNotificationManager);

                    if (result.equals(ConnectionDatabase.CONTENT_DEFAULT)) {
                        result = append;
                    } else {
                        result = result + ", \n" + append;
                    }
                }
            }
        }

        if (scan_credit_card) {
            m = pattern_credit_card.matcher(plaintext);
            if (m.find() && !sensitive_org.contains(m.group(1))) {
                String append = m.group(1);

                /** Anonymity */
                append = append.substring(0, 4) + "-****-****-" + append.substring(15, 19);

                append = ConnectionDatabase.CONTENT_CREDIT_CARD + "(\"" + append + "\")";

                if (result.contains(append)) {
                    ;
                } else {
                    sendNotification(append, appName, mNotificationManager);

                    if (result.equals(ConnectionDatabase.CONTENT_DEFAULT)) {
                        result = append;
                    } else {
                        result = result + ", \n" + append;
                    }
                }
            }
        }

        return result;
    }

    private static void sendNotification(String message, String appName,
                                  NotificationManager mNotificationManager) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ContextUtil.getInstance());
        android.support.v4.app.NotificationCompat.BigTextStyle style =
                new android.support.v4.app.NotificationCompat.BigTextStyle();

        mBuilder.setSmallIcon(R.drawable.ic_warning_black_24dp);
        mBuilder.setContentText(appName + " is sending " + message);
        mBuilder.setContentTitle("Detect Potential Data Leak!");
        style.setBigContentTitle("Detect Potential Data Leak!");
        style.bigText(appName + " is sending " + message);
        mBuilder.setStyle(style);
        mNotificationManager.notify(notificationID++, mBuilder.build());
    }
}
